package com.nexa.chat.feature.messaging;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.nexa.chat.R;
import com.nexa.chat.feature.messaging.model.*;
import com.nexa.chat.feature.messaging.repository.*;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.UnaryOperator;

/** Focused IMP-01 presentation checks. All navigation, typing, send and retry use real UI actions. */
@RunWith(AndroidJUnit4.class)
public final class MessagingUiPresentationTest {
    private static final String LONG = "Synthetic incoming message: a long conversation should remain "
            + "readable without covering the composer. This text checks natural wrapping across "
            + "many lines, spacing inside the bubble, and the distinction between incoming and "
            + "outgoing messages. No production account or service is used.";
    private MessagingTestHost host;

    @After public void closeAccountAndRuntime() throws Exception {
        if (host != null) host.close();
    }

    @Test public void ui01_identityBubblesWrappingAndAuthoritativeTimestamp() throws Exception {
        host = MessagingTestHost.launch(MessagingTestFixtures.presentation(true,
                delegate -> new UiHistory(delegate, false)));
        awaitConversation();
        onView(conversation()).check((view, missing) -> {
            if (missing != null) throw missing;
            Button row = (Button) view;
            assertNotNull("Deterministic avatar", row.getCompoundDrawablesRelative()[0]);
            assertTrue("Accessible row target", row.getHeight() >= target(row));
            assertTrue(row.getContentDescription().toString().contains(MessagingTestFixtures.PEER));
        });
        capture("01-conversation-list");
        openConversation();
        MessagingTestHost.awaitDisplayed(withText(LONG + " · SENT"));
        onView(withText(MessagingTestFixtures.HISTORY_ROW)).check((view, missing) -> {
            if (missing != null) throw missing;
            ViewGroup bubble = (ViewGroup) view.getParent();
            assertEquals(Boolean.TRUE, bubble.getTag());
            assertNull("Unknown message timestamp stays absent",
                    bubble.findViewById(R.id.messaging_timestamp));
        });
        onView(withText(LONG + " · SENT")).check((view, missing) -> {
            if (missing != null) throw missing;
            TextView body = (TextView) view;
            ViewGroup bubble = (ViewGroup) view.getParent();
            assertEquals(Boolean.FALSE, bubble.getTag());
            assertTrue("Long message wraps", body.getLineCount() > 3);
            TextView time = bubble.findViewById(R.id.messaging_timestamp);
            assertNotNull("Known createdAt is presented", time);
            assertFalse(time.getText().toString().isEmpty());
            assertTrue("Readable accessibility status",
                    body.getContentDescription().toString().contains(
                            text(R.string.messaging_state_sent_description)));
            assertTrue("Text stays inside bubble", body.getRight() <= bubble.getWidth());
        });
        host.inspectSurface(surface -> {
            ViewGroup outgoing = bubbleFor(surface, MessagingTestFixtures.HISTORY_ROW);
            ViewGroup incoming = bubbleFor(surface, LONG + " · SENT");
            int[] out = new int[2], in = new int[2];
            outgoing.getLocationOnScreen(out);
            incoming.getLocationOnScreen(in);
            assertTrue("Outgoing lane is right aligned", out[0] > in[0]);
            assertTrue("Bubble stays above composer",
                    in[1] + incoming.getHeight() <= surface.findViewById(
                            R.id.messaging_composer).getTop()
                            + screenY((View) surface.findViewById(R.id.messaging_composer).getParent()));
        });
        onView(withText(text(R.string.messaging_receipt_read))).check(
                androidx.test.espresso.assertion.ViewAssertions.doesNotExist());
        onView(withText(text(R.string.messaging_receipt_delivered))).check(
                androidx.test.espresso.assertion.ViewAssertions.doesNotExist());
        capture("02-chat-detail-long-incoming-and-outgoing");
    }

