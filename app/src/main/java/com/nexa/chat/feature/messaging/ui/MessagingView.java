package com.nexa.chat.feature.messaging.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

import androidx.core.view.ViewCompat;

import com.nexa.chat.R;

import com.nexa.chat.feature.messaging.model.*;
import com.nexa.chat.feature.messaging.repository.ChatFailure;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * TV2 surface, not Session/Home integration. Host retains controller across recreation and removes
 * the old surface on account switch. Only UI state is View-owned; all account mutations use controller.
 * Render callbacks already running on main may finish; host must serialize account surface replacement
 * on main. Queued results check binding/generation/current scope and never resurrect a detached View.
 */
public final class MessagingView extends LinearLayout {
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final Executor MAIN_EXECUTOR = command -> {
        if (!MAIN.post(command)) throw new RejectedExecutionException("Main looper unavailable");
    };

    private final MessagingController controller;
    private final MessagingRouter router;
    private final MessagingPresentation presentation;
    private MessagingRouter.Subscription routeSubscription;
    private long renderedRevision = -1;
    private boolean historyLoading;
    private boolean historyDirty;
    private Button sendButton;
    private MessagingController.ViewSubscription subscription;
    private Binding binding;
    private String conversation;
    private int generation;
    private LinearLayout rows;
    private LinearLayout feedback;
    private ScrollView historyScroll;
    private EditText input;
    private final DraftInputOrigin inputOrigin = new DraftInputOrigin();

    /** PROPOSED TV2 entry: host retains controller and its router, not this View. */
    public MessagingView(Context context, MessagingController controller, MessagingRouter router) {
        super(context);
        setOrientation(VERTICAL);
        setBackgroundColor(context.getColor(R.color.messaging_background));
        this.controller = Objects.requireNonNull(controller);
        this.router = Objects.requireNonNull(router);
        if (router != controller.getRouter()) throw new IllegalArgumentException("Different account router");
        presentation = new MessagingPresentation(router);
    }

    /** Main-thread host signals; these are proposed, not an approved Home interface. */
    public void setHostState(boolean messagesSelected, boolean hostResumed) {
        if (!presentation.setHostState(messagesSelected, hostResumed)) return;
        generation++;
        historyLoading = false;
        renderedRevision = -1;
        if (presentation.isActive()) renderRoute();
    }

    public boolean handleBack() { return router.handleBack(); }

    private void renderRoute() {
        if (!controller.isActive()) { showInvalidated(); return; }
        if (!presentation.isActive()) return;
        MessagingRouter.Route route = router.currentRoute();
        if (renderedRevision == route.getRevision()) return;
        renderedRevision = route.getRevision();
        historyLoading = false;
        if (route.getConversationId() == null) renderList();
        else renderConversation(route.getConversationId(), route.getTitle());
    }

    private void stateChanged() {
        if (!controller.isActive()) { showInvalidated(); return; }
        if (!presentation.isActive()) return;
        if (renderedRevision != router.currentRoute().getRevision()) renderRoute();
        else if (conversation != null) {
            if (historyLoading) historyDirty = true;
            else loadHistory();
        }
    }

    private void loadHistory() {
        historyLoading = true;
        historyDirty = false;
        await(controller.loadMessages(conversation), RequestKind.HISTORY);
    }

    public void refresh() {
        if (!controller.isActive()) { showInvalidated(); return; }
        renderedRevision = -1;
        renderRoute();
    }

    public void showList() { router.showList(); renderRoute(); }

