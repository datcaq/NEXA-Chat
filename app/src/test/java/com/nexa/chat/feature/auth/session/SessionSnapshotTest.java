package com.nexa.chat.feature.auth.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.UUID;
import org.junit.Test;

public class SessionSnapshotTest {
    private final UUID userId = UUID.randomUUID();
    private final UUID epoch = UUID.randomUUID();

    @Test
    public void authenticatedValueKeepsItsIdentityAndEpoch() {
        SessionSnapshot snapshot =
                new SessionSnapshot(SessionRepository.State.AUTHENTICATED, userId, epoch);
        assertEquals(SessionRepository.State.AUTHENTICATED, snapshot.getState());
        assertEquals(userId, snapshot.getCurrentUserId());
        assertEquals(epoch, snapshot.getSessionEpoch());
        assertTrue(snapshot.isAuthenticatedIn(epoch));
    }

    @Test
    public void authenticatedValueRequiresAUserUuid() {
        assertThrows(IllegalArgumentException.class,
                () -> new SessionSnapshot(SessionRepository.State.AUTHENTICATED, null, epoch));
    }

    @Test
    public void nonAuthenticatedStatesRejectAnIdentity() {
        for (SessionRepository.State state : SessionRepository.State.values()) {
            if (state != SessionRepository.State.AUTHENTICATED) {
                assertThrows(IllegalArgumentException.class,
                        () -> new SessionSnapshot(state, userId, epoch));
            }
        }
    }

    @Test
    public void nonAuthenticatedStatesExposeNoIdentityOrAccess() {
        for (SessionRepository.State state : SessionRepository.State.values()) {
            if (state != SessionRepository.State.AUTHENTICATED) {
                SessionSnapshot snapshot = new SessionSnapshot(state, null, epoch);
                assertNull(snapshot.getCurrentUserId());
                assertFalse(snapshot.isAuthenticatedIn(epoch));
            }
        }
    }

    @Test
    public void requiresStateAndEpoch() {
        assertThrows(NullPointerException.class, () -> new SessionSnapshot(null, null, epoch));
        for (SessionRepository.State state : SessionRepository.State.values()) {
            UUID identity = state == SessionRepository.State.AUTHENTICATED ? userId : null;
            assertThrows(NullPointerException.class,
                    () -> new SessionSnapshot(state, identity, null));
        }
    }

    @Test
    public void anotherEpochCannotAuthorizeAnOldCallbackEvenForTheSameUser() {
        SessionSnapshot current = new SessionSnapshot(
                SessionRepository.State.AUTHENTICATED, userId, UUID.randomUUID());
        assertFalse(current.isAuthenticatedIn(epoch));
        assertFalse(current.isAuthenticatedIn(null));
    }

    @Test
    public void newSnapshotDoesNotMutateAnOlderValue() {
        SessionSnapshot old =
                new SessionSnapshot(SessionRepository.State.AUTHENTICATED, userId, epoch);
        SessionSnapshot signedOut =
                new SessionSnapshot(SessionRepository.State.SIGNED_OUT, null, UUID.randomUUID());
        assertEquals(userId, old.getCurrentUserId());
        assertEquals(epoch, old.getSessionEpoch());
        assertNull(signedOut.getCurrentUserId());
        assertFalse(signedOut.isAuthenticatedIn(epoch));
    }
}
