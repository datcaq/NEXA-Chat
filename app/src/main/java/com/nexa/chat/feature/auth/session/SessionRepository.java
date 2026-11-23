package com.nexa.chat.feature.auth.session;

/**
 * Proposed TV1 session boundary for CORE-01, pending integration review.
 *
 * <p>An implementation owns credential protection and authoritative session checks. UI previews
 * must never publish AUTHENTICATED. This contract exposes no credentials or authentication setter.
 * It has no dependency on an OTP provider, token format, or API route.
 */
public interface SessionRepository {
    enum State {
        CHECKING,
        SIGNED_OUT,
        AUTHENTICATED,
        UNAVAILABLE
    }

    /** Returns CHECKING until restoration finishes; restored local data alone is not authentication. */
    State currentState();

    /**
     * Registers an observer and delivers the current state followed by ordered changes.
     * Implementations must serialize state changes and callback delivery. Android adapters arrange
     * main-thread delivery. Closing the subscription stops subsequent delivery.
     */
    Subscription observe(Observer observer);

    /** Asynchronously restores and validates the stored session, publishing CHECKING first. */
    void restore();

    /**
     * Asynchronously revalidates or refreshes the session using the approved authentication flow.
     * Rejection publishes SIGNED_OUT; an inability to verify publishes UNAVAILABLE.
     * Concurrent requests must share the same pending refresh operation.
     */
    void refresh();

    /**
     * Immediately invalidates local access and publishes SIGNED_OUT, then attempts server logout.
     * A late restore/refresh callback must not restore access after logout or an account change.
     * Server revocation and offline retry policy require the team's approved backend contract.
     *
     * <p>The application coordinator also closes authenticated screens, cancels account work,
     * disconnects realtime, and clears account dependencies and private cached state.
     */
    void logout();

    interface Observer {
        void onStateChanged(State state);
    }

    interface Subscription extends AutoCloseable {
        /** Idempotent cancellation; no checked exception is required. */
        @Override
        void close();
    }
}
