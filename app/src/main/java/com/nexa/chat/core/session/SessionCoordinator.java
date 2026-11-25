package com.nexa.chat.core.session;

import static com.nexa.chat.feature.auth.session.SessionAccessPolicy.Destination.CHECK_SESSION;
import static com.nexa.chat.feature.auth.session.SessionAccessPolicy.Destination.HOME;

import com.nexa.chat.feature.auth.session.SessionAccessPolicy;
import com.nexa.chat.feature.auth.session.SessionAccessPolicy.Destination;
import com.nexa.chat.feature.auth.session.SessionRepository;
import com.nexa.chat.feature.auth.session.SessionSnapshot;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;

/**
 * Internal coordination for an eventual shell. The repository owns session identity and validity.
 * Construct and use this on one owner thread. The observer executor delivers on that same thread
 * and stays available until close. Resource hooks must return promptly without waiting for I/O
 * or callbacks; their implementation and cleanup policy belong to the resource owner.
 * Adapter hooks report recoverable failures as runtime exceptions. An owner must retain this
 * coordinator while it still holds quarantined handles, even after its UI scope is closed.
 */
final class SessionCoordinator implements AutoCloseable {
    interface ResourceFactory {
        /**
         * Prepares isolated resources without starting protected I/O. The session can change
         * concurrently with this call. Empty means the optional resources are unavailable.
         * The factory owns and rolls back partial allocations until it returns successfully.
         */
        Optional<Resources> open(UUID userId, UUID epoch);
    }

    interface Resources {
        /** Idempotently fence logical access before returning. Throw if closure cannot be proven. */
        void invalidate();

        /**
         * Nonblocking, idempotent handoff of physical cleanup to the resource owner. On success
         * that owner tracks termination. Throw only if the handoff failed and the caller still
         * owns the handle. Never wait for an I/O task, callback or worker to terminate here.
         */
        default void scheduleCleanup() {}
    }

    enum Failure {
        OBSERVE, PREPARATION, INVALIDATION, CLEANUP_HANDOFF, UNSUBSCRIBE
    }

    private static final int MAX_ATTEMPTS = 2;
    private final Thread owner = Thread.currentThread();
    private final SessionRepository repository;
    private final Executor observerExecutor;
    private final ResourceFactory resourceFactory;
    private SessionRepository.Subscription subscription;
    private Object observation;
    private Scope scope;
    // Retiring may still be logically unsafe. Pending cleanup holds only a fenced scope.
    private Scope retiring;
    private Scope pendingCleanup;
    private SessionSnapshot failedPreparation;
    private int preparationFailures;
    private int subscriptionAttempts;
    private int unsubscribeAttempts;
    private Failure lastFailure;
    private boolean starting;
    private boolean started;
    private boolean reconciling;
    private boolean dirty;
    private boolean closed;

    SessionCoordinator(SessionRepository repository, Executor observerExecutor,
                       ResourceFactory resourceFactory) {
        this.repository = Objects.requireNonNull(repository);
        this.observerExecutor = Objects.requireNonNull(observerExecutor);
        this.resourceFactory = Objects.requireNonNull(resourceFactory);
    }

    void start() {
        requireOwner();
        if (started || starting || closed || subscriptionAttempts >= MAX_ATTEMPTS) {
            return;
        }
        starting = true;
        subscriptionAttempts++;
        Object generation = new Object();
        observation = generation;
        try {
            subscription = Objects.requireNonNull(repository.observe(
                    observerExecutor, ignored -> onSessionChanged(generation)));
            started = !closed;
        } catch (RuntimeException failure) {
            // The repository owns any registration for which it never returned a handle.
            observation = null;
            lastFailure = Failure.OBSERVE;
        } finally {
            starting = false;
        }
        drain();
    }

    Failure lastFailure() {
        requireOwner();
        return lastFailure;
    }

    /** Explicit retry only. A stale caller cannot retry work for a replacement account. */
    void retry(UUID expectedUserId, UUID expectedEpoch) {
        requireOwner();
        if (starting || reconciling) {
            return;
        }
        SessionSnapshot expected = repository.currentSnapshot();
        if (!sameAccount(expected, expectedUserId, expectedEpoch)) {
            return;
        }
        reconciling = true;
        try {
            if (pendingCleanup != null && handOffCleanup(pendingCleanup, true)) {
                pendingCleanup = null;
            }
            finishRetirement(true);
            if (closed) {
                unsubscribe(true);
            } else if (started) {
                reconcile(expected);
            }
        } finally {
            reconciling = false;
        }
        drain();
    }

    /** Evaluate access at use time, including while an observation is waiting in the owner queue. */
    Destination destination() {
        requireOwner();
        if (!started || closed) {
            return CHECK_SESSION;
        }
        SessionSnapshot current = repository.currentSnapshot();
        Destination destination = SessionAccessPolicy.destinationFor(current);
        if (destination == HOME && (retiring != null || scope == null || !scope.canRetain(current))) {
            return CHECK_SESSION;
        }
        return destination;
    }