    private void renderList() {
        if (!controller.isActive()) { showInvalidated(); return; }
        conversation = null;
        generation++;
        removeAllViews();
        input = null;
        sendButton = null;
        LinearLayout heading = section(this);
        TextView title = label(heading, getContext().getString(R.string.messaging_title));
        MessagingUi.textSize(title, R.dimen.messaging_title_text);
        title.setTypeface(null, Typeface.BOLD);
        ViewCompat.setAccessibilityHeading(title, true);
        LinearLayout direct = new LinearLayout(getContext());
        direct.setGravity(Gravity.CENTER_VERTICAL);
        heading.addView(direct);
        EditText peer = new EditText(getContext());
        peer.setHint(R.string.messaging_peer_hint);
        peer.setSingleLine(true);
        peer.setMinHeight(size(R.dimen.messaging_touch_target));
        MessagingUi.textSize(peer, R.dimen.messaging_body_text);
        peer.setTextColor(getContext().getColor(R.color.messaging_text));
        peer.setHintTextColor(getContext().getColor(R.color.messaging_secondary));
        direct.addView(peer, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        Button open = button(direct, getContext().getString(R.string.messaging_open_direct),
                () -> openDirect(peer.getText().toString().trim()));
        open.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        feedback = section(this);
        feedback.setVisibility(GONE);
        rows = list();
        showLoading();
        await(controller.listConversations(), RequestKind.LIST);
    }

    public void openDirect(String peer) {
        if (peer == null || peer.trim().isEmpty()) return;
        if (!controller.isActive()) { showInvalidated(); return; }
        // Navigation completion is account-owned and captures no View. No parallel UI navigation.
        await(controller.openDirectAndNavigate(peer), RequestKind.DIRECT);
    }

    public void showConversation(String id, String name) { router.openConversation(id, name); renderRoute(); }

    private void renderConversation(String id, String name) {
        if (!controller.isActive()) { showInvalidated(); return; }
        conversation = id;
        generation++;
        removeAllViews();
        LinearLayout header = section(this);
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(getContext().getColor(R.color.messaging_surface));
        Button back = button(header, getContext().getString(R.string.messaging_back), this::handleBack);
        back.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        TextView title = label(header, name);
        title.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        title.setMaxLines(2);
        title.setEllipsize(TextUtils.TruncateAt.END);
        title.setContentDescription(name);
        ViewCompat.setAccessibilityHeading(title, true);
        title.setTypeface(null, Typeface.BOLD);
        MessagingUi.textSize(title, R.dimen.messaging_header_text);
        title.setCompoundDrawablesRelative(MessagingUi.avatar(getContext(), name), null, null, null);
        title.setCompoundDrawablePadding(size(R.dimen.messaging_space));
        rows = list();
        historyScroll.setId(R.id.messaging_history);
        showLoading();
        feedback = section(this);
        feedback.setVisibility(GONE);
        LinearLayout composer = section(this);
        composer.setOrientation(HORIZONTAL);
        composer.setGravity(Gravity.BOTTOM);
        composer.setBackgroundColor(getContext().getColor(R.color.messaging_surface));
        input = new EditText(getContext());
        input.setId(R.id.messaging_composer);
        input.setHint(R.string.messaging_composer_hint);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        input.setMinLines(1);
        input.setMaxLines(4);
        input.setMinHeight(size(R.dimen.messaging_touch_target));
        input.setGravity(Gravity.TOP | Gravity.START);
        input.setTextColor(getContext().getColor(R.color.messaging_text));
        input.setHintTextColor(getContext().getColor(R.color.messaging_secondary));
        MessagingUi.textSize(input, R.dimen.messaging_body_text);
        input.setBackground(MessagingUi.surface(getContext(), R.color.messaging_background, true));
        int padding = size(R.dimen.messaging_space_medium);
        input.setPadding(padding, padding, padding, padding);
        synchronizeDraft(controller.draft(id));
        composer.addView(input, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean genuineUserInput = inputOrigin.isGenuineUserInput();
                if (!genuineUserInput || !presentation.canType(id, genuineUserInput)) return;
                try { controller.editDraft(id, s.toString()); }
                catch (CancellationException closed) { showInvalidated(); }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        sendButton = button(composer, getContext().getString(R.string.messaging_send), () -> {
            CompletionStage<OutboxEntry> request = controller.sendDraft(id);
            sendButton.setEnabled(!controller.isSending(id));
            await(request, RequestKind.SEND);
        });
        sendButton.setId(R.id.messaging_send);
        LayoutParams sendLayout = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        sendLayout.setMarginStart(size(R.dimen.messaging_space));
        sendButton.setLayoutParams(sendLayout);
        sendButton.setEnabled(!controller.isSending(id));
        loadHistory();
    }

    /** No future continuation captures this View or its child Views. */
    private void await(CompletionStage<?> request, RequestKind kind) {
        deliver(request, new WeakReference<>(binding), generation, presentation.capture(), router.navigationIntent(), kind);
    }

    private static void deliver(CompletionStage<?> request, WeakReference<Binding> target,
                                int version, MessagingPresentation.Token token, long intent, RequestKind kind) {
        request.whenCompleteAsync((value, error) -> {
                    Binding current = target.get();
                    MessagingView view = current == null ? null : current.currentView();
                    if (view == null || version != view.generation) return;
                    if (!view.controller.isActive()) { view.showInvalidated(); return; }
                    if (!view.presentation.accepts(token)) return;
                    if (kind == RequestKind.DIRECT && !view.router.isCurrentIntent(intent)) return;
                    view.onResult(kind, value, error);
        }, MAIN_EXECUTOR);
    }

    @SuppressWarnings("unchecked")
    private void onResult(RequestKind kind, Object value, Throwable error) {
        if (!controller.isActive()) { showInvalidated(); return; }
        if (kind == RequestKind.SEND) {
            if (error != null) showFeedback(error(error));
            return;
        }
        if (kind == RequestKind.DIRECT) {
            if (error != null) {
                showFeedback(error(error));
                button(feedback, getContext().getString(R.string.messaging_return), this::showList);
            }
            return;
        }
        if (kind == RequestKind.HISTORY) historyLoading = false;
        // Follow new messages only while already at the end; keep a reader's older viewport.
        boolean followEnd = !historyScroll.canScrollVertically(1);
        int scrollY = historyScroll.getScrollY();
        rows.removeAllViews();
        if (kind == RequestKind.LIST) {
            if (error != null) {
                label(rows, error(error));
                button(rows, getContext().getString(R.string.messaging_retry), this::refresh);
                return;
            }
            List<Conversation> items = (List<Conversation>) value;
            if (items.isEmpty()) showEmpty(R.string.messaging_empty_list,
                    R.string.messaging_empty_list_detail);
            for (Conversation item : items) {
                Button row = button(rows, item.getTitle(), () ->
                        router.openConversation(item.getId(), item.getTitle()));
                row.setId(R.id.messaging_conversation);
                row.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                row.setMinHeight(size(R.dimen.messaging_conversation_height));
                row.setBackgroundTintList(null);
                row.setBackground(MessagingUi.rowBackground(getContext()));
                row.setCompoundDrawablesRelative(MessagingUi.avatar(getContext(), item.getTitle()),
                        null, null, null);
                row.setCompoundDrawablePadding(size(R.dimen.messaging_space_medium));
                row.setContentDescription(getContext().getString(
                        R.string.messaging_open_conversation, item.getTitle()));
                LayoutParams layout = (LayoutParams) row.getLayoutParams();
                layout.bottomMargin = size(R.dimen.messaging_space);
            }
            return;
        }
        List<OutboxEntry> entries = error == null ? (List<OutboxEntry>) value
                : controller.localEntries(conversation);
        if (error != null) {
            label(rows, error(error));
            button(rows, getContext().getString(R.string.messaging_reload), this::refresh);
        }
        if (error == null && entries.isEmpty()) showEmpty(R.string.messaging_empty_history,
                R.string.messaging_empty_history_detail);
        for (OutboxEntry entry : entries) {
            if (!controller.isActive()) { showInvalidated(); return; }
            renderMessage(entry);
        }
        if (!input.getText().toString().equals(controller.draft(conversation))) {
            synchronizeDraft(controller.draft(conversation));
        }
        Throwable failure = controller.lastFailure(conversation);
        showFeedback(failure == null ? null : error(failure));
        sendButton.setEnabled(!controller.isSending(conversation));
        restoreScroll(followEnd, scrollY);
        if (historyDirty) loadHistory();
    }

    private void synchronizeDraft(String draft) {
        inputOrigin.synchronize(() -> input.setText(draft));
    }

    private void showInvalidated() {
        generation++;
        conversation = null;
        renderedRevision = -1;
        historyLoading = false;
        historyDirty = false;
        closeBinding();
        removeAllViews();
        rows = null;
        input = null;
        sendButton = null;
        historyScroll = null;
        feedback = null;
        label(this, getContext().getString(R.string.messaging_invalidated));
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        closeBinding();
        presentation.setAttached(true);
        renderedRevision = -1;
        Binding next = new Binding(this);
        binding = next;
        // Atomic subscribe returns a closed handle if invalidation won; no isActive/observe TOCTOU.
        subscription = controller.observe(MAIN_EXECUTOR, next::refresh, next::invalidated);
        routeSubscription = router.observe(MAIN_EXECUTOR, next::routeChanged);
        if (!subscription.isOpen() || !controller.isActive()) showInvalidated();
    }

    @Override protected void onDetachedFromWindow() {
        generation++;
        presentation.setAttached(false);
        closeBinding();
        removeAllViews();
        rows = null;
        input = null;
        sendButton = null;
        conversation = null;
        historyScroll = null;
        feedback = null;
        // Detach is not logout. The retained controller still owns ACK/retry/draft state.
        super.onDetachedFromWindow();
    }

    private void closeBinding() {
        if (binding != null) { binding.view.clear(); binding = null; }
        if (subscription != null) { subscription.close(); subscription = null; }
        if (routeSubscription != null) { routeSubscription.close(); routeSubscription = null; }
    }

    private static final class Binding {
        final WeakReference<MessagingView> view;
        Binding(MessagingView view) { this.view = new WeakReference<>(view); }
        MessagingView currentView() {
            MessagingView value = view.get();
            return value != null && value.binding == this && value.isAttachedToWindow() ? value : null;
        }
        void refresh() {
            MessagingView value = currentView();
            if (value != null) value.stateChanged();
        }
        void routeChanged() {
            MessagingView value = currentView();
            if (value != null) value.renderRoute();
        }
        void invalidated() {
            MessagingView value = currentView();
            if (value != null) value.showInvalidated();
        }
    }

    private enum RequestKind { LIST, DIRECT, HISTORY, SEND }

    private Button button(LinearLayout parent, String text, Runnable click) {
        Button button = new Button(getContext());
        button.setText(text);
        button.setAllCaps(false);
        button.setMinHeight(size(R.dimen.messaging_touch_target));
        button.setTextColor(getContext().getColor(R.color.messaging_accent));
        button.setBackgroundTintList(ColorStateList.valueOf(
                getContext().getColor(R.color.messaging_accent_container)));
        MessagingUi.textSize(button, R.dimen.messaging_body_text);
        button.setOnClickListener(view -> {
            if (controller.isActive() && presentation.isActive()) click.run();
            else if (!controller.isActive()) showInvalidated();
        });
        parent.addView(button);
        return button;
    }

    private TextView label(LinearLayout parent, String text) {
        TextView view = new TextView(getContext());
        view.setText(text);
        view.setTextColor(getContext().getColor(R.color.messaging_text));
        MessagingUi.textSize(view, R.dimen.messaging_body_text);
        parent.addView(view);
        return view;
    }

    private int size(int resource) { return MessagingUi.size(getContext(), resource); }

    private LinearLayout section(LinearLayout parent) {
        LinearLayout section = new LinearLayout(getContext());
        section.setOrientation(VERTICAL);
        int padding = size(R.dimen.messaging_space_medium);
        section.setPadding(padding, padding, padding, padding);
        parent.addView(section, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        return section;
    }

    private LinearLayout list() {
        ScrollView scroll = new ScrollView(getContext());
        scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(getContext());
        content.setOrientation(VERTICAL);
        int padding = size(R.dimen.messaging_space_large);
        content.setPadding(padding, padding, padding, padding);
        scroll.addView(content);
        addView(scroll, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1));
        historyScroll = scroll;
        return content;
    }

    private void showLoading() {
        LinearLayout loading = section(rows);
        loading.setGravity(Gravity.CENTER_HORIZONTAL);
        ProgressBar progress = new ProgressBar(getContext());
        int target = size(R.dimen.messaging_avatar);
        loading.addView(progress, new LayoutParams(target, target));
        TextView text = label(loading, getContext().getString(R.string.messaging_loading));
        text.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
    }

    private void showEmpty(int title, int detail) {
        LinearLayout state = section(rows);
        TextView heading = label(state, getContext().getString(title));
        heading.setTypeface(null, Typeface.BOLD);
        TextView description = label(state, getContext().getString(detail));
        description.setTextColor(getContext().getColor(R.color.messaging_secondary));
    }

    private void showFeedback(String text) {
        feedback.removeAllViews();
        feedback.setVisibility(text == null ? GONE : VISIBLE);
        if (text != null) {
            TextView message = label(feedback, text);
            message.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
            MessagingUi.textSize(message, R.dimen.messaging_caption_text);
        }
    }

    private void renderMessage(OutboxEntry entry) {
        Message message = entry.getMessage();
        boolean outgoing = controller.getCurrentUserId().toString().equals(message.getSenderId());
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(HORIZONTAL);
        LayoutParams rowLayout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        rowLayout.bottomMargin = size(R.dimen.messaging_space_medium);
        rows.addView(row, rowLayout);
        Space gutter = new Space(getContext());
        LayoutParams gutterLayout = new LayoutParams(size(R.dimen.messaging_bubble_gutter), 1);
        if (outgoing) row.addView(gutter, gutterLayout);
        LinearLayout lane = new LinearLayout(getContext());
        lane.setOrientation(VERTICAL);
        lane.setGravity(outgoing ? Gravity.END : Gravity.START);
        row.addView(lane, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        if (!outgoing) row.addView(gutter, gutterLayout);
        LinearLayout bubble = new LinearLayout(getContext());
        bubble.setId(R.id.messaging_message_bubble);
        bubble.setTag(outgoing);
        bubble.setOrientation(VERTICAL);
        bubble.setBackground(MessagingUi.surface(getContext(), outgoing
                ? R.color.messaging_accent_container : R.color.messaging_surface, !outgoing));
        int padding = size(R.dimen.messaging_space_medium);
        bubble.setPadding(padding, padding, padding, padding);
        lane.addView(bubble, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        String status = getContext().getString(MessagingUi.state(entry.getState()));
        String rendered = getContext().getString(R.string.messaging_message_with_state,
                message.getText(), status);
        SpannableString styled = new SpannableString(rendered);
        int statusStart = rendered.length() - status.length();
        styled.setSpan(new RelativeSizeSpan(0.8f), statusStart, rendered.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(new StyleSpan(Typeface.BOLD), statusStart, rendered.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        TextView body = label(bubble, rendered);
        body.setId(R.id.messaging_message_text);
        body.setText(styled);
        body.setContentDescription(getContext().getString(R.string.messaging_message_description,
                getContext().getString(outgoing ? R.string.messaging_you : R.string.messaging_incoming),
                message.getText(), getContext().getString(MessagingUi.stateDescription(entry.getState()))));
        String timestamp = MessagingUi.timestamp(message.getCreatedAt());
        if (!timestamp.isEmpty()) {
            TextView time = label(bubble, timestamp);
            time.setId(R.id.messaging_timestamp);
            time.setTextColor(getContext().getColor(R.color.messaging_secondary));
            MessagingUi.textSize(time, R.dimen.messaging_caption_text);
        }
        if (entry.getReceiptStatus() != MessageStatus.NONE) {
            TextView receipt = label(bubble, getContext().getString(
                    entry.getReceiptStatus() == MessageStatus.READ
                            ? R.string.messaging_receipt_read : R.string.messaging_receipt_delivered));
            MessagingUi.textSize(receipt, R.dimen.messaging_caption_text);
        }
        if ((entry.getState() == OutboxState.PENDING || entry.getState() == OutboxState.FAILED)
                && controller.canRetry(message.getClientMessageId())) {
            button(lane, getContext().getString(R.string.messaging_retry_send), () ->
                    await(controller.retry(message.getClientMessageId()), RequestKind.SEND));
        }
    }

    private void restoreScroll(boolean followEnd, int previousY) {
        ScrollView target = historyScroll;
        int version = generation;
        target.post(() -> {
            if (target != historyScroll || version != generation || !presentation.isActive()
                    || !controller.isActive()) return;
            // scrollTo never moves focus out of the editor.
            target.scrollTo(0, followEnd ? rows.getHeight() : previousY);
        });
    }

    private String error(Throwable error) {
        while (error.getCause() != null) error = error.getCause();
        int resource = R.string.messaging_error;
        if (error instanceof ChatFailure) {
            switch (((ChatFailure) error).getCode()) {
                case OFFLINE: resource = R.string.messaging_error_offline; break;
                case TIMEOUT: resource = R.string.messaging_error_timeout; break;
                case UNAUTHENTICATED: resource = R.string.messaging_error_auth; break;
                case FORBIDDEN: resource = R.string.messaging_error_forbidden; break;
                case CONFLICT: resource = R.string.messaging_error_conflict; break;
                case INVALID_ARGUMENT: resource = R.string.messaging_error_invalid; break;
            }
        }
        return getContext().getString(resource);
    }
}
