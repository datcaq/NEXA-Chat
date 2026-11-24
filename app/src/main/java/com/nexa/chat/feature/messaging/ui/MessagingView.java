package com.nexa.chat.feature.messaging.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;

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
    private EditText input;
    private final DraftInputOrigin inputOrigin = new DraftInputOrigin();

    /** PROPOSED TV2 entry: host retains controller and its router, not this View. */
    public MessagingView(Context context, MessagingController controller, MessagingRouter router) {
        super(context);
        setOrientation(VERTICAL);
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
        label(this, "Tin nhắn · FAKE DEMO");
        EditText peer = new EditText(getContext());
        peer.setHint("Peer user ID");
        addView(peer);
        button(this, "Mở chat 1-1", () -> openDirect(peer.getText().toString().trim()));
        rows = list();
        label(rows, "Đang tải…");
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
        button(this, "‹ Danh sách", this::handleBack);
        label(this, name);
        rows = list();
        label(rows, "Đang tải…");
        input = new EditText(getContext());
        input.setHint("Nhập tin nhắn");
        synchronizeDraft(controller.draft(id));
        addView(input);
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
        sendButton = button(this, "Gửi", () -> {
            CompletionStage<OutboxEntry> request = controller.sendDraft(id);
            sendButton.setEnabled(!controller.isSending(id));
            await(request, RequestKind.SEND);
        });
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
            if (error != null) label(this, error(error));
            return;
        }
        if (kind == RequestKind.DIRECT) {
            if (error != null) {
                label(this, error(error));
                button(this, "Quay lại", this::showList);
            }
            return;
        }
        if (kind == RequestKind.HISTORY) historyLoading = false;
        rows.removeAllViews();
        if (kind == RequestKind.LIST) {
            if (error != null) {
                label(rows, error(error));
                button(rows, "Thử lại", this::refresh);
                return;
            }
            List<Conversation> items = (List<Conversation>) value;
            if (items.isEmpty()) label(rows, "Chưa có hội thoại");
            for (Conversation item : items) button(rows, item.getTitle(), () -> {
                router.openConversation(item.getId(), item.getTitle());
            });
            return;
        }
        List<OutboxEntry> entries = error == null ? (List<OutboxEntry>) value
                : controller.localEntries(conversation);
        if (error != null) {
            label(rows, error(error));
            button(rows, "Tải lại", this::refresh);
        }
        for (OutboxEntry entry : entries) {
            if (!controller.isActive()) { showInvalidated(); return; }
            Message message = entry.getMessage();
            label(rows, message.getText() + " · " + entry.getState());
            if ((entry.getState() == OutboxState.PENDING || entry.getState() == OutboxState.FAILED)
                    && controller.canRetry(message.getClientMessageId())) {
                button(rows, "Thử gửi lại", () ->
                        await(controller.retry(message.getClientMessageId()), RequestKind.SEND));
            }
        }
        if (!input.getText().toString().equals(controller.draft(conversation))) {
            synchronizeDraft(controller.draft(conversation));
        }
        Throwable failure = controller.lastFailure(conversation);
        if (failure != null) label(rows, error(failure));
        sendButton.setEnabled(!controller.isSending(conversation));
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
        label(this, "Phiên nhắn tin đã kết thúc");
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
        button.setOnClickListener(view -> {
            if (controller.isActive() && presentation.isActive()) click.run();
            else if (!controller.isActive()) showInvalidated();
        });
        parent.addView(button);
        return button;
    }

    private void label(LinearLayout parent, String text) {
        TextView view = new TextView(getContext());
        view.setText(text);
        parent.addView(view);
    }

    private LinearLayout list() {
        ScrollView scroll = new ScrollView(getContext());
        LinearLayout content = new LinearLayout(getContext());
        content.setOrientation(VERTICAL);
        scroll.addView(content);
        addView(scroll, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1));
        return content;
    }

    private String error(Throwable error) {
        while (error.getCause() != null) error = error.getCause();
        return error instanceof ChatFailure ? ((ChatFailure) error).getCode().name()
                : "Không thực hiện được thao tác";
    }
}
