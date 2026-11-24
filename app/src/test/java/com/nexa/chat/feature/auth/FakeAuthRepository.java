package com.nexa.chat.feature.auth;

import java.util.Objects;

/** Test fixture with independently controllable requests and optional inline success. */
final class FakeAuthRepository implements AuthRepository {
    private final RegistrationChallenge inlineChallenge;

    FakeAuthRepository() {
        inlineChallenge = null;
    }

    FakeAuthRepository(RegistrationChallenge inlineChallenge) {
        this.inlineChallenge = Objects.requireNonNull(inlineChallenge, "inlineChallenge");
    }

    @Override
    public PendingRequest startRegistrationChallenge(String phoneE164, ChallengeCallback callback) {
        Objects.requireNonNull(phoneE164, "phoneE164");
        Objects.requireNonNull(callback, "callback");
        if (!phoneE164.equals(PhoneNumberValidator.toE164(phoneE164, null))) {
            throw new IllegalArgumentException("Phone number must be valid canonical E.164");
        }

        PendingRequest request = new PendingRequest(callback);
        if (inlineChallenge != null) {
            request.succeed(inlineChallenge);
        }
        return request;
    }

    static final class PendingRequest implements Request {
        private ChallengeCallback pendingCallback;

        private PendingRequest(ChallengeCallback callback) {
            pendingCallback = callback;
        }

        boolean succeed(RegistrationChallenge challenge) {
            Objects.requireNonNull(challenge, "challenge");
            ChallengeCallback callback = takePendingCallback();
            if (callback == null) {
                return false;
            }
            callback.onChallenge(challenge);
            return true;
        }

        boolean fail(AuthFailure failure) {
            Objects.requireNonNull(failure, "failure");
            ChallengeCallback callback = takePendingCallback();
            if (callback == null) {
                return false;
            }
            callback.onFailure(failure);
            return true;
        }

        @Override
        public void cancel() {
            takePendingCallback();
        }

        // The first removal under this monitor linearizes cancellation or callback admission.
        // The winner can release the monitor before invoking external code.
        private synchronized ChallengeCallback takePendingCallback() {
            ChallengeCallback callback = pendingCallback;
            pendingCallback = null;
            return callback;
        }
    }
}
