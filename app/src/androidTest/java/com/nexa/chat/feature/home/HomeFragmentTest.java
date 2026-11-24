package com.nexa.chat.feature.home;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Instrumentation;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.ParcelFileDescriptor;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.matcher.ViewMatchers.Visibility;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.nexa.chat.MainActivity;
import com.nexa.chat.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class HomeFragmentTest {
    private static final String HOME_TEST_TAG = "home-test";

    @Test
    public void allTabsShowTheirOwnUnavailablePanel() throws IOException {
        try (ActivityScenario<MainActivity> host = launchHost()) {
            assertTab(R.id.home_panel_messages, R.string.home_messages);
            host.onActivity(HomeFragmentTest::assertNavigationClearsBottomGestureArea);
            capture("01-messages");
            tapTab(R.id.home_tab_contacts);
            assertTab(R.id.home_panel_contacts, R.string.home_contacts);
            capture("02-contacts");
            tapTab(R.id.home_tab_groups);
            assertTab(R.id.home_panel_groups, R.string.home_groups);
            capture("03-groups");
            tapTab(R.id.home_tab_profile);
            assertTab(R.id.home_panel_profile, R.string.home_profile);
            capture("04-profile");
            tapTab(R.id.home_tab_contacts);
            assertTab(R.id.home_panel_contacts, R.string.home_contacts);
            tapTab(R.id.home_tab_messages);
            assertTab(R.id.home_panel_messages, R.string.home_messages);
        }
    }

    @Test
    public void selectedTabSurvivesActivityRecreation() throws IOException {
        try (ActivityScenario<MainActivity> host = launchHost()) {
            tapTab(R.id.home_tab_groups);
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
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, new HomeFragment(), HOME_TEST_TAG)
                    .commitNow();
            configureHost(activity);
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
        View home = activity.getSupportFragmentManager()
                .findFragmentByTag(HOME_TEST_TAG).requireView();
        ViewCompat.setOnApplyWindowInsetsListener(home, (view, windowInsets) -> {
            applyHomeInsets(view, windowInsets);
            return WindowInsetsCompat.CONSUMED;
        });
        WindowInsetsCompat currentInsets = ViewCompat.getRootWindowInsets(home);
        if (currentInsets != null) {
            applyHomeInsets(home, currentInsets);
        }
        ViewCompat.requestApplyInsets(home);

        boolean isDark = (activity.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(activity.getWindow(), content);
        controller.setAppearanceLightStatusBars(!isDark);
        controller.setAppearanceLightNavigationBars(!isDark);
    }

    private static void applyHomeInsets(View home, WindowInsetsCompat windowInsets) {
        // Keep real tab taps outside the system's gesture area in this edge-to-edge host.
        Insets safeArea = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        | WindowInsetsCompat.Type.displayCutout()
                        | WindowInsetsCompat.Type.mandatorySystemGestures());
        home.setPadding(safeArea.left, safeArea.top, safeArea.right, safeArea.bottom);
    }

    private static void assertTab(int panelId, int titleId) {
        onView(withId(panelId)).check(matches(isDisplayed()));
        onView(withId(R.id.home_tab_title))
                .check(matches(allOf(withText(titleId), isCompletelyDisplayed())));
        onView(allOf(withText(R.string.home_unavailable), isDescendantOfA(withId(panelId))))
                .check(matches(isDisplayed()));
    }

    private static void assertNavigationClearsBottomGestureArea(MainActivity activity) {
        View navigation = activity.findViewById(R.id.home_navigation);
        WindowInsetsCompat rootInsets = ViewCompat.getRootWindowInsets(navigation);
        assertNotNull("Home must have received window insets before tapping its tabs", rootInsets);
        int gestureBottom = rootInsets.getInsets(
                WindowInsetsCompat.Type.mandatorySystemGestures()).bottom;
        int[] position = new int[2];
        navigation.getLocationInWindow(position);
        int navigationBottom = position[1] + navigation.getHeight();
        int gestureStart = activity.getWindow().getDecorView().getHeight() - gestureBottom;
        assertTrue("Home navigation bottom " + navigationBottom
                + " overlaps the system gesture area starting at " + gestureStart,
                navigationBottom <= gestureStart);
    }

    private static void tapTab(int menuItemId) throws IOException {
        Point center = new Point();
        onView(withId(menuItemId))
                .check(matches(allOf(isCompletelyDisplayed(), isClickable(), isEnabled())))
                .check((tab, missingView) -> {
                    if (missingView != null) {
                        throw missingView;
                    }
                    float[] coordinates = GeneralLocation.CENTER.calculateCoordinates(tab);
                    center.set(Math.round(coordinates[0]), Math.round(coordinates[1]));
                });

        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        // Send DOWN and UP in one system tap command. A delayed Espresso UP became a long press.
        String command = "input touchscreen tap " + center.x + " " + center.y;
        try (InputStream output = new ParcelFileDescriptor.AutoCloseInputStream(
                instrumentation.getUiAutomation().executeShellCommand(command))) {
            // Reading EOF waits for the command to finish dispatching the tap.
            assertEquals("Unexpected Android input command output", -1, output.read());
        }
        instrumentation.waitForIdleSync();
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
