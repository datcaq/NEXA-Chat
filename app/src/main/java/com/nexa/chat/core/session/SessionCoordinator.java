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
 */
final class SessionCoordinator implements AutoCloseable {
    interface ResourceFactory {
        /**
         * Prepares isolated resources without starting protected I/O. The session can change
         * concurrently with this call. Empty means the optional resources are unavailable.
         */
        Optional<Resources> open(UUID userId, UUID epoch);
    }

    interface Resources {
        /** Close logical access before returning. Physical cleanup may finish asynchronously. */
        void invalidate();
    }

    private final Thread owner = Thread.currentThread();
    private final SessionRepository repository;
    private final Executor observerExecutor;
    private final ResourceFactory resourceFactory;
    private SessionRepository.Subscription subscription;
    private Scope scope;
    private boolean started;
    private boolean closed;

    SessionCoordinator(SessionRepository repository, Executor observerExecutor,
                       ResourceFactory resourceFactory) {
        this.repository = Objects.requireNonNull(repository);
        this.observerExecutor = Objects.requireNonNull(observerExecutor);
        this.resourceFactory = Objects.requireNonNull(resourceFactory);
    }

    void start() {
        requireOwner();
        if (started || closed) {
            return;
        }
        started = true;
        SessionRepository.Subscription opened = repository.observe(
                observerExecutor, ignored -> onSessionChanged());
        if (closed) {
            // The initial observer may have run inline and closed the coordinator.
            opened.close();
        } else {
            subscription = opened;
        }
    }

    /** Evaluate access at use time, including while an observation is waiting in the owner queue. */
    Destination destination() {
        requireOwner();
        if (!started || closed) {
            return CHECK_SESSION;
        }
        SessionSnapshot current = repository.currentSnapshot();
        Destination destination = SessionAccessPolicy.destinationFor(current);
        if (destination == HOME && (scope == null || !scope.canRetain(current))) {
            return CHECK_SESSION;
        }
        return destination;
    }

    private void onSessionChanged() {
        requireOwner();
        // SessionRepository serializes delivery per subscription, including with a direct executor.
        if (!closed) {
            reconcile();
        }
    }

    private void reconcile() {
        // An observer payload is a wake-up signal. Its epoch may already be stale.
        SessionSnapshot current = repository.currentSnapshot();
        if (scope != null && !scope.canRetain(current)) {
            retireScope();
            if (closed) {
                return;
            }
            current = repository.currentSnapshot();
        }
        if (scope != null || SessionAccessPolicy.destinationFor(current) != HOME) {
            return;
        }

        UUID userId = current.getCurrentUserId();
        UUID epoch = current.getSessionEpoch();
        Scope opened = new Scope(userId, epoch, resourceFactory.open(userId, epoch));
        if (closed || !opened.canRetain(repository.currentSnapshot())) {
            opened.invalidate();
            return;
        }
        scope = opened;
    }

    private void retireScope() {
        Scope previous = scope;
        scope = null;
        if (previous != null) {
            previous.invalidate();
        }
    }

    @Override
    public void close() {
        requireOwner();
        if (closed) {
            return;
        }
        closed = true;
        SessionRepository.Subscription previous = subscription;
        subscription = null;
        try {
            retireScope();
        } finally {
            if (previous != null) {
                previous.close();
            }
        }
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

        private void invalidate() {
            resources.ifPresent(Resources::invalidate);
        }
    }
}
