package com.nexa.chat.feature.messaging;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static androidx.test.espresso.matcher.ViewMatchers.hasFocus;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.widget.Button;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.nexa.chat.feature.messaging.ui.MessagingView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** UI-01..UI-05 exercise the independent fake-backed surface, not production integration. */
@RunWith(AndroidJUnit4.class)
public final class MessagingUiSmokeTest {
    private MessagingTestHost host;

    @Before public void mountFreshAccount() throws Exception {
        host = MessagingTestHost.launch();
    }

    @After public void alwaysCloseAccountAndRuntime() throws Exception {
        if (host != null) host.close();
    }

    @Test public void ui01_mountMessagingView() {
        MessagingTestHost.awaitDisplayed(withText(MessagingTestHost.LIST_TITLE));
        onView(isAssignableFrom(MessagingView.class)).check(matches(isDisplayed()));
        onView(withText(MessagingTestHost.LIST_TITLE)).check(matches(isDisplayed()));
    }

    @Test public void ui02_displayConversationFixture() {
        awaitConversation();
        onView(conversationButton()).check(matches(allOf(isDisplayed(), isEnabled())));
        MessagingTestHost.assertVisibleCount(conversationButton(), 1);
    }

    @Test public void ui03_openConversationThroughUiAndDisplayHistory() {
        openConversationThroughUi();
        onView(withText(MessagingTestFixtures.PEER)).check(matches(isDisplayed()));
        onView(withText(MessagingTestFixtures.HISTORY_ROW)).check(matches(isDisplayed()));
        onView(withHint(MessagingTestHost.COMPOSER_HINT)).check(matches(isDisplayed()));
    }

    @Test public void ui04_sendThroughComposerAndReconcileExactlyOnce() throws Exception {
        openConversationThroughUi();
        onView(withHint(MessagingTestHost.COMPOSER_HINT)).check(matches(withText("")));
        onView(withHint(MessagingTestHost.COMPOSER_HINT)).perform(click());
        onView(withHint(MessagingTestHost.COMPOSER_HINT)).check(matches(hasFocus()));
        host.recordComposer("UI04_BEFORE_FOCUSED_INPUT");
        onView(withHint(MessagingTestHost.COMPOSER_HINT))
                .perform(typeTextIntoFocusedView(MessagingTestFixtures.COMPOSED));
        host.recordComposer("UI04_AFTER_FOCUSED_INPUT");
        onView(withHint(MessagingTestHost.COMPOSER_HINT)).perform(closeSoftKeyboard());
        onView(withHint(MessagingTestHost.COMPOSER_HINT))
                .check(matches(withText(MessagingTestFixtures.COMPOSED)));
        onView(withText(MessagingTestHost.SEND)).perform(click());

        MessagingTestHost.awaitDisplayed(withText(MessagingTestFixtures.COMPOSED_ROW));
        onView(withHint(MessagingTestHost.COMPOSER_HINT)).check(matches(withText("")));
        onView(withText(MessagingTestHost.SEND)).check(matches(isEnabled()));
        MessagingTestHost.assertVisibleCount(withText(MessagingTestFixtures.COMPOSED_ROW), 1);
        host.fixtures.assertSingleReconciledSend();

        // Reload only via real Back + conversation clicks: server history and outbox overlap.
        onView(withText(MessagingTestHost.BACK)).perform(click());
        openConversationThroughUi();
        MessagingTestHost.awaitDisplayed(withText(MessagingTestFixtures.COMPOSED_ROW));
        MessagingTestHost.assertVisibleCount(withText(MessagingTestFixtures.HISTORY_ROW), 1);
        MessagingTestHost.assertVisibleCount(withText(MessagingTestFixtures.COMPOSED_ROW), 1);
        host.fixtures.assertSingleReconciledSend();
    }

    @Test public void ui05_internalBackRestoresConversationList() {
        openConversationThroughUi();
        // Validate the default target, then run the original real Espresso click.
        onView(withText(MessagingTestHost.BACK)).perform(host.checkedBackClick(click()));
        MessagingTestHost.awaitDisplayed(withText(MessagingTestHost.LIST_TITLE));
        awaitConversation();
        onView(conversationButton()).check(matches(isDisplayed()));
        onView(withHint(MessagingTestHost.COMPOSER_HINT)).check(doesNotExist());
        onView(withText(MessagingTestFixtures.HISTORY_ROW)).check(doesNotExist());
        onView(withText(MessagingTestHost.BACK)).check(doesNotExist());
        host.recordRestoredList();
    }

    private void awaitConversation() {
        MessagingTestHost.awaitDisplayed(conversationButton());
    }

    private void openConversationThroughUi() {
        awaitConversation();
        onView(conversationButton()).perform(click());
        MessagingTestHost.awaitDisplayed(withText(MessagingTestFixtures.HISTORY_ROW));
        onView(withText(MessagingTestHost.BACK)).check(matches(isDisplayed()));
    }

    private static org.hamcrest.Matcher<android.view.View> conversationButton() {
        return allOf(isAssignableFrom(Button.class), withText(MessagingTestFixtures.PEER));
    }
}
