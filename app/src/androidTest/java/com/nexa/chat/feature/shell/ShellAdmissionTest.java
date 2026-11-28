package com.nexa.chat.feature.shell;

import static com.nexa.chat.feature.auth.session.SessionAccessPolicy.Destination.AUTH;
import static com.nexa.chat.feature.auth.session.SessionAccessPolicy.Destination.CHECK_SESSION;
import static com.nexa.chat.feature.auth.session.SessionAccessPolicy.Destination.HOME;
import static com.nexa.chat.feature.auth.session.SessionAccessPolicy.Destination.RETRY_SESSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.nexa.chat.MainActivity;
import com.nexa.chat.R;
import com.nexa.chat.feature.auth.session.SessionRepository;
import com.nexa.chat.feature.auth.session.SessionSnapshot;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public final class ShellAdmissionTest {
    private static final UUID ACCOUNT_A = new UUID(0, 1);
    private static final UUID ACCOUNT_B = new UUID(0, 2);

    private final FixtureSessions sessions = new FixtureSessions();
    private final Queue<Runnable> ui = new ArrayDeque<>();

    @Test
    public void pendingRestoreDoesNotMountHomeAndValidatedResultAdmitsHome() {
        try (ActivityScenario<MainActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                ShellAdmissionHost host = attach(activity);
                UUID restoreEpoch = sessions.currentSnapshot().getSessionEpoch();
                sessions.restore(restoreEpoch);
                drain();
                assertEquals(CHECK_SESSION, host.destination());
                assertNull(host.accountContent());

                sessions.completeRestore(restoreEpoch, ACCOUNT_A);
                drain();
                assertEquals(HOME, host.destination());
                assertEquals(ACCOUNT_A, host.userId());
                assertNotNull(host.accountContent());
            });
        }
    }

    @Test
    public void expiredSessionReturnsToAuthAndClearsPrivateView() {
        try (ActivityScenario<MainActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                ShellAdmissionHost host = attach(activity);
                sessions.validatedAccount(ACCOUNT_A);
                drain();
                TextView oldContent = host.accountContent();
                oldContent.setText("private fixture");
                UUID accountEpoch = sessions.currentSnapshot().getSessionEpoch();
                sessions.refresh(accountEpoch);
                drain();
                assertEquals(CHECK_SESSION, host.destination());
                assertNull(host.accountContent());

                sessions.reject(accountEpoch);
                drain();
                assertEquals(AUTH, host.destination());
                assertNull(host.userId());
                assertEquals("", oldContent.getText().toString());
            });
        }
    }

    @Test
    public void logoutFencesQueuedAuthenticatedNotificationAndLateRestore() {
        try (ActivityScenario<MainActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                ShellAdmissionHost host = attach(activity);
                UUID restoreEpoch = sessions.currentSnapshot().getSessionEpoch();
                sessions.restore(restoreEpoch);
                host.logout();
                sessions.completeRestore(restoreEpoch, ACCOUNT_A);
                assertEquals(SessionRepository.State.SIGNED_OUT, sessions.currentSnapshot().getState());
                drain();
                assertEquals(AUTH, host.destination());

                sessions.validatedAccount(ACCOUNT_A);
                // Leave the AUTHENTICATED notification queued until after logout.
                host.logout();
                assertEquals(AUTH, host.destination());
                while (!ui.isEmpty()) {
                    ui.remove().run();
                    // Check each delivery so transient admission cannot hide behind a later AUTH.
                    assertEquals(AUTH, host.destination());
                    assertNull(host.userId());
                    assertNull(host.accountContent());
                }
            });
        }
    }

    @Test
    public void replacementAccountDoesNotInheritPrivateViewOrAcceptStaleLogout() {
        try (ActivityScenario<MainActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                ShellAdmissionHost host = attach(activity);
                sessions.validatedAccount(ACCOUNT_A);
                drain();
                UUID oldEpoch = sessions.currentSnapshot().getSessionEpoch();
                TextView oldContent = host.accountContent();
                oldContent.setText("account A fixture");

                sessions.validatedAccount(ACCOUNT_B);
                sessions.logout(oldEpoch);
                drain();
                assertEquals(HOME, host.destination());
                assertEquals(ACCOUNT_B, host.userId());
                assertNotSame(oldContent, host.accountContent());
                assertEquals("", oldContent.getText().toString());
                assertEquals("", host.accountContent().getText().toString());
                host.logout();
                assertEquals(AUTH, host.destination());
                assertNull(host.accountContent());
            });
        }
    }

    @Test
    public void unavailableSessionFailsClosedAfterHomeWasOpen() {
        try (ActivityScenario<MainActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                ShellAdmissionHost host = attach(activity);
                sessions.validatedAccount(ACCOUNT_A);
                drain();
                TextView oldContent = host.accountContent();
                oldContent.setText("private fixture");
                sessions.unavailable();
                drain();
                assertEquals(RETRY_SESSION, host.destination());
                assertNull(host.userId());
                assertNull(host.accountContent());
                assertEquals("", oldContent.getText().toString());
            });
        }
    }

    @Test
    public void recreationWhileCheckingDoesNotGrantHomeOrRestoreOldContent() {
        AtomicReference<TextView> oldContent = new AtomicReference<>();
        try (ActivityScenario<MainActivity> scenario = launch()) {
            scenario.onActivity(activity -> {
                ShellAdmissionHost host = attach(activity);
                sessions.validatedAccount(ACCOUNT_A);
                drain();
                oldContent.set(host.accountContent());
                oldContent.get().setText("private fixture");
                // Queue a non-authenticated notification on the old Activity.
                sessions.refresh(sessions.currentSnapshot().getSessionEpoch());
            });
            scenario.recreate();
            scenario.onActivity(activity -> {
                assertNotNull(activity.findViewById(R.id.auth_title));
                ShellAdmissionHost recreated = attach(activity);
                drain();
                assertEquals(CHECK_SESSION, recreated.destination());
                assertNull(recreated.userId());
                assertNull(recreated.accountContent());
                assertEquals("", oldContent.get().getText().toString());
            });
        }
    }

    private static ActivityScenario<MainActivity> launch() {
        return ActivityScenario.launch(MainActivity.class);
    }

    private ShellAdmissionHost attach(MainActivity activity) {
        return new ShellAdmissionHost(activity, sessions, ui::add);
    }

    private void drain() {
        while (!ui.isEmpty()) {
            ui.remove().run();
        }
    }

    /** Deterministic main-thread fixture. It never validates a credential or calls a backend. */
    private static final class FixtureSessions implements SessionRepository {
        private SessionSnapshot snapshot = new SessionSnapshot(State.CHECKING, null, UUID.randomUUID());
        private FixtureSubscription subscription;

        @Override
        public SessionSnapshot currentSnapshot() {
            return snapshot;
        }

        @Override
        public Subscription observe(Executor callbackExecutor, Observer observer) {
            subscription = new FixtureSubscription(callbackExecutor, observer);
            subscription.enqueue(snapshot);
            return subscription;
        }

        @Override
        public void restore(UUID expectedEpoch) {
            refresh(expectedEpoch);
        }

        @Override
        public void refresh(UUID expectedEpoch) {
            if (snapshot.getSessionEpoch().equals(expectedEpoch)) {
                publish(new SessionSnapshot(State.CHECKING, null, expectedEpoch));
            }
        }

        @Override
        public void logout(UUID expectedEpoch) {
            reject(expectedEpoch);
        }

        void reject(UUID expectedEpoch) {
            if (snapshot.getSessionEpoch().equals(expectedEpoch)) {
                publish(new SessionSnapshot(State.SIGNED_OUT, null, UUID.randomUUID()));
            }
        }

        void completeRestore(UUID expectedEpoch, UUID user) {
            if (snapshot.getState() == State.CHECKING && snapshot.getSessionEpoch().equals(expectedEpoch)) {
                validatedAccount(user);
            }
        }

        void validatedAccount(UUID user) {
            publish(new SessionSnapshot(State.AUTHENTICATED, user, UUID.randomUUID()));
        }

        void unavailable() {
            publish(new SessionSnapshot(State.UNAVAILABLE, null, snapshot.getSessionEpoch()));
        }

        private void publish(SessionSnapshot next) {
            snapshot = next;
            if (subscription != null) {
                subscription.enqueue(next);
            }
        }

        private static final class FixtureSubscription implements Subscription {
            private final Executor executor;
            private final Observer observer;
            private boolean closed;

            FixtureSubscription(Executor executor, Observer observer) {
                this.executor = executor;
                this.observer = observer;
            }

            void enqueue(SessionSnapshot next) {
                if (!closed) {
                    executor.execute(() -> {
                        if (!closed) {
                            observer.onSnapshotChanged(next);
                        }
                    });
                }
            }

            @Override
            public void close() {
                closed = true;
            }
        }
    }
}
