package com.nexa.chat.feature.profile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.res.Configuration;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.material.textfield.TextInputLayout;
import com.nexa.chat.MainActivity;
import com.nexa.chat.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayDeque;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(AndroidJUnit4.class)
public final class ProfileFragmentTest {
    @Test
    public void validatesNameAndRetainsDraftAcrossRecreationUntilCancel() {
        try (ActivityScenario<MainActivity> host = launchProfile(new ProfileFragment())) {
            host.onActivity(activity -> {
                assertFalse(name(activity).isEnabled());
                click(activity, R.id.profile_edit);
                name(activity).setText(" ");
                click(activity, R.id.profile_save);
                TextInputLayout nameField = activity.findViewById(R.id.profile_name_field);
                assertEquals(activity.getString(R.string.profile_name_required),
                        nameField.getError().toString());
                name(activity).setText("An Nguyễn");
                bio(activity).setText("Xin chào");
            });
            host.recreate();
            host.onActivity(activity -> {
                assertTrue(name(activity).isEnabled());
                assertEquals("An Nguyễn", name(activity).getText().toString());
                assertEquals("Xin chào", bio(activity).getText().toString());
                click(activity, R.id.profile_cancel);
                assertEquals(activity.getString(R.string.profile_mock_name),
                        name(activity).getText().toString());
                assertEquals("", bio(activity).getText().toString());
                assertFalse(name(activity).isEnabled());
            });
        }
    }

    @Test
    public void mockSaveShowsLoadingSurvivesRecreationAndCanRetryAfterError() {
        Queue<Runnable> pending = new ArrayDeque<>();
        AtomicBoolean success = new AtomicBoolean();
        ViewModelProvider.Factory factory = new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> type) {
                return type.cast(new ProfileViewModel(pending::add, success::get));
            }
        };
        try (ActivityScenario<MainActivity> host = launchProfile(new ProfileFragment(factory))) {
            host.onActivity(activity -> {
                click(activity, R.id.profile_edit);
                name(activity).setText("An");
                bio(activity).setText("Mock bio");
                click(activity, R.id.profile_save);
                assertSaving(activity);
            });
            host.recreate();
            host.onActivity(activity -> {
                assertSaving(activity);
                assertEquals(1, pending.size());
                pending.remove().run();
                assertFalse(activity.findViewById(R.id.profile_progress).isShown());
                assertTrue(name(activity).isEnabled());
                assertEquals("An", name(activity).getText().toString());
                assertEquals("Mock bio", bio(activity).getText().toString());
                assertFeedback(activity, R.string.profile_save_error);

                success.set(true);
                click(activity, R.id.profile_save);
                assertSaving(activity);
                pending.remove().run();
                assertFeedback(activity, R.string.profile_saved_mock);
                assertFalse(name(activity).isEnabled());
                assertEquals("An", name(activity).getText().toString());
                assertEquals("Mock bio", bio(activity).getText().toString());
            });
        }
    }

    @Test
    public void profileResourcesSupportVietnameseAndEnglishWhileLauncherStaysOnAuth() {
        try (ActivityScenario<MainActivity> host = ActivityScenario.launch(MainActivity.class)) {
            host.onActivity(activity -> {
                assertNotNull(activity.findViewById(R.id.auth_title));
                assertNull(activity.findViewById(R.id.profile_screen));
                Configuration config = new Configuration(activity.getResources().getConfiguration());
                config.setLocale(Locale.ENGLISH);
                assertEquals("Profile", activity.createConfigurationContext(config)
                        .getString(R.string.profile_title));
                config.setLocale(Locale.forLanguageTag("vi"));
                assertEquals("Hồ sơ", activity.createConfigurationContext(config)
                        .getString(R.string.profile_title));
                assertEquals("Vui lòng nhập tên hiển thị.", activity.createConfigurationContext(config)
                        .getString(R.string.profile_name_required));
            });
        }
    }

    private static ActivityScenario<MainActivity> launchProfile(ProfileFragment fragment) {
        ActivityScenario<MainActivity> host = ActivityScenario.launch(MainActivity.class);
        try {
            host.onActivity(activity -> activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_content, fragment).commitNow());
            return host;
        } catch (RuntimeException | Error failure) {
            host.close();
            throw failure;
        }
    }

    private static void assertSaving(MainActivity activity) {
        assertEquals(View.VISIBLE, activity.findViewById(R.id.profile_progress).getVisibility());
        assertFalse(activity.findViewById(R.id.profile_save).isEnabled());
        assertFalse(activity.findViewById(R.id.profile_cancel).isEnabled());
        assertFalse(name(activity).isEnabled());
        assertFeedback(activity, R.string.profile_saving);
    }

    private static void assertFeedback(MainActivity activity, int message) {
        TextView feedback = activity.findViewById(R.id.profile_feedback);
        assertEquals(View.VISIBLE, feedback.getVisibility());
        assertEquals(activity.getString(message), feedback.getText().toString());
    }

    private static void click(MainActivity activity, int id) {
        activity.findViewById(id).performClick();
    }

    private static EditText name(MainActivity activity) {
        return activity.findViewById(R.id.profile_name);
    }

    private static EditText bio(MainActivity activity) {
        return activity.findViewById(R.id.profile_bio);
    }
}
