package com.nexa.chat.feature.auth.session;

import java.util.UUID;
import java.util.concurrent.Executor;

/**
 * Proposed TV1 session contract V2, pending TV2/TV3 review.
 * This interface neither stores credentials nor defines OTP, JWT lifetime or server API policy.
 *
 * <p>Implementations must be thread-safe. Snapshot publication and invalidation are serialized.
 * Each snapshot is atomic: callers must use its state, identity and epoch together.
 * Initial state is CHECKING. Local cached identity alone must never establish authentication.
 *
 * <p>Allocate a fresh local UUID epoch on process initialization, a newly authenticated scope,
 * logout, account change or definitive session rejection. Never reuse an invalidated epoch,
 * including when the same user signs in again. Credential refresh within a scope retains its
 * epoch. A UUID epoch is not a credential or a server authorization decision.
 */
public interface SessionRepository {
    enum State {
        CHECKING,
        SIGNED_OUT,
        AUTHENTICATED,
        UNAVAILABLE
    }

    /** Always returns a non-null snapshot. Null method arguments are rejected synchronously. */
    SessionSnapshot currentSnapshot();

    /**
     * Atomically registers and queues the current snapshot, then later snapshots in publication
     * order. Implementations serialize delivery per subscription on callbackExecutor, even if
     * that executor has multiple threads. A direct executor may deliver inline. Observers must
     * not block or throw. Callers own the executor and Android callers supply a main-thread executor.
     *
     * <p>Already queued snapshots can be stale when delivered. Check the latest snapshot state and epoch
     * before account side effects. The executor must accept work while the subscription is open.
     */
    Subscription observe(Executor callbackExecutor, Observer observer);

    /**
     * Starts asynchronous restoration for the expected current epoch, publishing CHECKING first.
     * Repeated pending restores coalesce. Stale-epoch calls are ignored. A successful validated
     * login establishes a fresh authenticated epoch; old restore results cannot replace a new scope.
     */
    void restore(UUID expectedEpoch);

    /**
     * Revalidates or refreshes asynchronously for the expected epoch. This operation and
     * CredentialProvider requests share one pending refresh per epoch. Stale-epoch calls are ignored.
     * While validity cannot be established, publish CHECKING or UNAVAILABLE without exposing identity.
     * Definitive rejection invalidates the epoch and publishes SIGNED_OUT.
     */
    void refresh(UUID expectedEpoch);

    /**
     * For the expected current epoch, invalidate local access before returning and publish SIGNED_OUT
     * with a fresh epoch. Stale-epoch calls are ignored, so an old screen cannot log out a new account.
     * Credential work from the old scope must fail as SCOPE_EXPIRED. Late authentication/refresh
     * results must never restore that scope. Server revoke and offline retry remain backend policy.
     *
     * <p>The future application coordinator closes protected screens, cancels account work,
     * disconnects realtime and clears account dependencies and private state. That coordinator
     * is not implemented by this contract.
     */
    void logout(UUID expectedEpoch);

    interface Observer {
        void onSnapshotChanged(SessionSnapshot snapshot);
    }

    interface Subscription extends AutoCloseable {
        /**
         * Thread-safe, idempotent. After close returns, no new callback may begin, including queued
         * callbacks. A callback that already began may finish and must still check epoch.
         * This method must be safe when called from inside the callback.
         */
        @Override
        void close();
    }
}
