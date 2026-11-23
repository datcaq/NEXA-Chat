package com.nexa.chat.feature.auth.session;

/**
 * Pure Java destination policy for the proposed CORE-01 shell.
 * This class neither validates credentials nor performs navigation.
 */
public final class SessionAccessPolicy {
    public enum Destination {
        CHECK_SESSION,
        AUTH,
        HOME,
        RETRY_SESSION
    }

    private SessionAccessPolicy() {}

    /**
     * Unknown or unverifiable state keeps protected destinations inaccessible.
     * HOME requires an AUTHENTICATED state supplied by the authoritative session implementation.
     */
    public static Destination destinationFor(SessionRepository.State state) {
        if (state == null) {
            return Destination.CHECK_SESSION;
        }

        switch (state) {
            case SIGNED_OUT:
                return Destination.AUTH;
            case AUTHENTICATED:
                return Destination.HOME;
            case UNAVAILABLE:
                return Destination.RETRY_SESSION;
            case CHECKING:
            default:
                return Destination.CHECK_SESSION;
        }
    }
}
