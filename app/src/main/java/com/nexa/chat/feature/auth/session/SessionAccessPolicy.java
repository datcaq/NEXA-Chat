package com.nexa.chat.feature.auth.session;

/** Pure Java destination proposal for CORE-01. The session adapter supplies authoritative snapshots. */
public final class SessionAccessPolicy {
    public enum Destination {
        CHECK_SESSION,
        AUTH,
        HOME,
        RETRY_SESSION
    }

    private SessionAccessPolicy() {}

    /** This policy selects a destination only; the future host must observe the latest snapshot. */
    public static Destination destinationFor(SessionSnapshot snapshot) {
        if (snapshot == null) {
            return Destination.CHECK_SESSION;
        }

        switch (snapshot.getState()) {
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
