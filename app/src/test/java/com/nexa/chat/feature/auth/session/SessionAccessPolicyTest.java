package com.nexa.chat.feature.auth.session;

import static com.nexa.chat.feature.auth.session.SessionAccessPolicy.Destination.AUTH;
import static com.nexa.chat.feature.auth.session.SessionAccessPolicy.Destination.CHECK_SESSION;
import static com.nexa.chat.feature.auth.session.SessionAccessPolicy.Destination.HOME;
import static com.nexa.chat.feature.auth.session.SessionAccessPolicy.Destination.RETRY_SESSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.UUID;
import org.junit.Test;

public class SessionAccessPolicyTest {
    // Value fixtures exercise invariants only; no session implementation or login is installed.
    private final UUID userId = UUID.randomUUID();
    private final UUID epoch = UUID.randomUUID();

    @Test
    public void unknownAndCheckingSnapshotsKeepHomeClosed() {
        assertEquals(CHECK_SESSION, SessionAccessPolicy.destinationFor(null));
        assertEquals(CHECK_SESSION, SessionAccessPolicy.destinationFor(
                new SessionSnapshot(SessionRepository.State.CHECKING, null, epoch)));
    }

    @Test
    public void verificationUnavailableAllowsRetryWithoutOpeningHome() {
        assertEquals(RETRY_SESSION, SessionAccessPolicy.destinationFor(
                new SessionSnapshot(SessionRepository.State.UNAVAILABLE, null, epoch)));
    }

    @Test
    public void onlyAuthenticatedSnapshotSelectsHome() {
        for (SessionRepository.State state : SessionRepository.State.values()) {
            UUID identity = state == SessionRepository.State.AUTHENTICATED ? userId : null;
            SessionSnapshot snapshot = new SessionSnapshot(state, identity, epoch);
            if (state == SessionRepository.State.AUTHENTICATED) {
                assertEquals(HOME, SessionAccessPolicy.destinationFor(snapshot));
            } else {
                assertNotEquals(HOME, SessionAccessPolicy.destinationFor(snapshot));
            }
        }
    }

    @Test
    public void signedOutSnapshotSelectsAuth() {
        assertEquals(AUTH, SessionAccessPolicy.destinationFor(
                new SessionSnapshot(SessionRepository.State.SIGNED_OUT, null, UUID.randomUUID())));
    }
}
