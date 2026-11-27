package com.nexa.chat.feature.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.WindowManager;
import android.view.View;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.material.textfield.TextInputLayout;
import com.nexa.chat.MainActivity;
import com.nexa.chat.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 29)
public final class AuthFragmentTest {
    private static final String PHONE = "+12025550123";
    private final TestRepository repository = new TestRepository();
    private Application application;
    private final Application.ActivityLifecycleCallbacks factoryInstaller =
            new TestActivityCallbacks() {
                @Override
                public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle state) {
                    if (activity instanceof MainActivity) {
                        ((MainActivity) activity).getSupportFragmentManager().setFragmentFactory(
                                new FragmentFactory() {
                                    @NonNull
                                    @Override
                                    public Fragment instantiate(@NonNull ClassLoader classLoader,
                                                                @NonNull String className) {
                                        if (AuthFragment.class.getName().equals(className)) {
                                            return new AuthFragment(modelFactory());
                                        }
                                        return super.instantiate(classLoader, className);
                                    }
                                });
                    }
                }
            };

    @Before
    public void installTestFactory() {
        application = (Application) InstrumentationRegistry.getInstrumentation()
                .getTargetContext().getApplicationContext();
        application.registerActivityLifecycleCallbacks(factoryInstaller);
    }

    @After
    public void removeTestFactory() {
        application.unregisterActivityLifecycleCallbacks(factoryInstaller);
    }

    @Test
    public void recreationKeepsPendingRequestAndOnlyNewViewRendersCompletion() {
        AtomicReference<TextView> oldTitle = new AtomicReference<>();
        AtomicReference<EditText> oldPhone = new AtomicReference<>();
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                beginRegistration(activity);
                oldTitle.set(activity.findViewById(R.id.auth_title));
                oldPhone.set(activity.findViewById(R.id.auth_phone));
                assertEquals(1, repository.starts);
                assertEquals(View.VISIBLE, activity.findViewById(R.id.auth_progress).getVisibility());
            });

            scenario.recreate();

            scenario.onActivity(activity -> {
                TextView currentTitle = activity.findViewById(R.id.auth_title);
                assertNotSame(oldTitle.get(), currentTitle);
                assertEquals(1, repository.starts);
                assertEquals(0, repository.cancellations);
                assertEquals(View.VISIBLE, activity.findViewById(R.id.auth_progress).getVisibility());
                oldPhone.get().setText("+12025550124");
                assertEquals(0, repository.cancellations);

                repository.callback.onChallenge(new RegistrationChallenge("opaque-test-reference"));

                assertEquals(activity.getString(R.string.auth_otp_title), currentTitle.getText().toString());
                assertEquals(activity.getString(R.string.auth_register), oldTitle.get().getText().toString());
                assertEquals(View.VISIBLE, activity.findViewById(R.id.auth_otp_field).getVisibility());
                assertFalse(activity.findViewById(R.id.auth_submit).isEnabled());
                assertNull(activity.findViewById(R.id.home_navigation));
                assertEquals(1, repository.starts);
            });
        }
    }

    @Test
    public void eachRepositoryFailureRendersItsMappedMessageAndEnablesRetry() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                beginRegistration(activity);
                assertFailure(activity, AuthFailure.UNAVAILABLE, R.string.auth_challenge_unavailable);
                activity.findViewById(R.id.auth_submit).performClick();
                assertFailure(activity, AuthFailure.RATE_LIMITED, R.string.auth_challenge_rate_limited);
                activity.findViewById(R.id.auth_submit).performClick();
                assertFailure(activity, AuthFailure.REJECTED, R.string.auth_challenge_rejected);
                assertEquals(3, repository.starts);
            });
        }
    }

    @Test
    public void closingHostClearsViewModelAndCancelsPendingRequest() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        try {
            scenario.onActivity(this::beginRegistration);
        } finally {
            scenario.close();
        }
        assertEquals(1, repository.cancellations);
    }

    @Test
    public void signInValidatesFieldsAndCompletesPreviewWithoutSession() throws InterruptedException {
        CountDownLatch previewComplete = new CountDownLatch(1);
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                View submit = activity.findViewById(R.id.auth_submit);
                EditText phone = activity.findViewById(R.id.auth_phone);
                EditText password = activity.findViewById(R.id.auth_password);
                submit.performClick();
                TextInputLayout phoneField = activity.findViewById(R.id.auth_phone_field);
                assertEquals(activity.getString(R.string.auth_error_phone_required), phoneField.getError());
                phone.setText(PHONE);
                submit.performClick();
                TextInputLayout passwordField = activity.findViewById(R.id.auth_password_field);
                assertEquals(activity.getString(R.string.auth_error_password_required),
                        passwordField.getError());

                password.setText(Character.toString('x'));
                phone.setText("+12");
                submit.performClick();
                assertEquals(activity.getString(R.string.auth_error_phone_invalid), phoneField.getError());
                phone.setText(PHONE);

                Fragment fragment = activity.getSupportFragmentManager()
                        .findFragmentById(R.id.main_content);
                new ViewModelProvider(fragment).get(AuthViewModel.class).state().observe(activity, state -> {
                    if (state.feedback == AuthViewModel.Feedback.SIGN_IN_PENDING) {
                        previewComplete.countDown();
                    }
                });
                submit.performClick();
                assertFalse(submit.isEnabled());
                assertEquals(View.VISIBLE, activity.findViewById(R.id.auth_progress).getVisibility());
                assertTrue(password.length() == 0);
            });
            assertTrue("Sign-in preview did not finish", previewComplete.await(10, TimeUnit.SECONDS));
            scenario.onActivity(activity -> {
                TextView feedback = activity.findViewById(R.id.auth_feedback);
                assertEquals(activity.getString(R.string.auth_sign_in_pending), feedback.getText());
                assertTrue(activity.findViewById(R.id.auth_submit).isEnabled());
                assertNull(activity.findViewById(R.id.home_navigation));
                assertEquals(0, repository.starts);
            });
        }
    }

    @Test
    public void passwordToggleDoesNotPersistTextOrUnmaskedState() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                EditText password = activity.findViewById(R.id.auth_password);
                View toggle = passwordToggle(activity);
                assertTrue(password.getTransformationMethod() instanceof PasswordTransformationMethod);
                assertFalse(password.isSaveEnabled());
                password.setText(Character.toString('x'));
                assertTrue((activity.getWindow().getAttributes().flags
                        & WindowManager.LayoutParams.FLAG_SECURE) != 0);
                toggle.performClick();
                assertFalse(password.getTransformationMethod() instanceof PasswordTransformationMethod);
                toggle.performClick();
                assertTrue(password.getTransformationMethod() instanceof PasswordTransformationMethod);
                toggle.performClick();
            });
            scenario.recreate();
            scenario.onActivity(activity -> {
                EditText password = activity.findViewById(R.id.auth_password);
                assertTrue(password.length() == 0);
                assertPasswordMasked(password, passwordToggle(activity));
                password.setText(Character.toString('x'));
                View switchScreen = activity.findViewById(R.id.auth_switch);
                switchScreen.performClick();
                assertEquals(View.GONE, activity.findViewById(R.id.auth_password_field).getVisibility());
                switchScreen.performClick();
                assertTrue(password.length() == 0);
            });
        }
    }

    @Test
    public void passwordIsMaskedAfterSwitchAndMockSubmit() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                EditText password = activity.findViewById(R.id.auth_password);
                View toggle = passwordToggle(activity);
                password.setText(Character.toString('x'));
                toggle.performClick();
                assertTrue(((Checkable) toggle).isChecked());
                View switchScreen = activity.findViewById(R.id.auth_switch);
                switchScreen.performClick();
                switchScreen.performClick();
                password.setText(Character.toString('x'));
                assertPasswordMasked(password, toggle);

                toggle.performClick();
                EditText phone = activity.findViewById(R.id.auth_phone);
                phone.setText(PHONE);
                activity.findViewById(R.id.auth_submit).performClick();
                assertTrue(password.length() == 0);
                assertPasswordMasked(password, toggle);
                assertFalse(isWindowSecure(activity));
            });
        }
    }

    @Test
    public void passwordCaptureProtectionPreservesHostFlagAndCleansUpOwnedFlag() {
        for (boolean hostSecure : new boolean[] { false, true }) {
            try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
                scenario.onActivity(activity -> {
                    if (hostSecure) {
                        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
                    }
                    EditText password = activity.findViewById(R.id.auth_password);
                    password.setText(Character.toString('x'));
                    assertTrue(isWindowSecure(activity));
                    password.setText(null);
                    assertEquals(hostSecure, isWindowSecure(activity));
                    password.setText(Character.toString('x'));
                    Fragment auth = activity.getSupportFragmentManager()
                            .findFragmentById(R.id.main_content);
                    activity.getSupportFragmentManager().beginTransaction().remove(auth).commitNow();
                    assertEquals(hostSecure, isWindowSecure(activity));
                });
            }
        }
    }

    private static View passwordToggle(MainActivity activity) {
        return activity.findViewById(R.id.auth_password_field)
                .findViewById(com.google.android.material.R.id.text_input_end_icon);
    }

    private static void assertPasswordMasked(EditText password, View toggle) {
        assertTrue("Password must be masked by default",
                password.getTransformationMethod() instanceof PasswordTransformationMethod);
        assertFalse("Visibility toggle must be unchecked", ((Checkable) toggle).isChecked());
    }

    private static boolean isWindowSecure(MainActivity activity) {
        return (activity.getWindow().getAttributes().flags
                & WindowManager.LayoutParams.FLAG_SECURE) != 0;
    }

    @Test
    public void signInPasswordResourcesAreLocalized() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                Configuration config = new Configuration(activity.getResources().getConfiguration());
                config.setLocale(Locale.ENGLISH);
                assertEquals("Password",
                        activity.createConfigurationContext(config).getString(R.string.auth_password));
                config.setLocale(Locale.forLanguageTag("vi"));
                assertEquals("Mật khẩu",
                        activity.createConfigurationContext(config).getString(R.string.auth_password));
                assertEquals("Vui lòng nhập mật khẩu.", activity.createConfigurationContext(config)
                        .getString(R.string.auth_error_password_required));
            });
        }
    }

    private ViewModelProvider.Factory modelFactory() {
        return new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return modelClass.cast(new AuthViewModel(repository));
            }
        };
    }

    private void beginRegistration(MainActivity activity) {
        activity.findViewById(R.id.auth_switch).performClick();
        EditText phone = activity.findViewById(R.id.auth_phone);
        phone.setText(PHONE);
        activity.findViewById(R.id.auth_submit).performClick();
    }

    private void assertFailure(MainActivity activity, AuthFailure failure, int message) {
        repository.callback.onFailure(failure);
        TextView feedback = activity.findViewById(R.id.auth_feedback);
        assertEquals(View.VISIBLE, feedback.getVisibility());
        assertEquals(activity.getString(message), feedback.getText().toString());
        assertEquals(View.GONE, activity.findViewById(R.id.auth_progress).getVisibility());
        assertTrue(activity.findViewById(R.id.auth_submit).isEnabled());
    }

    private static final class TestRepository implements AuthRepository {
        ChallengeCallback callback;
        int starts;
        volatile int cancellations;

        @Override
        public Request startRegistrationChallenge(String phoneE164, ChallengeCallback callback) {
            this.callback = callback;
            starts++;
            return () -> cancellations++;
        }
    }

    private abstract static class TestActivityCallbacks implements Application.ActivityLifecycleCallbacks {
        @Override public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle state) {}
        @Override public void onActivityStarted(@NonNull Activity activity) {}
        @Override public void onActivityResumed(@NonNull Activity activity) {}
        @Override public void onActivityPaused(@NonNull Activity activity) {}
        @Override public void onActivityStopped(@NonNull Activity activity) {}
        @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle state) {}
        @Override public void onActivityDestroyed(@NonNull Activity activity) {}
    }
}
