package com.nexa.chat.feature.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;
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

import com.nexa.chat.MainActivity;
import com.nexa.chat.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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
