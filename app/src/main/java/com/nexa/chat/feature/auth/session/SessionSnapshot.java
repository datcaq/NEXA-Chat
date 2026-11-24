package com.nexa.chat.feature.auth.session;

import java.util.Objects;
import java.util.UUID;

/**
 * Immutable proposed session projection for CORE-01 review.
 * Creating this value does not authenticate a user. Only an authoritative session implementation
 * may publish AUTHENTICATED after validation of server-issued identity and credentials.
 */
public final class SessionSnapshot {
    private final SessionRepository.State state;
    private final UUID currentUserId;
    private final UUID sessionEpoch;

    /**
     * UUID types prevent malformed string identifiers. Every non-authenticated state hides identity.
     * sessionEpoch is a local scope identifier and contains no server session or token information.
     */
    public SessionSnapshot(SessionRepository.State state, UUID currentUserId, UUID sessionEpoch) {
        this.state = Objects.requireNonNull(state, "state");
        this.sessionEpoch = Objects.requireNonNull(sessionEpoch, "sessionEpoch");
        if ((state == SessionRepository.State.AUTHENTICATED) != (currentUserId != null)) {
            throw new IllegalArgumentException(
                    "Only AUTHENTICATED must contain a current user UUID");
        }
        this.currentUserId = currentUserId;
    }

    public SessionRepository.State getState() {
        return state;
    }

    /** Returns null for CHECKING, SIGNED_OUT and UNAVAILABLE. */
    public UUID getCurrentUserId() {
        return currentUserId;
    }

    public UUID getSessionEpoch() {
        return sessionEpoch;
    }

    /**
     * Call on the latest repository snapshot before applying an asynchronous account result.
     * A previously captured snapshot cannot establish that its scope is still current.
     */
    public boolean isAuthenticatedIn(UUID expectedEpoch) {
        return state == SessionRepository.State.AUTHENTICATED
                && sessionEpoch.equals(expectedEpoch);
    }
}
