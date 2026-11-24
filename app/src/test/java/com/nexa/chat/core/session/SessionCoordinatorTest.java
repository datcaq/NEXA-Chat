package com.nexa.chat.core.session;

import static com.nexa.chat.feature.auth.session.SessionAccessPolicy.Destination.AUTH;
import static com.nexa.chat.feature.auth.session.SessionAccessPolicy.Destination.CHECK_SESSION;
import static com.nexa.chat.feature.auth.session.SessionAccessPolicy.Destination.HOME;
import static com.nexa.chat.feature.auth.session.SessionAccessPolicy.Destination.RETRY_SESSION;
import static com.nexa.chat.feature.auth.session.SessionRepository.State.AUTHENTICATED;
import static com.nexa.chat.feature.auth.session.SessionRepository.State.CHECKING;
import static com.nexa.chat.feature.auth.session.SessionRepository.State.SIGNED_OUT;
import static com.nexa.chat.feature.auth.session.SessionRepository.State.UNAVAILABLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.nexa.chat.feature.auth.session.SessionRepository;
import com.nexa.chat.feature.auth.session.SessionSnapshot;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public final class SessionCoordinatorTest {
    private static final UUID A = new UUID(0L, 1L);
    private static final UUID B = new UUID(0L, 2L);
    private static final UUID INITIAL = new UUID(0L, 3L);
    private static final UUID EPOCH_A = new UUID(0L, 4L);
    private static final UUID EPOCH_B = new UUID(0L, 5L);
    private static final UUID SIGNED_OUT_EPOCH = new UUID(0L, 6L);

    private final TestRepository repository = new TestRepository(withoutIdentity(CHECKING, INITIAL));
    private final Queue<Runnable> ownerQueue = new ConcurrentLinkedQueue<>();
    private final List<RecordingResources> opened = new ArrayList<>();
    private final List<String> events = new ArrayList<>();
    private final SessionCoordinator coordinator =
            new SessionCoordinator(repository, ownerQueue::add, this::openResources);

    @Test
    public void initialCheckingKeepsProtectedAccessClosed() {
        assertEquals(CHECK_SESSION, coordinator.destination());
        start();

        assertEquals(CHECK_SESSION, coordinator.destination());
        assertTrue(opened.isEmpty());
    }

    @Test
    public void signedOutSelectsAuth() {
        start();
        publish(withoutIdentity(SIGNED_OUT, SIGNED_OUT_EPOCH));

        assertEquals(AUTH, coordinator.destination());
        assertTrue(opened.isEmpty());
    }

    @Test
    public void authenticatedSnapshotOpensScopeBeforeProtectedAccess() {
        start();
        repository.publish(authenticated(A, EPOCH_A));

        assertEquals(CHECK_SESSION, coordinator.destination());
        drainOwner();

        assertEquals(HOME, coordinator.destination());
        assertEquals(1, opened.size());
        assertEquals(A, opened.get(0).userId);
        assertEquals(EPOCH_A, opened.get(0).epoch);
    }

    @Test
    public void unavailableSelectsRetryWithoutCreatingScope() {
        start();
        publish(withoutIdentity(UNAVAILABLE, INITIAL));

        assertEquals(RETRY_SESSION, coordinator.destination());
        assertTrue(opened.isEmpty());
    }

    @Test
    public void repeatedStartAndAuthenticatedSnapshotsDoNotDuplicateScope() {
        start();
        publish(authenticated(A, EPOCH_A));
        coordinator.start();
        publish(authenticated(A, EPOCH_A));
        publish(authenticated(A, EPOCH_A));

        assertEquals(1, repository.observations);
        assertEquals(1, opened.size());
        assertEquals(HOME, coordinator.destination());
    }

    @Test
    public void checkingAndUnavailableSuspendAccessButRetainScopeForSameEpoch() {
        start();
        publish(authenticated(A, EPOCH_A));

        publish(withoutIdentity(CHECKING, EPOCH_A));
        assertEquals(CHECK_SESSION, coordinator.destination());
        publish(withoutIdentity(UNAVAILABLE, EPOCH_A));
        assertEquals(RETRY_SESSION, coordinator.destination());
        assertEquals(0, opened.get(0).invalidations);

        publish(authenticated(A, EPOCH_A));
        assertEquals(HOME, coordinator.destination());
        assertEquals(1, opened.size());
        assertEquals(0, opened.get(0).invalidations);
    }

    @Test
    public void logoutDeniesAccessBeforeQueuedCleanupAndRetiresResourcesOnce() {
        start();
        publish(authenticated(A, EPOCH_A));
        repository.logout(EPOCH_A);

        assertEquals(AUTH, coordinator.destination());
        drainOwner();
        assertEquals(1, opened.get(0).invalidations);
        coordinator.close();
        assertEquals(1, opened.get(0).invalidations);
    }

    @Test
    public void switchingAccountInvalidatesOldScopeBeforeOpeningNewScope() {
        start();
        publish(authenticated(A, EPOCH_A));

        repository.publish(authenticated(B, EPOCH_B));
        assertEquals(CHECK_SESSION, coordinator.destination());
        drainOwner();

        assertEquals(Arrays.asList("open:" + EPOCH_A, "invalidate:" + EPOCH_A,
                "open:" + EPOCH_B), events);
        assertEquals(B, opened.get(1).userId);
        assertEquals(HOME, coordinator.destination());
    }

    @Test
    public void freshEpochForSameUserRequiresANewScope() {
        start();
        publish(authenticated(A, EPOCH_A));
        publish(authenticated(A, EPOCH_B));

        assertEquals(2, opened.size());
        assertEquals(1, opened.get(0).invalidations);
        assertEquals(EPOCH_B, opened.get(1).epoch);
    }

    @Test
    public void definitiveRejectionRetiresScopeAndQueuedOldPayloadCannotRestoreIt() {
        start();
        SessionSnapshot old = authenticated(A, EPOCH_A);
        publish(old);
        repository.publish(old);
        repository.publish(withoutIdentity(SIGNED_OUT, SIGNED_OUT_EPOCH));
        drainOwner();

        assertEquals(AUTH, coordinator.destination());
        assertEquals(1, opened.size());
        assertEquals(1, opened.get(0).invalidations);
    }

    @Test
    public void queuedAuthenticatedPayloadDoesNotOpenAStaleScope() {
        start();
        repository.publish(authenticated(A, EPOCH_A));
        repository.publish(authenticated(B, EPOCH_B));
        drainOwner();

        assertEquals(1, opened.size());
        assertEquals(B, opened.get(0).userId);
        assertEquals(HOME, coordinator.destination());
    }

    @Test
    public void scopeOpenedDuringAChangeIsRetiredBeforeItCanGrantAccess() {
        try (SessionCoordinator changing = new SessionCoordinator(repository, ownerQueue::add,
                (user, epoch) -> {
                    Optional<SessionCoordinator.Resources> result = openResources(user, epoch);
                    if (epoch.equals(EPOCH_A)) {
                        repository.publish(authenticated(B, EPOCH_B));
                    }
                    return result;
                })) {
            changing.start();
            drainOwner();
            repository.publish(authenticated(A, EPOCH_A));
            ownerQueue.remove().run();

            assertEquals(1, opened.get(0).invalidations);
            assertEquals(CHECK_SESSION, changing.destination());
            drainOwner();
            assertEquals(HOME, changing.destination());
            assertEquals(B, opened.get(1).userId);
        }
    }

    @Test
    public void closeDetachesObserverAndRejectsLateDelivery() {
        start();
        publish(authenticated(A, EPOCH_A));
        Runnable late = repository.captureDelivery(authenticated(B, EPOCH_B));

        repository.publish(authenticated(B, EPOCH_B));
        coordinator.close();
        coordinator.close();
        late.run();
        drainOwner();

        assertEquals(1, repository.subscriptionCloses);
        assertEquals(1, opened.size());
        assertEquals(1, opened.get(0).invalidations);
        assertEquals(CHECK_SESSION, coordinator.destination());
    }

    @Test
    public void closeDuringInlineObservationClosesReturnedSubscriptionAndResources() {
        repository.publish(authenticated(A, EPOCH_A));
        SessionCoordinator[] reference = new SessionCoordinator[1];
        reference[0] = new SessionCoordinator(repository, Runnable::run, (user, epoch) -> {
            reference[0].close();
            return openResources(user, epoch);
        });

        reference[0].start();

        assertEquals(1, repository.subscriptionCloses);
        assertEquals(1, opened.get(0).invalidations);
        assertEquals(CHECK_SESSION, reference[0].destination());
    }

    @Test
    public void physicalCleanupCanFinishAfterNewScopeOpensWithoutChangingIt() {
        start();
        publish(authenticated(A, EPOCH_A));
        publish(authenticated(B, EPOCH_B));
        RecordingResources previous = opened.get(0);
        RecordingResources active = opened.get(1);

        assertEquals(1, previous.invalidations);
        assertFalse(previous.physicallyClosed);
        assertEquals(HOME, coordinator.destination());

        previous.finishPhysicalCleanup();

        assertTrue(previous.physicallyClosed);
        assertFalse(active.physicallyClosed);
        assertEquals(0, active.invalidations);
        assertEquals(HOME, coordinator.destination());
    }

    @Test
    public void unavailableModuleResourcesDoNotChangeValidSessionDestination() {
        List<UUID> attempts = new ArrayList<>();
        try (SessionCoordinator optionalResources = new SessionCoordinator(repository, ownerQueue::add,
                (user, epoch) -> {
                    attempts.add(epoch);
                    return Optional.empty();
                })) {
            optionalResources.start();
            drainOwner();
            publish(authenticated(A, EPOCH_A));
            publish(authenticated(A, EPOCH_A));

            assertEquals(HOME, optionalResources.destination());
            assertEquals(Arrays.asList(EPOCH_A), attempts);
            publish(withoutIdentity(SIGNED_OUT, SIGNED_OUT_EPOCH));
            assertEquals(AUTH, optionalResources.destination());
        }
    }

    @Test
    public void workerPublicationIsReconciledOnlyWhenOwnerRunsTheObserver() throws Exception {
        start();
        FutureTask<Void> worker = new FutureTask<>(() -> {
            repository.publish(authenticated(A, EPOCH_A));
            assertThrows(IllegalStateException.class, coordinator::destination);
            return null;
        });
        Thread thread = new Thread(worker, "session-publication-test");
        thread.setDaemon(true);
        thread.start();
        worker.get(5, TimeUnit.SECONDS);

        assertTrue(opened.isEmpty());
        assertEquals(CHECK_SESSION, coordinator.destination());
        drainOwner();
        assertEquals(HOME, coordinator.destination());
    }

    private void start() {
        coordinator.start();
        drainOwner();
    }

    private void publish(SessionSnapshot snapshot) {
        repository.publish(snapshot);
        drainOwner();
    }

    private void drainOwner() {
        for (Runnable action = ownerQueue.poll(); action != null; action = ownerQueue.poll()) {
            action.run();
        }
    }

    private Optional<SessionCoordinator.Resources> openResources(UUID user, UUID epoch) {
        RecordingResources resources = new RecordingResources(user, epoch);
        events.add("open:" + epoch);
        opened.add(resources);
        return Optional.of(resources);
    }

    private static SessionSnapshot authenticated(UUID user, UUID epoch) {
        return new SessionSnapshot(AUTHENTICATED, user, epoch);
    }

    private static SessionSnapshot withoutIdentity(SessionRepository.State state, UUID epoch) {
        return new SessionSnapshot(state, null, epoch);
    }

    private final class RecordingResources implements SessionCoordinator.Resources {
        final UUID userId;
        final UUID epoch;
        int invalidations;
        boolean physicallyClosed;

        RecordingResources(UUID userId, UUID epoch) {
            this.userId = userId;
            this.epoch = epoch;
        }

        @Override
        public void invalidate() {
            invalidations++;
            events.add("invalidate:" + epoch);
        }

        void finishPhysicalCleanup() {
            physicallyClosed = true;
        }
    }

    private static final class TestRepository implements SessionRepository {
        private volatile SessionSnapshot current;
        private Observer observer;
        private Executor executor;
        int observations;
        int subscriptionCloses;

        TestRepository(SessionSnapshot initial) {
            current = initial;
        }

        @Override
        public SessionSnapshot currentSnapshot() {
            return current;
        }

        @Override
        public Subscription observe(Executor executor, Observer observer) {
            this.executor = executor;
            this.observer = observer;
            observations++;
            notifyObserver(current);
            return () -> {
                this.observer = null;
                subscriptionCloses++;
            };
        }

        @Override
        public void restore(UUID expectedEpoch) {
            throw new AssertionError("Coordinator must not initiate restoration");
        }

        @Override
        public void refresh(UUID expectedEpoch) {
            throw new AssertionError("Coordinator must not initiate refresh");
        }

        @Override
        public void logout(UUID expectedEpoch) {
            assertEquals(expectedEpoch, current.getSessionEpoch());
            publish(withoutIdentity(SIGNED_OUT, SIGNED_OUT_EPOCH));
        }

        void publish(SessionSnapshot snapshot) {
            current = snapshot;
            notifyObserver(snapshot);
        }

        private void notifyObserver(SessionSnapshot snapshot) {
            Observer active = observer;
            if (active != null) {
                executor.execute(() -> {
                    if (observer == active) {
                        active.onSnapshotChanged(snapshot);
                    }
                });
            }
        }

        // Simulate a delivery that has already captured its observer when close wins.
        Runnable captureDelivery(SessionSnapshot payload) {
            Observer admitted = observer;
            return () -> admitted.onSnapshotChanged(payload);
        }
    }
}
