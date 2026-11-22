package com.nexa.chat.feature.auth;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public final class AuthViewModel extends ViewModel {
    enum Screen { SIGN_IN, REGISTER, OTP }
    enum Feedback { NONE, SIGN_IN_PENDING, OTP_PREVIEW, VERIFICATION_PENDING }

    static final class State {
        final Screen screen;
        final boolean loading;
        final Feedback feedback;

        State(Screen screen, boolean loading, Feedback feedback) {
            this.screen = screen;
            this.loading = loading;
            this.feedback = feedback;
        }
    }

    private final MutableLiveData<State> state =
            new MutableLiveData<>(new State(Screen.SIGN_IN, false, Feedback.NONE));
    private final Handler handler = new Handler(Looper.getMainLooper());

    LiveData<State> state() {
        return state;
    }

    Screen screen() {
        return state.getValue().screen;
    }

    void show(Screen screen) {
        if (!state.getValue().loading) {
            state.setValue(new State(screen, false, Feedback.NONE));
        }
    }

    // UI-only preview. No account, challenge, SMS, or session is created.
    void preview() {
        State current = state.getValue();
        if (current.loading) {
            return;
        }

        state.setValue(new State(current.screen, true, Feedback.NONE));
        handler.postDelayed(() -> {
            switch (current.screen) {
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

    @Override
    protected void onCleared() {
        handler.removeCallbacksAndMessages(null);
    }
}
