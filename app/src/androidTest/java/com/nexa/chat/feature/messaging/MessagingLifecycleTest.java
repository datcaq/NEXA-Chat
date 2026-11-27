package com.nexa.chat.feature.messaging;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasFocus;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.*;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.lifecycle.Lifecycle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.espresso.util.TreeIterables;

import com.nexa.chat.feature.messaging.model.OutboxState;
import com.nexa.chat.feature.messaging.model.ServerMessage;
import com.nexa.chat.feature.messaging.ui.MessagingController;
import com.nexa.chat.feature.messaging.ui.MessagingExecution;
import com.nexa.chat.feature.messaging.ui.MessagingPresentation;
import com.nexa.chat.feature.messaging.ui.MessagingRouter;
import com.nexa.chat.feature.messaging.ui.MessagingView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.MultipleFailureException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** Independent retained account harness; no App Shell, Session or network integration claim. */
@RunWith(AndroidJUnit4.class)
public final class MessagingLifecycleTest {
    private static final String DRAFT = "IMP02B retained draft";
    private MessagingTestHost host;
    private MessagingTestHost accountB;

    @Before public void launchAccount() throws Exception {
        host = MessagingTestHost.launch(MessagingTestFixtures.controlled(
                MessagingTestFixtures.USER, MessagingTestFixtures.EPOCH,
                MessagingTestFixtures.PEER, MessagingTestFixtures.HISTORY));
    }

    @After public void closeEveryRuntimeEvenAfterFailure() throws Exception {
        List<Throwable> failures = new ArrayList<>();
        try { if (host != null) host.close(); } catch (Throwable failure) { failures.add(failure); }
        try { if (accountB != null) accountB.close(); } catch (Throwable failure) { failures.add(failure); }
        MultipleFailureException.assertEmpty(failures);
    }

    @Test public void lc01_routeAndHistorySurviveActivityRecreation() {
        openConversation(host);
        MessagingRouter.Route route = host.account().getRouter().currentRoute();
        MessagingView old = surface(host);
        host.recreateActivity();
        assertDetail(host, "");
        assertSame("Retained immutable route", route, host.account().getRouter().currentRoute());
        assertNotSame(old, surface(host));
        onMain(() -> assertFalse(old.isAttachedToWindow()));
    }

    @Test public void lc02_draftRestoresWithoutUserEditOrSend() throws Exception {
        openConversation(host);
        typeDraft(DRAFT);
        assertEquals(DRAFT, host.account().draft(host.fixtures.conversation.getId()));
        long revision = host.account().draftRevision(host.fixtures.conversation.getId());
        assertTrue(revision > 0);
        host.recreateActivity();
        assertDetail(host, DRAFT);
        awaitIdle(host.accountLease());
        assertEquals("Programmatic restoration is not a genuine edit", revision,
                host.account().draftRevision(host.fixtures.conversation.getId()));
        assertEquals(DRAFT, host.account().draft(host.fixtures.conversation.getId()));
        assertTrue(host.account().localEntries(host.fixtures.conversation.getId()).isEmpty());
        host.fixtures.assertSeedOnly();
    }

    @Test public void lc03_sameAccountRetainsControllerEpochAndLease() {
        openConversation(host);
        MessagingController controller = host.account();
        MessagingExecution runtime = host.runtime();
        MessagingExecution.AccountLease lease = host.accountLease();
        assertEquals(1, runtime.liveLeases());
        host.recreateActivity();
        assertDetail(host, "");
        assertSame(controller, host.account());
        assertSame(runtime, host.runtime());
        assertSame(lease, host.accountLease());
        assertEquals(MessagingTestFixtures.USER, controller.getCurrentUserId());
        assertEquals(MessagingTestFixtures.EPOCH, controller.getSessionEpoch());
        assertEquals(controller.getCurrentUserId(), lease.userId);
        assertEquals(controller.getSessionEpoch(), lease.epoch);
        assertTrue(controller.isActive());
        assertTrue(controller.getRouter().isActive());
        assertEquals("No replacement lease", 1, runtime.liveLeases());
    }

