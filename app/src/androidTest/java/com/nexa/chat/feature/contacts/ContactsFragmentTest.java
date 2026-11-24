package com.nexa.chat.feature.contacts;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.app.Instrumentation;
import android.app.LocaleManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.LocaleList;
import android.view.Choreographer;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.material.textfield.TextInputLayout;
import com.nexa.chat.MainActivity;
import com.nexa.chat.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public final class ContactsFragmentTest {
    private static final String JAPAN_LABEL = "Japan (+81)";
    private static final String JAPANESE_PHONE = "090-1234-5678";
    private static final String INTERNATIONAL_PHONE = "+12025550123";
    private static final String PUBLIC_TEST_NAME = "Alex Example";

    private LocaleListCompat originalLocales;
    private int originalNightMode;

    @Before
    public void recordConfiguration() {
        originalNightMode = AppCompatDelegate.getDefaultNightMode();
    }

    @After
    public void restoreConfiguration() {
        instrumentation().runOnMainSync(() -> {
            AppCompatDelegate.setDefaultNightMode(originalNightMode);
            if (originalLocales != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    LocaleManager locales = instrumentation().getTargetContext()
                            .getSystemService(LocaleManager.class);
                    locales.setApplicationLocales(LocaleList.forLanguageTags(originalLocales.toLanguageTags()));
                } else {
                    AppCompatDelegate.setApplicationLocales(originalLocales);
                }
            }
        });
    }

    @Test
    public void invalidInputKeepsInitialStateAndValidInputReportsUnavailable() {
        try (ActivityScenario<MainActivity> host = launchContacts()) {
            host.onActivity(activity -> {
                assertSearchStatus(activity, R.string.contacts_search_initial);
                activity.findViewById(R.id.contacts_search).performClick();
                assertPhoneError(activity, R.string.contacts_phone_required);
                assertSearchStatus(activity, R.string.contacts_search_initial);

                phone(activity).setText("+12");
                activity.findViewById(R.id.contacts_search).performClick();
                assertPhoneError(activity, R.string.contacts_phone_invalid);
                assertSearchStatus(activity, R.string.contacts_search_initial);
                assertEquals(View.GONE, activity.findViewById(R.id.contacts_result).getVisibility());

                phone(activity).setText(INTERNATIONAL_PHONE);
                activity.findViewById(R.id.contacts_search).performClick();
                assertNull(phoneField(activity).getError());
                assertSearchStatus(activity, R.string.contacts_search_unavailable);
            });
        }
    }

    @Test
    public void keyboardInputAndRegionSurviveRecreationWithoutSubmitting() {
        try (ActivityScenario<MainActivity> host = launchContacts()) {
            selectJapan();
            host.onActivity(activity -> phone(activity).setText(JAPANESE_PHONE));
            host.recreate();
            host.onActivity(activity -> {
                assertEquals(JAPAN_LABEL, text(activity, R.id.contacts_region));
                assertEquals(JAPANESE_PHONE, phone(activity).getText().toString());
                assertSearchStatus(activity, R.string.contacts_search_initial);
                activity.findViewById(R.id.contacts_search).performClick();
                assertSearchStatus(activity, R.string.contacts_search_unavailable);
            });
            host.recreate();
            host.onActivity(activity -> {
                assertEquals(JAPAN_LABEL, text(activity, R.id.contacts_region));
                assertSearchStatus(activity, R.string.contacts_search_unavailable);
            });
        }
    }

    @Test
    public void changingLanguageDoesNotChangeSelectedCallingRegion() {
        try (ActivityScenario<MainActivity> host = launchContacts()) {
            selectJapan();
            host.onActivity(activity -> phone(activity).setText(JAPANESE_PHONE));
            changeLanguage(host, "vi");

            onView(withId(R.id.contacts_title)).check(matches(withText("Danh bạ")));
            host.onActivity(activity -> {
                assertTrue(text(activity, R.id.contacts_region).contains("+81"));
                assertEquals(JAPANESE_PHONE, phone(activity).getText().toString());
                assertSearchStatus(activity, R.string.contacts_search_initial);
                activity.findViewById(R.id.contacts_search).performClick();
                assertNull(phoneField(activity).getError());
                assertSearchStatus(activity, R.string.contacts_search_unavailable);
            });
        }
    }

    @Test
    public void presentationRendersLoadingEmptyErrorAndPublicResultFromTestFixture() {
        try (ActivityScenario<MainActivity> host = launchContacts()) {
            host.onActivity(activity -> {
                ContactsFragment contacts = (ContactsFragment) activity.getSupportFragmentManager()
                        .findFragmentById(R.id.main_content);
                assertNotNull(contacts);
                contacts.renderSearch(ContactsFragment.SearchState.LOADING, null);
                assertEquals(View.VISIBLE, activity.findViewById(R.id.contacts_progress).getVisibility());
                assertFalse(activity.findViewById(R.id.contacts_search).isEnabled());

                contacts.renderSearch(ContactsFragment.SearchState.EMPTY, null);
                assertSearchStatus(activity, R.string.contacts_search_empty);
                contacts.renderSearch(ContactsFragment.SearchState.ERROR, null);
                assertSearchStatus(activity, R.string.contacts_search_error);
                assertTrue(activity.findViewById(R.id.contacts_search).isEnabled());

                contacts.renderSearch(ContactsFragment.SearchState.RESULT, PUBLIC_TEST_NAME);
                assertEquals(PUBLIC_TEST_NAME, text(activity, R.id.contacts_result_name));
                assertFalse(activity.findViewById(R.id.contacts_send_invitation).isEnabled());
                assertNull(activity.findViewById(R.id.home_navigation));
            });
        }
    }

    @Test
    public void invitationsRemainReachableByScrolling() {
        try (ActivityScenario<MainActivity> host = launchContacts()) {
            onView(withId(R.id.contacts_sent)).perform(scrollTo(), click());
            onView(withId(R.id.contacts_invitation_status))
                    .perform(scrollTo()).check(matches(withText(R.string.contacts_sent_unavailable)));
            onView(withId(R.id.contacts_received)).perform(scrollTo(), click());
            onView(withId(R.id.contacts_invitation_status))
                    .perform(scrollTo()).check(matches(withText(R.string.contacts_received_unavailable)));
        }
    }

    @Test
    public void productionLauncherStillShowsAuth() {
        try (ActivityScenario<MainActivity> host = ActivityScenario.launch(MainActivity.class)) {
            originalLocales = AppCompatDelegate.getApplicationLocales();
            host.onActivity(activity -> {
                assertNotNull(activity.findViewById(R.id.auth_title));
                assertNull(activity.findViewById(R.id.contacts_screen));
                assertNull(activity.findViewById(R.id.home_navigation));
            });
        }
    }

    @Test
    public void captureUiEvidence() throws IOException {
        try (ActivityScenario<MainActivity> host = launchContacts()) {
            onView(withId(R.id.contacts_title)).check(matches(withText("Contacts")));
            capture(host, "01-contacts-en");
            onView(withId(R.id.contacts_region)).perform(click());
            onView(withText(R.string.contacts_region_title)).check(matches(isDisplayed()));
            capture(host, "02-region-picker");
            pressBack();

            host.onActivity(activity -> {
                phone(activity).setText("+12");
                activity.findViewById(R.id.contacts_search).performClick();
                assertPhoneError(activity, R.string.contacts_phone_invalid);
            });
            capture(host, "03-invalid-phone");
        }
        try (ActivityScenario<MainActivity> host = launchContacts()) {
            host.onActivity(activity -> {
                phone(activity).setText(INTERNATIONAL_PHONE);
                activity.findViewById(R.id.contacts_search).performClick();
                assertSearchStatus(activity, R.string.contacts_search_unavailable);
            });
            capture(host, "04-unavailable");
        }
        try (ActivityScenario<MainActivity> host = launchContacts()) {
            changeLanguage(host, "vi");
            onView(withId(R.id.contacts_title)).check(matches(withText("Danh bạ")));
            capture(host, "05-contacts-vi");

            host.onActivity(activity -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES));
            host.recreate();
            host.onActivity(activity -> {
                configureInsets(activity);
                assertEquals(Configuration.UI_MODE_NIGHT_YES,
                        activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
            });
            capture(host, "06-dark");
        }
    }

    private ActivityScenario<MainActivity> launchContacts() {
        ActivityScenario<MainActivity> host = ActivityScenario.launch(MainActivity.class);
        host.onActivity(activity -> {
            if (originalLocales == null) {
                originalLocales = AppCompatDelegate.getApplicationLocales();
            }
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"));
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        });
        host.onActivity(activity -> {
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_content, new ContactsFragment())
                    .commitNow();
            configureInsets(activity);
        });
        return host;
    }

    private static void changeLanguage(ActivityScenario<MainActivity> host, String language) {
        host.onActivity(activity ->
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language)));
        host.recreate();
        host.onActivity(ContactsFragmentTest::configureInsets);
    }

    private static void configureInsets(MainActivity activity) {
        View content = activity.findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(content, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(content);

        boolean dark = (activity.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        View contacts = activity.findViewById(R.id.contacts_screen);
        content.setBackgroundColor(activity.getColor(R.color.home_background));
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(activity.getWindow(), contacts);
        controller.setAppearanceLightStatusBars(!dark);
        controller.setAppearanceLightNavigationBars(!dark);
    }

    private static void selectJapan() {
        onView(withId(R.id.contacts_region)).perform(click());
        onData(equalTo(JAPAN_LABEL)).perform(click());
    }

    private static void assertSearchStatus(MainActivity activity, int message) {
        assertEquals(activity.getString(message), text(activity, R.id.contacts_search_status));
    }

    private static void assertPhoneError(MainActivity activity, int message) {
        assertEquals(activity.getString(message), phoneField(activity).getError());
    }

    private static TextInputLayout phoneField(MainActivity activity) {
        return activity.findViewById(R.id.contacts_phone_field);
    }

    private static EditText phone(MainActivity activity) {
        return activity.findViewById(R.id.contacts_phone);
    }

    private static String text(MainActivity activity, int id) {
        TextView view = activity.findViewById(id);
        return view.getText().toString();
    }

    private static Instrumentation instrumentation() {
        return InstrumentationRegistry.getInstrumentation();
    }

    private static void capture(ActivityScenario<MainActivity> host, String filename)
            throws IOException {
        // Wait for the graphics pipeline to present the latest view state before taking evidence.
        CountDownLatch frames = new CountDownLatch(1);
        host.onActivity(activity -> {
            configureInsets(activity);
            Choreographer choreographer = Choreographer.getInstance();
            choreographer.postFrameCallback(new Choreographer.FrameCallback() {
                private long firstFrame;

                @Override
                public void doFrame(long frameTimeNanos) {
                    if (firstFrame == 0) {
                        firstFrame = frameTimeNanos;
                    }
                    // Material's hint and error captions animate after the view text changes.
                    if (frameTimeNanos - firstFrame >= TimeUnit.MILLISECONDS.toNanos(300)) {
                        frames.countDown();
                    } else {
                        choreographer.postFrameCallback(this);
                    }
                }
            });
        });
        try {
            assertTrue("Contacts frame was not presented", frames.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for Contacts rendering", interrupted);
        }
        Instrumentation instrumentation = instrumentation();
        instrumentation.waitForIdleSync();
        File directory = instrumentation.getTargetContext().getExternalFilesDir("contact-01a");
        if (directory == null || (!directory.isDirectory() && !directory.mkdirs())) {
            throw new IOException("Contacts screenshot directory unavailable");
        }

        Bitmap screenshot = instrumentation.getUiAutomation().takeScreenshot();
        assertNotNull(screenshot);
        try (FileOutputStream file = new FileOutputStream(new File(directory, filename + ".png"))) {
            assertTrue(screenshot.compress(Bitmap.CompressFormat.PNG, 100, file));
        } finally {
            screenshot.recycle();
        }
    }
}