    private void onSessionChanged(Object generation) {
        requireOwner();
        if (!closed && observation == generation) {
            dirty = true;
            drain();
        }
    }

    private void drain() {
        if (starting || reconciling) {
            return;
        }
        reconciling = true;
        try {
            while (!closed && started && dirty) {
                dirty = false;
                reconcile(null);
            }
            if (closed) {
                try {
                    retireScope();
                    finishRetirement(false);
                } finally {
                    unsubscribe(false);
                }
            }
        } finally {
            reconciling = false;
        }
    }

    private void reconcile(SessionSnapshot retry) {
        // An observer payload is a wake-up signal. Its epoch may already be stale.
        SessionSnapshot current = repository.currentSnapshot();
        if (scope != null && !scope.canRetain(current)) {
            retireScope();
        }
        finishRetirement(false);
        current = repository.currentSnapshot();
        if (closed || retiring != null || scope != null
                || SessionAccessPolicy.destinationFor(current) != HOME) {
            return;
        }

        UUID userId = current.getCurrentUserId();
        UUID epoch = current.getSessionEpoch();
        if (retry != null && !sameAccount(retry, userId, epoch)) {
            return;
        }
        boolean sameFailure = failedPreparation != null
                && sameAccount(failedPreparation, userId, epoch);
        if (sameFailure && (retry == null || preparationFailures >= MAX_ATTEMPTS)) {
            return;
        }
        Scope opened;
        try {
            opened = new Scope(userId, epoch, resourceFactory.open(userId, epoch));
        } catch (RuntimeException failure) {
            failedPreparation = current;
            preparationFailures = sameFailure ? preparationFailures + 1 : 1;
            lastFailure = Failure.PREPARATION;
            return;
        }
        failedPreparation = null;
        preparationFailures = 0;
        if (closed || !opened.canRetain(repository.currentSnapshot())) {
            retiring = opened;
            finishRetirement(false);
            return;
        }
        scope = opened;
    }

    private void retireScope() {
        if (scope != null) {
            retiring = scope;
            scope = null;
        }
    }

    private void finishRetirement(boolean retry) {
        if (retiring == null) {
            return;
        }
        if (!retiring.invalidated) {
            if (!canAttempt(retiring.invalidationAttempts, retry)) {
                return;
            }
            retiring.invalidationAttempts++;
            try {
                retiring.resources.ifPresent(Resources::invalidate);
                retiring.invalidated = true;
            } catch (RuntimeException failure) {
                lastFailure = Failure.INVALIDATION;
                return;
            }
        }
        if (handOffCleanup(retiring, retry)) {
            retiring = null;
        } else if (pendingCleanup == null) {
            pendingCleanup = retiring;
            retiring = null;
        }
        // If both slots are occupied, keep ownership and block replacement until one is released.
    }

    private boolean handOffCleanup(Scope previous, boolean retry) {
        if (!canAttempt(previous.cleanupAttempts, retry)) {
            return false;
        }
        previous.cleanupAttempts++;
        try {
            previous.resources.ifPresent(Resources::scheduleCleanup);
            return true;
        } catch (RuntimeException failure) {
            lastFailure = Failure.CLEANUP_HANDOFF;
            return false;
        }
    }

    private void unsubscribe(boolean retry) {
        if (subscription == null || !canAttempt(unsubscribeAttempts, retry)) {
            return;
        }
        unsubscribeAttempts++;
        try {
            subscription.close();
            subscription = null;
        } catch (RuntimeException failure) {
            lastFailure = Failure.UNSUBSCRIBE;
        }
    }

    private static boolean canAttempt(int attempts, boolean retry) {
        return attempts == 0 || (retry && attempts < MAX_ATTEMPTS);
    }

    private static boolean sameAccount(SessionSnapshot snapshot, UUID userId, UUID epoch) {
        return snapshot.getSessionEpoch().equals(epoch)
                && Objects.equals(snapshot.getCurrentUserId(), userId);
    }

    @Override
    public void close() {
        requireOwner();
        if (closed) {
            return;
        }
        closed = true;
        observation = null;
        drain();
    }

    private void requireOwner() {
        if (Thread.currentThread() != owner) {
            throw new IllegalStateException("Session coordinator must run on its owner thread");
        }
    }

    private static final class Scope {
        private final UUID userId;
        private final UUID epoch;
        private final Optional<Resources> resources;
        private int invalidationAttempts;
        private int cleanupAttempts;
        private boolean invalidated;

        private Scope(UUID userId, UUID epoch, Optional<Resources> resources) {
            this.userId = userId;
            this.epoch = epoch;
            this.resources = Objects.requireNonNull(resources);
        }

        private boolean canRetain(SessionSnapshot snapshot) {
            UUID currentUser = snapshot.getCurrentUserId();
            return epoch.equals(snapshot.getSessionEpoch())
                    && snapshot.getState() != SessionRepository.State.SIGNED_OUT
                    && (currentUser == null || userId.equals(currentUser));
        }
    }
}