    @Test public void lc04_detachRejectsOldHistoryBindingAfterReplacement() throws Exception {
        openConversation(host);
        typeDraft(DRAFT);
        onView(withText(MessagingTestHost.BACK)).perform(click());
        awaitConversation(host);
        host.fixtures.controlled.holdNextHistory();
        onView(conversationButton(host)).perform(click());
        host.fixtures.controlled.awaitHistoryEntered();
        MessagingView old = surface(host);
        MessagingRouter.Route route = host.account().getRouter().currentRoute();
        long revision = host.account().draftRevision(host.fixtures.conversation.getId());
        host.detachSurface();
        onMain(() -> {
            assertFalse(old.isAttachedToWindow());
            assertEquals("Detached view bindings clear their children", 0, old.getChildCount());
        });
        assertTrue(host.account().isActive());
        // Public eligibility foundation, using actual attachment evidence; no private View reflection.
        MessagingPresentation detached = new MessagingPresentation(host.account().getRouter());
        detached.setHostState(true, true);
        detached.setAttached(false);
        assertFalse(detached.isActive());
        assertFalse(detached.canType(host.fixtures.conversation.getId(), true));

        host.remountSurface();
        assertDetail(host, DRAFT);
        String replacement = surfaceText(host);
        host.fixtures.controlled.completeHistory();
        awaitIdle(host.accountLease());
        assertEquals("Old result cannot alter replacement presentation", replacement, surfaceText(host));
        assertSame(route, host.account().getRouter().currentRoute());
        assertEquals(revision, host.account().draftRevision(host.fixtures.conversation.getId()));
        onMain(() -> assertEquals("Stale binding cannot repopulate old View", 0, old.getChildCount()));
        host.fixtures.assertSeedOnly();
    }

    @Test public void lc05_selectedAndResumedGuardsAreIndependent() throws Exception {
        openConversation(host);
        typeDraft(DRAFT);
        long revision = host.account().draftRevision(host.fixtures.conversation.getId());
        ServerMessage seed = host.fixtures.repository.listMessages(host.fixtures.conversation.getId())
                .toCompletableFuture().get(5, TimeUnit.SECONDS).get(0);
        // Exercise the public READ/TYPING eligibility contract, not transport emissions.
        MessagingPresentation eligibility = new MessagingPresentation(host.account().getRouter());
        eligibility.setAttached(true);
        eligibility.setHostState(true, true);
        assertTrue(eligibility.isActive());
        assertTrue(eligibility.canType(host.fixtures.conversation.getId(), true));
        assertFalse(eligibility.canType(host.fixtures.conversation.getId(), false));
        assertTrue(eligibility.canRead(seed, true));
        assertFalse(eligibility.canRead(seed, false));
        assertFalse(eligibility.canType("different-conversation", true));
        MessagingPresentation.Token active = eligibility.capture();

        host.setHostState(false, true);
        eligibility.setHostState(false, true);
        assertInactiveGuards(eligibility, seed, active);
        typeDraft(" hidden");
        onView(withText(MessagingTestHost.SEND)).perform(click());
        assertUnchangedDraft(revision);
        host.setHostState(true, true);
        eligibility.setHostState(true, true);
        assertDetail(host, DRAFT);
        assertTrue(eligibility.isActive());

        active = eligibility.capture();
        host.setHostState(true, false);
        eligibility.setHostState(true, false);
        assertInactiveGuards(eligibility, seed, active);
        typeDraft(" paused");
        onView(withText(MessagingTestHost.SEND)).perform(click());
        assertUnchangedDraft(revision);
        host.setHostState(true, true);
        eligibility.setHostState(true, true);
        assertDetail(host, DRAFT);
        assertTrue(eligibility.canType(host.fixtures.conversation.getId(), true));
        assertTrue(eligibility.canRead(seed, true));
        assertEquals(revision, host.account().draftRevision(host.fixtures.conversation.getId()));
        host.fixtures.assertSeedOnly();
    }

