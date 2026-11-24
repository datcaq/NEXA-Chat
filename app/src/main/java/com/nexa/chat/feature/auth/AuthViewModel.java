package com.nexa.chat.feature.auth;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.MainThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

public final class AuthViewModel extends ViewModel {
    enum Screen { SIGN_IN, REGISTER, OTP }
    enum Feedback { NONE, SIGN_IN_PENDING, OTP_PREVIEW, VERIFICATION_PENDING }

    static final class State {
        final Screen screen;
        final boolean loading;
        final Feedback feedback;
        final AuthFailure failure;
        final RegistrationChallenge challenge;

        State(Screen screen, boolean loading, Feedback feedback) {
            this(screen, loading, feedback, null, null);
        }

        State(Screen screen, boolean loading, Feedback feedback, AuthFailure failure,
              RegistrationChallenge challenge) {
            this.screen = screen;
            this.loading = loading;
            this.feedback = feedback;
            this.failure = failure;
            this.challenge = challenge;
        }
    }

    private final MutableLiveData<State> state =
            new MutableLiveData<>(new State(Screen.SIGN_IN, false, Feedback.NONE));
    private final Thread owner = Thread.currentThread();
    private final AuthRepository repository;
    private final Executor ownerDispatcher;
    private Handler previewHandler;
    private Attempt pending;
    private boolean cleared;

    public AuthViewModel() {
        this(null);
    }

    @MainThread
    AuthViewModel(AuthRepository repository) {
        this(repository, mainDispatcher());
    }

    // The dispatcher delivers on the owner thread and may reject only after owner shutdown.
    @MainThread
    AuthViewModel(AuthRepository repository, Executor ownerDispatcher) {
        this.repository = repository;
        this.ownerDispatcher = Objects.requireNonNull(ownerDispatcher);
    }

    private static Executor mainDispatcher() {
        Handler handler = new Handler(Looper.getMainLooper());
        return action -> {
            if (!handler.post(action)) {
                throw new RejectedExecutionException("Auth owner stopped");
            }
        };
    }

    LiveData<State> state() {
        return state;
    }

    @MainThread
    Screen screen() {
        return state.getValue().screen;
    }

    @MainThread
    boolean isRegistrationPending() {
        return pending != null;
    }

    @MainThread
    void show(Screen screen) {
        if (!cleared && (!state.getValue().loading || pending != null)) {
            replaceState(new State(screen, false, Feedback.NONE));
        }
    }

    @MainThread
    void phoneChanged() {
        State current = state.getValue();
        if (!cleared && current.screen == Screen.REGISTER
                && (pending != null || current.failure != null)) {
            replaceState(new State(Screen.REGISTER, false, Feedback.NONE));
        }
    }

    /** Returns false for an invalid phone. OTP preview and duplicate submits are handled in place. */
    @MainThread
    boolean submit(String phone, String region) {
        State current = state.getValue();
        if (cleared || current.loading || current.challenge != null) {
            return true;
        }
        String phoneE164 = null;
        if (current.screen != Screen.OTP) {
            phoneE164 = phone == null ? null : PhoneNumberValidator.toE164(phone, region);
            if (phoneE164 == null) {
                return false;
            }
        }

        if (current.screen == Screen.REGISTER && repository != null) {
            startRegistration(phoneE164);
        } else {
            preview(current.screen);
        }
        return true;
    }

    private void startRegistration(String phoneE164) {
        Attempt attempt = new Attempt();
        pending = attempt;
        state.setValue(new State(Screen.REGISTER, true, Feedback.NONE));
        if (pending != attempt) {
            return;
        }

        AuthRepository.Request request = repository.startRegistrationChallenge(
                phoneE164, new AuthRepository.ChallengeCallback() {
                    @Override
                    public void onChallenge(RegistrationChallenge challenge) {
                        deliver(attempt, challenge, null);
                    }

                    @Override
                    public void onFailure(AuthFailure failure) {
                        deliver(attempt, null, failure);
                    }
                });

        // Inline callbacks or an observer may already have replaced the attempt.
        if (pending == attempt) {
            attempt.request = request;
        } else {
            request.cancel();
        }
    }

    private void deliver(Attempt attempt, RegistrationChallenge challenge, AuthFailure failure) {
        if (Thread.currentThread() == owner) {
            finish(attempt, challenge, failure);
            return;
        }
        try {
            ownerDispatcher.execute(() -> finish(attempt, challenge, failure));
        } catch (RejectedExecutionException ownerStopped) {
            // There is no owner left to notify. Never mutate LiveData on the worker.
        }
    }

    @MainThread
    private void finish(Attempt attempt, RegistrationChallenge challenge, AuthFailure failure) {
        if (pending != attempt) {
            return;
        }
        pending = null;
        Screen next = challenge == null ? Screen.REGISTER : Screen.OTP;
        state.setValue(new State(next, false, Feedback.NONE, failure, challenge));
    }

    private void replaceState(State next) {
        Attempt previous = pending;
        pending = null;
        state.setValue(next);
        cancel(previous);
    }

    private static void cancel(Attempt attempt) {
        if (attempt != null && attempt.request != null) {
            attempt.request.cancel();
        }
    }

    // Launcher preview. It never calls a repository or creates a challenge or session.
    private void preview(Screen screen) {
        state.setValue(new State(screen, true, Feedback.NONE));
        if (cleared) {
            return;
        }
        if (previewHandler == null) {
            previewHandler = new Handler(Looper.getMainLooper());
        }
        previewHandler.postDelayed(() -> {
            switch (screen) {
                case SIGN_IN:
                    state.setValue(new State(Screen.SIGN_IN, false, Feedback.SIGN_IN_PENDING));
                    break;
                case REGISTER:
                    state.setValue(new State(Screen.OTP, false, Feedback.OTP_PREVIEW));
                    break;
                case OTP:
                    state.setValue(new State(Screen.OTP, false, Feedback.VERIFICATION_PENDING));
                    break;
            }
        }, 350);
    }

    @MainThread
    @Override
    protected void onCleared() {
        cleared = true;
        Attempt previous = pending;
        pending = null;
        cancel(previous);
        if (previewHandler != null) {
            previewHandler.removeCallbacksAndMessages(null);
        }
    }

    /** Its object identity is local to this ViewModel and survives View recreation. */
    private static final class Attempt {
        AuthRepository.Request request;
    }
}