    @Test public void ui02_keyboardVisibleComposerAndSameIdentityRetry() throws Exception {
        host = MessagingTestHost.launch(MessagingTestFixtures.create());
        openConversation();
        onView(withHint(MessagingTestHost.COMPOSER_HINT)).perform(click(),
                typeTextIntoFocusedView(MessagingTestFixtures.COMPOSED));
        MessagingTestHost.awaitUi("IME visible with unobscured composer and Back", root -> {
            WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(root);
            if (insets == null || !insets.isVisible(WindowInsetsCompat.Type.ime())) return false;
            EditText editor = root.findViewById(R.id.messaging_composer);
            View send = root.findViewById(R.id.messaging_send);
            Rect safe = new Rect(), field = new Rect(), action = new Rect();
            editor.getWindowVisibleDisplayFrame(safe);
            return editor.hasFocus() && editor.getGlobalVisibleRect(field)
                    && send.getGlobalVisibleRect(action) && safe.contains(field) && safe.contains(action);
        });
        onView(withText(MessagingTestHost.BACK)).check(matches(isDisplayed()));
        host.recordComposer("IMP01_IME_VISIBLE");
        capture("03-composer-keyboard-visible");
        host.fixtures.repository.setFailure(FakeConversationRepository.Failure.TIMEOUT);
        onView(withText(MessagingTestHost.SEND)).perform(click());
        onView(withHint(MessagingTestHost.COMPOSER_HINT)).perform(closeSoftKeyboard());
        MessagingTestHost.awaitDisplayed(withText(MessagingTestFixtures.COMPOSED + " · FAILED"));
        String client = host.account().localEntries(host.fixtures.conversation.getId())
                .get(0).getMessage().getClientMessageId();
        onView(withText(text(R.string.messaging_retry_send))).perform(scrollTo());
        onView(withText(text(R.string.messaging_retry_send))).check(
                matches(allOf(isDisplayed(), isEnabled())));
        capture("04-failed-and-retry");
        host.fixtures.repository.setFailure(FakeConversationRepository.Failure.NONE);
        onView(withText(text(R.string.messaging_retry_send))).perform(click());
        MessagingTestHost.awaitDisplayed(withText(MessagingTestFixtures.COMPOSED_ROW));
        assertEquals("Retry retains the frozen clientMessageId", client,
                host.account().localEntries(host.fixtures.conversation.getId())
                        .get(0).getMessage().getClientMessageId());
        MessagingTestHost.assertVisibleCount(withText(MessagingTestFixtures.COMPOSED_ROW), 1);
        host.fixtures.assertSingleReconciledSend();
        onView(withHint(MessagingTestHost.COMPOSER_HINT)).check(matches(withText("")));
        capture("05-retry-reconciled-once");
    }

    @Test public void ui03_loadingEmptyAndRecoverableErrorUseExistingState() throws Exception {
        host = MessagingTestHost.launch(MessagingTestFixtures.presentation(false,
                UnaryOperator.identity()));
        awaitConversation();
        host.fixtures.controlled.holdNextHistory();
        onView(conversation()).perform(click());
        host.fixtures.controlled.awaitHistoryEntered();
        MessagingTestHost.awaitDisplayed(withText(text(R.string.messaging_loading)));
        capture("06-loading-history");
        host.fixtures.controlled.completeHistory();
        MessagingTestHost.awaitDisplayed(withText(text(R.string.messaging_empty_history)));
        capture("07-empty-history");
        host.fixtures.repository.setFailure(FakeConversationRepository.Failure.OFFLINE);
        onView(withText(MessagingTestHost.BACK)).perform(click());
        MessagingTestHost.awaitDisplayed(withText(text(R.string.messaging_error_offline)));
        capture("08-recoverable-list-error");
        host.fixtures.repository.setFailure(FakeConversationRepository.Failure.NONE);
        onView(withText(text(R.string.messaging_retry))).perform(click());
        awaitConversation();
        host.close();
        host = MessagingTestHost.launch(MessagingTestFixtures.presentation(false,
                delegate -> new UiHistory(delegate, true)));
        MessagingTestHost.awaitDisplayed(withText(text(R.string.messaging_empty_list)));
        capture("09-empty-conversation-list");
    }