    @Test public void lc06_hiddenAckReconcilesAccountWithoutUpdatingInactiveView() throws Exception {
        openConversation(host);
        host.fixtures.controlled.holdNextSend();
        typeDraft(MessagingTestFixtures.COMPOSED);
        onView(withText(MessagingTestHost.SEND)).perform(click());
        host.fixtures.controlled.awaitSendEntered();
        MessagingTestHost.awaitDisplayed(withText(MessagingTestFixtures.COMPOSED + " · SENDING"));
        host.setHostState(false, true);
        String frozen = surfaceText(host);
        host.fixtures.controlled.completeSend();
        awaitReconciled(host);
        assertEquals("Inactive presentation receives no ACK rendering", frozen, surfaceText(host));
        onView(withText(MessagingTestFixtures.COMPOSED_ROW)).check(doesNotExist());
        onView(withHint(MessagingTestHost.COMPOSER_HINT))
                .check(matches(withText(MessagingTestFixtures.COMPOSED)));
        assertEquals("", host.account().draft(host.fixtures.conversation.getId()));
        host.setHostState(true, true);
        assertDetail(host, "");
        MessagingTestHost.awaitDisplayed(withText(MessagingTestFixtures.COMPOSED_ROW));
        MessagingTestHost.assertVisibleCount(withText(MessagingTestFixtures.COMPOSED_ROW), 1);
        host.fixtures.assertSingleReconciledSend();
    }

    @Test public void lc07_invalidatedAccountCannotLeakLateSendIntoAccountB() throws Exception {
        openConversation(host);
        host.fixtures.controlled.holdNextSend();
        typeDraft(MessagingTestFixtures.COMPOSED);
        onView(withText(MessagingTestHost.SEND)).perform(click());
        host.fixtures.controlled.awaitSendEntered();
        MessagingController old = host.account();
        MessagingExecution.AccountLease oldLease = host.accountLease();
        assertTrue(old.isSending(host.fixtures.conversation.getId()));
        host.invalidateAccount();
        host.closeScenario();
        assertFalse(old.isActive());
        assertFalse(old.getRouter().isActive());
        assertNull(old.getRouter().currentRoute().getConversationId());
        assertEquals("", old.draft(host.fixtures.conversation.getId()));
        assertTrue(old.localEntries(host.fixtures.conversation.getId()).isEmpty());

        UUID userB = UUID.fromString("44444444-4444-4444-8444-444444444444");
        UUID epochB = UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb");
        accountB = MessagingTestHost.launch(MessagingTestFixtures.controlled(userB, epochB,
                "55555555-5555-4555-8555-555555555555", "IMP02B account B history"));
        openConversation(accountB);
        typeDraft("Account B private draft");
        MessagingRouter.Route routeB = accountB.account().getRouter().currentRoute();
        long revisionB = accountB.account().draftRevision(accountB.fixtures.conversation.getId());
        String visibleB = surfaceText(accountB);
        host.fixtures.controlled.completeSend();
        awaitIdle(oldLease);
        assertEquals(visibleB, surfaceText(accountB));
        assertSame(routeB, accountB.account().getRouter().currentRoute());
        assertEquals(userB, accountB.account().getCurrentUserId());
        assertEquals(epochB, accountB.account().getSessionEpoch());
        assertEquals(revisionB, accountB.account().draftRevision(accountB.fixtures.conversation.getId()));
        assertEquals("Account B private draft",
                accountB.account().draft(accountB.fixtures.conversation.getId()));
        assertFalse(old.isActive());
        assertNull(old.getRouter().currentRoute().getConversationId());
        assertEquals("", old.draft(host.fixtures.conversation.getId()));
        assertTrue(old.localEntries(host.fixtures.conversation.getId()).isEmpty());
        onView(withText(MessagingTestFixtures.HISTORY_ROW)).check(doesNotExist());
        onView(withText(MessagingTestFixtures.COMPOSED_ROW)).check(doesNotExist());
        accountB.fixtures.assertSeedOnly();
    }

    @Test public void lc08_cleanupDrainsPendingSourcesAndPhysicallyTerminatesWorkers() throws Exception {
        openConversation(host);
        host.fixtures.controlled.holdNextSend();
        typeDraft(DRAFT);
        onView(withText(MessagingTestHost.SEND)).perform(click());
        host.fixtures.controlled.awaitSendEntered();
        MessagingView view = surface(host);
        MessagingController account = host.account();
        MessagingExecution runtime = host.runtime();
        MessagingExecution.AccountLease lease = host.accountLease();
        host.close(); // Invalidates first, then releases the controlled pending source.
        assertTrue(host.cleanupVerified());
        assertFalse(account.isActive());
        assertEquals("", account.draft(host.fixtures.conversation.getId()));
        assertFalse(account.getRouter().isActive());
        assertEquals(Lifecycle.State.DESTROYED, host.scenarioState());
        onMain(() -> assertFalse(view.isAttachedToWindow()));
        assertEquals(0, runtime.liveLeases());
        assertEquals(0, lease.callerReservations());
        assertEquals(0, lease.operationReservations());
        assertEquals(0, lease.sourceStages());
        assertEquals(0, lease.settlementMailboxSize());
        // awaitTermination checks isAlive() for every managed thread and dispatcher termination.
        assertTrue(runtime.awaitTermination(5, TimeUnit.SECONDS));
    }

