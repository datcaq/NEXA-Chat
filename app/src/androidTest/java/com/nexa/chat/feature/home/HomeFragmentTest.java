package com.nexa.chat.feature.home;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertTrue;

import android.app.Instrumentation;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.ViewMatchers.Visibility;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.nexa.chat.MainActivity;
import com.nexa.chat.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class HomeFragmentTest {
    @Test
    public void allTabsShowTheirOwnUnavailablePanel() throws IOException {
        try (ActivityScenario<MainActivity> host = launchHost()) {
            assertTab(R.id.home_panel_messages, R.string.home_messages);
            capture("01-messages");
            onView(withId(R.id.home_tab_contacts)).perform(click());
            assertTab(R.id.home_panel_contacts, R.string.home_contacts);
            capture("02-contacts");
            onView(withId(R.id.home_tab_groups)).perform(click());
            assertTab(R.id.home_panel_groups, R.string.home_groups);
            capture("03-groups");
            onView(withId(R.id.home_tab_profile)).perform(click());
            assertTab(R.id.home_panel_profile, R.string.home_profile);
            capture("04-profile");
        }
    }

    @Test
    public void selectedTabSurvivesActivityRecreation() {
        try (ActivityScenario<MainActivity> host = launchHost()) {
            onView(withId(R.id.home_tab_groups)).perform(click());
            host.recreate();
            host.onActivity(HomeFragmentTest::configureHost);
            assertTab(R.id.home_panel_groups, R.string.home_groups);
            onView(withId(R.id.home_panel_messages))
                    .check(matches(withEffectiveVisibility(Visibility.GONE)));
        }
    }

    // The test process mounts the fragment. The production launcher does not reference Home.
    private static ActivityScenario<MainActivity> launchHost() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        scenario.onActivity(activity -> {
            configureHost(activity);
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, new HomeFragment(), "home-test")
                    .commitNow();
        });
        return scenario;
    }

    private static void configureHost(MainActivity activity) {
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        View content = activity.findViewById(android.R.id.content);
        content.setBackgroundColor(activity.getColor(R.color.home_background));
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
        ViewCompat.setOnApplyWindowInsetsListener(content, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(content);

        boolean isDark = (activity.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(activity.getWindow(), content);
        controller.setAppearanceLightStatusBars(!isDark);
        controller.setAppearanceLightNavigationBars(!isDark);
    }

    private static void assertTab(int panelId, int titleId) {
        onView(withId(panelId)).check(matches(isDisplayed()));
        onView(withId(R.id.home_tab_title))
                .check(matches(allOf(withText(titleId), isCompletelyDisplayed())));
        onView(allOf(withText(R.string.home_unavailable), isDescendantOfA(withId(panelId))))
                .check(matches(isDisplayed()));
    }

    private static void capture(String name) throws IOException {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        instrumentation.waitForIdleSync();
        File directory = instrumentation.getTargetContext().getExternalFilesDir("core-02");
        if (directory == null || (!directory.isDirectory() && !directory.mkdirs())) {
            throw new IOException("CORE-02 screenshot directory unavailable");
        }

        Bitmap screenshot = instrumentation.getUiAutomation().takeScreenshot();
        try (FileOutputStream stream = new FileOutputStream(new File(directory, name + ".png"))) {
            assertTrue(screenshot.compress(Bitmap.CompressFormat.PNG, 100, stream));
        } finally {
            screenshot.recycle();
        }
    }
}
