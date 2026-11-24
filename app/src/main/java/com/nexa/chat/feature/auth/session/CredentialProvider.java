package com.nexa.chat.feature.auth.session;

import java.util.UUID;
import java.util.concurrent.Executor;

/**
 * Proposed asynchronous boundary for shared REST/WebSocket credentials, pending team review.
 * No implementation, credential storage, token parsing or refresh network logic is provided.
 *
 * <p>Implementations are thread-safe and share the SessionRepository's scope and refresh coordinator.
 * Null arguments are rejected synchronously. Methods return promptly; network work never blocks
 * the caller. For each non-cancelled request, invoke exactly one terminal callback on the supplied
 * executor. A direct executor may deliver inline. Callbacks must not block or throw; callers keep
 * the executor accepting work until completion or cancellation.
 *
 * <p>Credential delivery must verify current epoch and authoritative validity immediately before
 * callback execution. Logout/account change invalidates pending and queued success, reporting
 * SCOPE_EXPIRED unless the caller cancelled. A callback already running must check the latest
 * session snapshot before transport dispatch or applying results.
 */
public interface CredentialProvider {
    /** Obtain a usable credential for this scope, joining any required single-flight refresh. */
    Request acquire(UUID expectedEpoch, Executor callbackExecutor, Callback callback);

    /**
     * Report an authentication rejection for exactly the credential used by a transport.
     * Rejected credential and expectedEpoch must belong to the same scope.
     *
     * <p>If a newer credential already exists in the current epoch, return it without another
     * refresh. Otherwise all requests share one pending refresh for that epoch with SessionRepository.
     * Definitive rejection invalidates the scope and reports SCOPE_EXPIRED; inability to
     * verify reports UNAVAILABLE. An invalidated scope always reports SCOPE_EXPIRED.
     *
     * <p>The transport limits replay/reconnect attempts and preserves operation idempotency.
     * Permission errors such as HTTP 403 must not trigger this method automatically.
     */
    Request afterAuthenticationRejected(
            UUID expectedEpoch, Credential rejectedCredential,
            Executor callbackExecutor, Callback callback);

    /**
     * Transport-only credential bound to an authenticated identity, epoch and opaque revision.
     * All accessors return non-null values; the identity and epoch match the validated scope.
     * Implementations are immutable. Never log, persist in UI state, or include the authorization
     * value in toString/errors. Issuer, audience and expiration are server-auth integration concerns.
     */
    interface Credential {
        UUID getCurrentUserId();

        UUID getSessionEpoch();

        /** Fresh local identifier for each credential version; independent of the session epoch. */
        UUID getCredentialId();

        /** Complete Authorization header value for the approved transport authentication scheme. */
        String authorizationHeaderValue();
    }

    /**
     * UNAUTHENTICATED means the expected epoch is current but has no authenticated session.
     * SCOPE_EXPIRED means it was replaced/invalidated, including definitive refresh rejection.
     * UNAVAILABLE means validity could not be established (for example a transport outage).
     */
    enum Failure {
        UNAUTHENTICATED,
        SCOPE_EXPIRED,
        UNAVAILABLE
    }

    interface Callback {
        void onCredential(Credential credential);

        void onFailure(Failure failure);
    }

    interface Request {
        /**
         * Thread-safe, idempotent cancellation of this waiter. Suppress callbacks that have not
         * begun, including queued delivery. A running callback may finish. Cancel underlying work
         * when safe and unneeded; cancelling one waiter must not cancel other single-flight waiters.
         * Cancellation does not imply that a server operation has been rolled back.
         */
        void cancel();
    }
}