    private void awaitConversation() { MessagingTestHost.awaitDisplayed(conversation()); }

    private void openConversation() {
        awaitConversation();
        onView(conversation()).perform(click());
        MessagingTestHost.awaitDisplayed(withHint(MessagingTestHost.COMPOSER_HINT));
    }

    private static org.hamcrest.Matcher<View> conversation() {
        return allOf(isAssignableFrom(Button.class), withText(MessagingTestFixtures.PEER));
    }

    private static int target(View view) {
        return view.getResources().getDimensionPixelSize(R.dimen.messaging_touch_target);
    }

    private static int screenY(View view) {
        int[] xy = new int[2];
        view.getLocationOnScreen(xy);
        return xy[1];
    }

    private static ViewGroup bubbleFor(View root, String text) {
        for (View view : androidx.test.espresso.util.TreeIterables.breadthFirstViewTraversal(root)) {
            if (view instanceof TextView && text.contentEquals(((TextView) view).getText())) {
                return (ViewGroup) view.getParent();
            }
        }
        throw new AssertionError("Missing message bubble: " + text);
    }

    private static String text(int resource) {
        return InstrumentationRegistry.getInstrumentation().getTargetContext().getString(resource);
    }

    /** Gate E opts in; regular regression runs do not create or overwrite screenshot evidence. */
    private static void capture(String name) throws Exception {
        if (!"true".equals(InstrumentationRegistry.getArguments().getString("imp01Capture"))) return;
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        Bitmap bitmap = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        assertNotNull("Actual emulator screenshot", bitmap);
        File directory = InstrumentationRegistry.getInstrumentation().getTargetContext()
                .getExternalFilesDir("imp01-visual");
        assertNotNull(directory);
        File file = new File(directory, name + ".png");
        try (FileOutputStream output = new FileOutputStream(file)) {
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output));
        } finally {
            bitmap.recycle();
        }
        android.util.Log.i("MessagingImp01Visual", "Screenshot: " + file.getAbsolutePath());
    }

    /** Minimal synthetic incoming/empty projections for rendering; all mutations use the real fake. */
    private static final class UiHistory implements ConversationRepository {
        private final ConversationRepository delegate;
        private final boolean emptyList;
        UiHistory(ConversationRepository delegate, boolean emptyList) {
            this.delegate = delegate;
            this.emptyList = emptyList;
        }
        @Override public CompletionStage<List<Conversation>> listConversations() {
            return delegate.listConversations().thenApply(rows -> emptyList
                    ? Collections.emptyList() : rows);
        }
        @Override public CompletionStage<Conversation> openDirect(String peer) {
            return delegate.openDirect(peer);
        }
        @Override public CompletionStage<List<ServerMessage>> listMessages(String id) {
            return delegate.listMessages(id).thenApply(rows -> {
                List<ServerMessage> result = new ArrayList<>(rows);
                Message incoming = new Message("66666666-6666-4666-8666-666666666666", id,
                        MessagingTestFixtures.PEER, MessageType.TEXT, LONG, null, null,
                        "2026-11-27T01:05:00Z", null, null);
                result.add(new ServerMessage(incoming, new ServerAcknowledgement(
                        incoming.getClientMessageId(), "77777777-7777-4777-8777-777777777777", 2),
                        MessageStatus.NONE));
                return result;
            });
        }
        @Override public CompletionStage<MessagePage> loadMessages(String id, Long before, int limit) {
            return delegate.loadMessages(id, before, limit);
        }
        @Override public CompletionStage<List<OutboxEntry>> listOutbox(String id) {
            return delegate.listOutbox(id);
        }
        @Override public CompletionStage<OutboxEntry> send(Message message) {
            return delegate.send(message);
        }
        @Override public CompletionStage<Void> markRead(String id, ServerMessage through) {
            return delegate.markRead(id, through);
        }
    }
}