    private void assertUnchangedDraft(long revision) {
        assertEquals(DRAFT, host.account().draft(host.fixtures.conversation.getId()));
        assertEquals(revision, host.account().draftRevision(host.fixtures.conversation.getId()));
        assertFalse(host.account().isSending(host.fixtures.conversation.getId()));
        assertTrue(host.account().localEntries(host.fixtures.conversation.getId()).isEmpty());
    }

    private static void assertInactiveGuards(MessagingPresentation eligibility, ServerMessage seed,
                                            MessagingPresentation.Token oldToken) {
        assertFalse(eligibility.isActive());
        assertFalse(eligibility.canRead(seed, true));
        assertFalse(eligibility.canType(seed.getMessage().getConversationId(), true));
        assertFalse(eligibility.accepts(oldToken));
    }

    private static void typeDraft(String text) {
        onView(withHint(MessagingTestHost.COMPOSER_HINT)).perform(click());
        onView(withHint(MessagingTestHost.COMPOSER_HINT)).check(matches(hasFocus()));
        onView(withHint(MessagingTestHost.COMPOSER_HINT))
                .perform(typeTextIntoFocusedView(text), closeSoftKeyboard());
    }

    private static void awaitConversation(MessagingTestHost host) {
        MessagingTestHost.awaitDisplayed(conversationButton(host));
    }

    private static void openConversation(MessagingTestHost host) {
        awaitConversation(host);
        onView(conversationButton(host)).perform(click());
        assertDetail(host, "");
    }

    private static org.hamcrest.Matcher<View> conversationButton(MessagingTestHost host) {
        return allOf(isAssignableFrom(Button.class), withText(host.fixtures.peer));
    }

    private static void assertDetail(MessagingTestHost host, String draft) {
        MessagingTestHost.awaitDisplayed(withText(host.fixtures.historyRow));
        onView(withText(host.fixtures.peer)).check(matches(isDisplayed()));
        onView(withHint(MessagingTestHost.COMPOSER_HINT)).check(matches(withText(draft)));
        assertEquals(host.fixtures.conversation.getId(),
                host.account().getRouter().currentRoute().getConversationId());
        assertEquals(MessagingRouter.Screen.CHAT_DETAIL,
                host.account().getRouter().currentRoute().getScreen());
    }

    private static MessagingView surface(MessagingTestHost host) {
        MessagingView[] result = new MessagingView[1];
        host.inspectSurface(view -> result[0] = view);
        return result[0];
    }

    private static String surfaceText(MessagingTestHost host) {
        StringBuilder result = new StringBuilder();
        host.inspectSurface(surface -> {
            for (View view : TreeIterables.breadthFirstViewTraversal(surface)) {
                if (view instanceof TextView) {
                    result.append(view.getClass().getName()).append(':')
                            .append(((TextView) view).getText()).append(':')
                            .append(view.isEnabled()).append('\n');
                }
            }
        });
        return result.toString();
    }

    private static void awaitReconciled(MessagingTestHost host) {
        MessagingTestHost.awaitUi("matching ACK reconciled by retained account", root -> {
            List<com.nexa.chat.feature.messaging.model.OutboxEntry> rows =
                    host.account().localEntries(host.fixtures.conversation.getId());
            return rows.size() == 1 && rows.get(0).getState() == OutboxState.SENT
                    && !host.account().isSending(host.fixtures.conversation.getId());
        });
        awaitIdle(host.accountLease());
    }

    private static void awaitIdle(MessagingExecution.AccountLease lease) {
        MessagingTestHost.awaitUi("account reservations drained", root ->
                lease.callerReservations() == 0 && lease.operationReservations() == 0
                        && lease.sourceStages() == 0 && lease.settlementMailboxSize() == 0);
    }

    private static void onMain(Runnable assertion) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(assertion);
    }
}
