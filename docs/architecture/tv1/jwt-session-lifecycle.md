# JWT and Session Lifecycle

**PROPOSED / PENDING APPROVAL** · Owner TV1

[Architecture handoff and source evidence](README.md) · [Security and Auth](security-auth-spec.md) · [App Shell integration](composition-root-integration.md)

## Source-confirmed boundary

Session V2 defines an atomic snapshot with state, UUID only when authenticated, and a local epoch. It defines asynchronous observation and credential callbacks, cancellation, and shared single-flight refresh semantics. AuthRepository issues only a registration challenge, and the launcher ViewModel remains preview. Coordinator at PR #13 is a foundation with FIX1 still open. There is no verified SessionRepository runtime, secure store, real token issuance, refresh/revoke or protected transport from these sources. See E1–E5 and E12 in the [evidence index](README.md#source-evidence-index).

## Proposed token and revoke model

- Access token is a short-lived RS256 JWT with `iss`, `aud`, `sub` (server user UUID), `sid` (server session ID), `iat`, `exp` and optional `jti` for token-level tracing/revocation. A configured `kid` names a trusted signing key. Validate `nbf` if present. Access lifetime 15 minutes and 30 seconds clock tolerance are candidates. The Android epoch and credential revision never serve as server credentials.
- Refresh is a high-entropy opaque token. Store its hash with session/family ID, rotation lineage and state, expiry and revocation time. The proposed maximum absolute family lifetime is 30 days, subject to team approval. Lock the family in a transaction, mark the old token used and write the replacement. Retain the replaced hash long enough to detect reuse through policy expiry. Confirmed reuse revokes the family and signals relevant connections.
- An ambiguous refresh response can occur after server commit. Do not retry consumed credentials blindly. The simple initial policy is reauthentication after definitive rejection. Document the transport retry contract before offering recovery of an ambiguous rotation response. Prove refresh races and reuse outcomes in PostgreSQL integration tests. [RFC 9700](https://www.rfc-editor.org/rfc/rfc9700.html)

| Mode | Logout and residual access |
| --- | --- |
| Stateless access JWT, revoke refresh only | Previously issued JWTs remain acceptable through `exp + skew`. Near a fresh issue that can be approximately 15 minutes 30 seconds after logout under the candidates. Independently enforce socket expiry. |
| JWT + authoritative `sid` lookup at protected admission, recommended | After revocation commit, new request/handshake admission with that `sid` is denied. In-flight work admitted earlier may finish. Open connections require explicit revoke handling and delivery rechecks. A cached revocation view requires a documented bound on propagation delay. |
| Offline logout | Local gate and credentials are cleared immediately. Server revoke is only confirmed after server response or verified state. A stolen valid refresh token may remain able to mint access before server revocation or family expiry. Do not keep a plaintext credential to retry logout indefinitely. |

## Android credential storage

TV1 proposes access token in process memory and refresh ciphertext in `noBackupFilesDir`. Use Android Keystore AES-GCM with a fresh nonce per record, atomic writes, and minimal session metadata. Avoid plaintext SharedPreferences, Bundle, UI state, logs and analytics. Current minSdk is 26. Manifest enables backup and the rules are still placeholders. Test both cloud backup and device transfer rules on both API generations before saving any credential. `EncryptedSharedPreferences` is deprecated. Prefer direct Keystore plus an app-private encrypted file over introducing that API. [Keystore](https://developer.android.com/privacy-and-security/keystore) · [Backup rules](https://developer.android.com/identity/data/autobackup) · [Deprecated API](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences)

On corrupted ciphertext, key invalidation or failed replacement write, fail closed and return to a recoverable sign-in flow. Process death discards memory access token and allocates a fresh epoch. Restore reads storage on a worker and validates or refreshes against the server before enabling protected access. Do not use cached identity to authorize. Account switch and logout clear memory, delete previous persistent credential records and prevent late writes in the old epoch. Test atomic replacement, interrupted restore, corruption and logout during rotation.

## Session V2 handoff and transitions

Auth adapter receives a verified result and revalidates the current Auth attempt before handing it to a TV1-internal SessionRepository runtime entry point. Runtime owns credential persistence and acceptance, publishes a new authenticated snapshot and epoch, and remains the single source of truth for active identity. Auth UI renders presentation state and does not publish an independent authenticated flag.

| Trigger | Required behavior |
| --- | --- |
| Start/restore | New epoch and CHECKING, no protected UI. Only a reviewed server result can publish authenticated state. |
| Credential acquire/refresh | One refresh job per epoch across the repository and provider. A newer credential revision satisfies rejected-credential waiters without starting duplicate refresh. |
| Temporary network/revalidation failure | CHECKING or UNAVAILABLE without exposing current user. Keep isolated same-epoch scope, suspend protected admission and UI, and allow bounded retry. |
| Successful same-epoch refresh | Retain account UUID/epoch and dependencies. Rotate credential revision and release eligible waiters according to the validated snapshot. |
| Definitive reject/logout/account switch | Establish invalidation before new work can be admitted, cancel queued work and dispose or quarantine old scope. No stale refresh or credential callback can restore it. New login creates a fresh epoch even for the same user. |
| Process death | Fresh local epoch and fresh verification. Never restore a saved protected destination before session and account checks. |

## REST and WebSocket boundary

1. A request calls `CredentialProvider.acquire(expectedEpoch, executor, callback)` for a matching UUID and epoch. Check the latest snapshot and a shared admission gate immediately before tracked enqueue or handshake. Do not hold a gate lock during network I/O. Scope invalidation must reject new admissions, cancel admitted work where possible, and suppress late UI mutation.
2. On HTTP 401, pass the exact rejected credential to `afterAuthenticationRejected`. Reuse the newer revision or join the epoch's refresh job. Allow one replay only when safe, preserving keys for idempotent writes. HTTP 403 and policy-concealed 404 are domain results and never automatically refresh.
3. Acquire credentials before connecting WSS. The proposed Android path sends Bearer on HTTP upgrade. Do not put credentials in the URL. Bind trusted principal and `sid` at the server. Enforce access expiry for open sockets. Reconnect with bounded backoff, newly admitted credentials and an authorized replay cursor. If STOMP is adopted, choose one explicit auth point and enforce authorization at subscribe, send and delivery. [Spring WebSocket security](https://docs.spring.io/spring-security/reference/servlet/integrations/websocket.html)
4. Session revocation closes whole-session subscriptions/connections. Group membership revocation removes access only to that conversation and blocks new messages, history, files and queued delivery. Server authorization is authoritative. A cancellation request, callback completion and actual physical termination are different observations. Test late callbacks, HTTP/WS revoke races and preservation of idempotency after timeout.

Coordinator state is a local safety boundary. It does not demonstrate that TV2 or TV3 actually checked a credential at dispatch. Before production wiring each transport adapter must prove admission, cancellation ownership and guards at result application. See E2, E7–E10 in the [evidence index](README.md#source-evidence-index).
