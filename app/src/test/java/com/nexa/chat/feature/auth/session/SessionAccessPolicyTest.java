package com.nexa.chat.feature.auth.session;

import static com.nexa.chat.feature.auth.session.SessionAccessPolicy.Destination.AUTH;
import static com.nexa.chat.feature.auth.session.SessionAccessPolicy.Destination.CHECK_SESSION;
import static com.nexa.chat.feature.auth.session.SessionAccessPolicy.Destination.HOME;
import static com.nexa.chat.feature.auth.session.SessionAccessPolicy.Destination.RETRY_SESSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class SessionAccessPolicyTest {
    @Test
    public void restoredButUnverifiedStateCannotOpenHome() {
        assertEquals(CHECK_SESSION, SessionAccessPolicy.destinationFor(null));
        assertEquals(CHECK_SESSION,
                SessionAccessPolicy.destinationFor(SessionRepository.State.CHECKING));
    }

    @Test
    public void verificationFailureKeepsHomeClosedAndAllowsRetry() {
        assertEquals(RETRY_SESSION,
                SessionAccessPolicy.destinationFor(SessionRepository.State.UNAVAILABLE));
    }

    @Test
    public void onlyAnAuthenticatedStateSelectsHome() {
        for (SessionRepository.State state : SessionRepository.State.values()) {
            if (state == SessionRepository.State.AUTHENTICATED) {
                assertEquals(HOME, SessionAccessPolicy.destinationFor(state));
            } else {
                assertNotEquals(HOME, SessionAccessPolicy.destinationFor(state));
            }
        }
    }

    @Test
    public void logoutStateLeavesHomeAndSelectsAuth() {
        assertEquals(HOME,
                SessionAccessPolicy.destinationFor(SessionRepository.State.AUTHENTICATED));
        assertEquals(AUTH,
                SessionAccessPolicy.destinationFor(SessionRepository.State.SIGNED_OUT));
    }
}
