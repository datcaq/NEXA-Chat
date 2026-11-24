package com.nexa.chat.feature.auth;

/** Initiates registration verification before any authenticated account scope exists. */
public interface AuthRepository {
    /**
     * Starts one registration challenge request and returns without waiting for network work.
     *
     * <p>phoneE164 must already be in canonical E.164 form and pass
     * {@link PhoneNumberValidator} validation. A null phone or callback throws
     * {@link NullPointerException}. A non-canonical or invalid number throws
     * {@link IllegalArgumentException}. Invalid arguments are rejected synchronously,
     * without starting work or invoking a callback.
     *
     * <p>Each request admits at most one terminal callback. The callback may run inline
     * before this method returns or on an execution thread. Implementations invoke it outside
     * their state lock. Both callback methods require non-null results. Callbacks must
     * return promptly and must not throw. UI scheduling and checking whether the auth
     * attempt is still current are the caller's responsibility.
     */
    Request startRegistrationChallenge(String phoneE164, ChallengeCallback callback);

    interface Request {
        /**
         * Idempotent and thread-safe. Cancellation and terminal callback admission compete
         * for the same pending request. Their order is decided by which first atomically
         * claims that request's pending callback. Cancellation that wins suppresses delivery,
         * including queued work. A callback already admitted before cancellation may
         * start or finish afterwards, so the caller must still check its auth attempt.
         *
         * <p>Does not wait for callbacks or network work. Physical cancellation may be
         * asynchronous and does not imply server rollback.
         */
        void cancel();
    }

    interface ChallengeCallback {
        /** A challenge was issued. This result grants no authentication or session. */
        void onChallenge(RegistrationChallenge challenge);

        void onFailure(AuthFailure failure);
    }
}
