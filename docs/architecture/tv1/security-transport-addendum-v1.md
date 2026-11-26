# Security and Transport Contract Addendum V1

**PROPOSED / PENDING APPROVAL** · TV1 owns shared Auth, Session and transport infrastructure

[Architecture handoff](README.md) · [Security and Auth](security-auth-spec.md) · [Session lifecycle](jwt-session-lifecycle.md) · [FIX1 design](coordinator-fix1-design.md) · [Host integration](composition-root-integration.md) · [Decision register](cross-team-decision-register.md)

## Baseline and review evidence

This addendum answers the six TV2 questions relayed for Architecture Review 03. TV2's reported support for shared Auth/Security/Session, FIX1, account-scoped Messaging and Host Integration is **AGREED DIRECTION**. Foundation stabilization has conditional TV2 support and still requires TV3 confirmation. At the preflight snapshot, #16 was Draft with TV2 and TV3 requested and no formal reviews or discussion comments on GitHub. These are separate approval channels. None freezes the wire, exception policy or transport.

The main baseline and Session/Auth source are at `8b730dccc171c6d55fd8ae127448ed2b6c5f23a4`. The prior #16 HEAD is `7d75bf8b919899f6487e6b8ed0b8e27a34772c11`. PR #13 at `1c459c517cbfea06d1c071554a05c894157e157e` still has changes requested and FIX1 remains design only. #14 at `b448991c3176618cdb8eeed46ebacb38693d7dc8` and #15 at `f3456179e01ec4d49c5088da07fd12f061a25910` are Draft and have separate approval records at their current HEADs. #15 remains test-only. None of these PRs was merged as part of this addendum.

| Source | Confirmed behavior and limits |
| --- | --- |
| Session V2 and Auth at `8b730dccc171c6d55fd8ae127448ed2b6c5f23a4` | A session snapshot publishes authoritative state, user UUID and local epoch together. CredentialProvider is an asynchronous interface. AuthRepository starts a registration challenge and AuthViewModel gates by attempt. A challenge never grants session. [Repository](https://github.com/datcaq/NEXA-Chat/blob/8b730dccc171c6d55fd8ae127448ed2b6c5f23a4/app/src/main/java/com/nexa/chat/feature/auth/session/SessionRepository.java#L10-L79) · [Snapshot](https://github.com/datcaq/NEXA-Chat/blob/8b730dccc171c6d55fd8ae127448ed2b6c5f23a4/app/src/main/java/com/nexa/chat/feature/auth/session/SessionSnapshot.java#L20-L49) · [Access policy](https://github.com/datcaq/NEXA-Chat/blob/8b730dccc171c6d55fd8ae127448ed2b6c5f23a4/app/src/main/java/com/nexa/chat/feature/auth/session/SessionAccessPolicy.java#L14-L30) · [CredentialProvider](https://github.com/datcaq/NEXA-Chat/blob/8b730dccc171c6d55fd8ae127448ed2b6c5f23a4/app/src/main/java/com/nexa/chat/feature/auth/session/CredentialProvider.java#L10-L83) · [AuthRepository](https://github.com/datcaq/NEXA-Chat/blob/8b730dccc171c6d55fd8ae127448ed2b6c5f23a4/app/src/main/java/com/nexa/chat/feature/auth/AuthRepository.java#L5-L40) · [AuthViewModel](https://github.com/datcaq/NEXA-Chat/blob/8b730dccc171c6d55fd8ae127448ed2b6c5f23a4/app/src/main/java/com/nexa/chat/feature/auth/AuthViewModel.java#L109-L225) |
| Coordinator #13 at `1c459c517cbfea06d1c071554a05c894157e157e` | Reconciles the latest snapshot and checks matching account scope. Resource hooks can throw into the observer path. Preparation and destination do not prove REST/WS admission. [Coordinator](https://github.com/datcaq/NEXA-Chat/blob/1c459c517cbfea06d1c071554a05c894157e157e/app/src/main/java/com/nexa/chat/core/session/SessionCoordinator.java#L52-L166) · [TV2-R13-01](https://github.com/datcaq/NEXA-Chat/pull/13#pullrequestreview-6050049996) |
| Messaging #9 at `ea239b9f66a153ac9f848fd089e5d4dd8b8874e4` | Account lease and bounded work, logical invalidation and route intent. View activation depends on selection/resume. Existing completion-stage accounting does not prove physical cancellation or server rollback. [Execution](https://github.com/datcaq/NEXA-Chat/blob/ea239b9f66a153ac9f848fd089e5d4dd8b8874e4/app/src/main/java/com/nexa/chat/feature/messaging/ui/MessagingExecution.java#L20-L57) · [Controller invalidation](https://github.com/datcaq/NEXA-Chat/blob/ea239b9f66a153ac9f848fd089e5d4dd8b8874e4/app/src/main/java/com/nexa/chat/feature/messaging/ui/MessagingController.java#L365-L400) · [Router](https://github.com/datcaq/NEXA-Chat/blob/ea239b9f66a153ac9f848fd089e5d4dd8b8874e4/app/src/main/java/com/nexa/chat/feature/messaging/ui/MessagingRouter.java#L35-L95) · [View](https://github.com/datcaq/NEXA-Chat/blob/ea239b9f66a153ac9f848fd089e5d4dd8b8874e4/app/src/main/java/com/nexa/chat/feature/messaging/ui/MessagingView.java#L46-L76) · [TV2 decisions](https://github.com/datcaq/NEXA-Chat/blob/ea239b9f66a153ac9f848fd089e5d4dd8b8874e4/app/src/main/java/com/nexa/chat/feature/messaging/DAY1_DECISIONS.md#L17-L80) |
| Groups Android #6 at `45c90a7e5be28332ace723ba9789af01a52fd9c7` | Bounded scope/Retrofit request ownership, logical invalidation and asynchronous cleanup. STARTED records an enqueue claim. TERMINATED needs a terminal callback or proof of no enqueue. SETTLED also accounts for owned tasks. No shared credential implementation is verified by this branch. [Integration](https://github.com/datcaq/NEXA-Chat/blob/45c90a7e5be28332ace723ba9789af01a52fd9c7/app/src/main/java/com/nexa/chat/feature/groups/GroupsIntegration.java#L51-L80) · [Scope dispatch](https://github.com/datcaq/NEXA-Chat/blob/45c90a7e5be28332ace723ba9789af01a52fd9c7/app/src/main/java/com/nexa/chat/feature/groups/GroupsAccountScope.java#L104) · [Retrofit routes](https://github.com/datcaq/NEXA-Chat/blob/45c90a7e5be28332ace723ba9789af01a52fd9c7/app/src/main/java/com/nexa/chat/feature/groups/data/GroupApi.java#L7-L29) · [Repository](https://github.com/datcaq/NEXA-Chat/blob/45c90a7e5be28332ace723ba9789af01a52fd9c7/app/src/main/java/com/nexa/chat/feature/groups/data/RemoteGroupRepository.java#L85-L169) · [Request handle](https://github.com/datcaq/NEXA-Chat/blob/45c90a7e5be28332ace723ba9789af01a52fd9c7/app/src/main/java/com/nexa/chat/feature/groups/data/GroupRequestHandle.java#L11-L22) · [Runtime capacity](https://github.com/datcaq/NEXA-Chat/blob/45c90a7e5be28332ace723ba9789af01a52fd9c7/app/src/main/java/com/nexa/chat/feature/groups/data/GroupRequestRuntime.java#L26-L46) |
| Groups backend #6 at the same `45c90a7e5be28332ace723ba9789af01a52fd9c7` | Current filter authenticates all requests and consumes issuer-configured JWT. Controller parses UUID subject. GroupErrors and filter responses use separate paths. No shared issuer/session authority or real WebSocket runtime is established. [Security](https://github.com/datcaq/NEXA-Chat/blob/45c90a7e5be28332ace723ba9789af01a52fd9c7/backend/src/main/java/com/nexa/chat/backend/groups/GroupConfiguration.java#L34-L74) · [Principal](https://github.com/datcaq/NEXA-Chat/blob/45c90a7e5be28332ace723ba9789af01a52fd9c7/backend/src/main/java/com/nexa/chat/backend/groups/GroupController.java#L21-L34) · [Error advice](https://github.com/datcaq/NEXA-Chat/blob/45c90a7e5be28332ace723ba9789af01a52fd9c7/backend/src/main/java/com/nexa/chat/backend/groups/GroupErrors.java#L11-L43) · [Request limit](https://github.com/datcaq/NEXA-Chat/blob/45c90a7e5be28332ace723ba9789af01a52fd9c7/backend/src/main/java/com/nexa/chat/backend/groups/GroupRequestLimit.java#L13-L37) · [GROUP-01 rules](https://github.com/datcaq/NEXA-Chat/blob/45c90a7e5be28332ace723ba9789af01a52fd9c7/backend/src/main/java/com/nexa/chat/backend/groups/GroupService.java#L182-L208) |

## A. WSS handshake authentication

**PROPOSED — R03, R18, R20.** Android acquires a matching credential through Session V2 and sets `Authorization: Bearer …` on the HTTPS upgrade to WSS. The URL never carries the token. Header/body logging, traces, crash reports and reverse-proxy access logs must redact credentials. Do not route protected upgrade requests through a permissive fallback or retry to a different origin with the same credential.

1. Validate signature with configured trusted keys and algorithm. Require expected issuer, audience, expiration, canonical server user UUID in `sub`, and server session UUID in `sid`. Validate time claims and consult the authoritative active session record before admitting a protected upgrade. Construct an internal trusted principal with user UUID, `sid` and effective credential expiry. These values come from the validated credential and authority, never from client payload identity or local epoch.
2. Register a unique server-generated connection ID bound to that principal. Register and recheck the session so revoke cannot miss a handshake in progress. Before upgrade, missing, invalid, expired or revoked proof returns a generic 401. A valid session disallowed from the endpoint returns 403. An unavailable authority fails closed with a service/transport failure. Protected admission cannot be based on an `Origin` header or possession of a connection ID.
3. An authenticated connection establishes identity only. Server authorizes inbound commands, send, subscribe and resubscribe against current session and conversation ACL. Check again at protected outbound delivery or replay, including queued fan-out. Unsubscribe, loss of membership and block/privacy changes follow domain policy. Control traffic cannot grant or preserve protected access.

The proposed entry is HTTP-upgrade authentication for Android. **OPEN / BLOCKING for production WS:** TV2 must specify raw WebSocket versus STOMP, endpoint, payloads and reconnect mapping. If STOMP is selected, authentication order and principal propagation from the handshake to CONNECT, frame authorization and outbound broker delivery need explicit tests. This proposal does not define a message envelope, close code or endpoint.

## B. Active WebSocket expiry and reconnect

**PROPOSED — R05, R18–R19.** Establish a single effective authorization cutoff at admission. Candidate is no protected delivery at signed `exp`. Any JWT clock tolerance used for validation must be reconciled with the socket cutoff and explicitly tested. Schedule close on expiry but also check authority and expiry on every protected send, subscribe and delivery because timers and asynchronous queues may run late. Clear unsafe queued delivery, invalidate the connection generation and close according to TV2's agreed policy. Never refresh a live connection in place under a different `sid` or user.

| Condition | Server boundary | Android action |
| --- | --- | --- |
| Network unavailable or ambiguous disconnect | Connection/authority status may be unknown. No successful auth inference. | Mark transport unavailable, suppress protected dispatch while validity is unknown and retry with bounded jittered reconnect if current scope permits. Keep same epoch during transient failure. |
| Credential expiry or authenticated transport rejection eligible for refresh | Stop protected traffic and close the expired connection, reject new admission with the old revision. | Call `afterAuthenticationRejected` for the exact rejected credential when rejection is known. For a local expiry prediction or ordinary reconnect, use `acquire` to obtain a usable revision. Refresh within the same valid account does not create a new epoch. |
| Definitive authentication rejection or revoked session | Retire `sid` traffic. No protected delivery. | If definitively confirmed by the authority, retire local scope and render Auth. No retry loop with an expired scope. |
| Membership revoked | Remove that conversation from the connection's subscriptions and replay, with session retained if otherwise valid. | Apply authorized domain result. No credential refresh. |

After authenticated reconnect, TV2 owns replay cursor, gap detection, deduplication and resync fallback. Recheck access before each replay batch and at delivery admission. Keep history cursor, replay cursor and read/delivered cursor distinct. Reset retry state on a demonstrably stable connection, not merely on a transient upgrade. Exact retry reset rule, close reason mapping and cursor format remain open.

## C. Session revocation propagation

**PROPOSED — R07, R19.** Revoke active server session and refresh family in an authoritative transaction. The proposal binds protected operation admission and revoke ordering to that authority. Signal active connections only after commit. Prefer reliable commit-coupled signaling and periodic recheck as recovery if notification is lost. In a single deployable the registry can start local. Multiple runtime instances require reliable propagation or an authoritative read with a documented bound. Keep an index by `sid` and by subscription conversation so targeted invalidation does not depend on enumerating untrusted client state.

| Surface | Required ordering |
| --- | --- |
| New HTTP admission and upgrade | If revoke commits first, authoritative check rejects the request. Authorization services recheck at write/commit where a stronger business guarantee is needed. An earlier permitted request can commit or complete. |
| Active connection registration | Track connection identity and generation. Recheck after registration to close the admission/registration race. After revoke, fence admission and then request close. |
| Inbound send or subscribe | Check the principal, current `sid`, expiry and relevant ACL at processing admission, not only at frame receipt. Revalidate authorization at mutation boundary for queued writes when policy requires. |
| Outbound or replay | Check current `sid`, expiry, active subscription and conversation policy at final local send admission. Drop queued work after the gate closes. A frame previously queued to the actual transport or bytes already sent cannot be recalled. |
| Android logout and switch | Local invalidation precedes unmount and cancellation. Offline logout cannot assert server revocation. Clear local credentials and private state, reconcile server revoke only through an approved and protected mechanism. Never retain raw credentials indefinitely to retry logout. |

Record the elapsed time from authoritative commit to denied new admission and to socket quiescence separately. **OPEN / BLOCKING** for any promise of immediate server revoke: TV1 and TV2 must agree lookup consistency, any maximum cache delay, signaling loss handling and retry budget. If authority cannot be verified, suspend protected work and surface unavailable. A socket close request alone neither removes queue races nor proves physical termination. Membership revoke is scoped to one conversation, involves TV3 policy and TV2 delivery checks, and does not revoke other sessions or force token refresh.

## D. Single-flight refresh and bounded replay

**PROPOSED implementation of the existing V2 API — R05–R07, R20–R21.** Preserve `acquire(expectedEpoch, executor, callback)` and `afterAuthenticationRejected(expectedEpoch, rejectedCredential, executor, callback)`. The runtime checks UUID and epoch, then matches the rejected credential's opaque revision. If a newer usable credential already exists in the same scope, return it. Otherwise one refresh job is shared by both SessionRepository and CredentialProvider for that account/epoch. Do not compare opaque revision UUIDs by numeric ordering.

- The callback can be inline. Keep the executor alive, and provide one terminal callback per non-cancelled waiter without blocking or throwing. Cancel only that waiter. Work cancellation is allowed only if safe and unneeded by other consumers. Immediately before callback delivery, recheck epoch and validity. If a callback already began, recheck again at actual dispatch or state mutation.
- Definitive reject invalidates the scope and yields `SCOPE_EXPIRED`. Temporary inability to verify yields `UNAVAILABLE`. `UNAUTHENTICATED` means the expected scope is current but no authenticated session is available. Never interpret HTTP 403 or a concealed domain 404 as credential rejection.
- A logical operation may replay **at most once** after an eligible authentication rejection and fresh admission. No recursive authenticator loop. Read/other replay-safe calls can replay. Writes need the original payload and original idempotency identity, plus server support. Messaging preserves `clientMessageId` and any wire idempotency key, Groups preserves its creation key. Do not convert a network timeout directly into a new write or a new key.
- Refresh uses atomic rotation and replay detection. With timeout after server commit or a lost rotation response, the previous token may already be consumed. No blanket refresh retry of the old token. First resolve using a separately approved recovery protocol or require reauthentication on definitive rejection. A transient network failure does not itself prove revocation. User-visible Messaging mutation timeout is an unknown server result until checked against an authorized authoritative read/ACK or retried under the same approved idempotency identity.

The proposal adds no public Session V2 method or mandatory new credential accessor. Server expiry, session ID and token validation are handled within the transport/runtime implementation or verified server response rather than UI or feature state.

## E. HTTP error and concealment policy

**PROPOSED — R09, R11–R16.** One error renderer receives the same opaque server `requestId` through filter, authentication entry point, controller advice and approved gateway paths. Keep `{code,message,requestId}` required and `details` optional, allowlisted and non-sensitive. Ensure status, code and observable responses do not distinguish nonexistent resources from concealed ones in the same policy. Never reflect account existence, phone, membership or raw provider/resource errors. A successful empty search response can be the deliberate concealment shape for exact lookup.

| Status | Boundary and client action |
| --- | --- |
| 400 | Request schema/validation. Safe field hints only. No auto-refresh. |
| 401 | Missing, invalid, expired or revoked auth proof. Protected client may enter the bounded refresh path for an eligible request. Public Auth errors use route-specific generic semantics. |
| 403 | Valid auth but operation denied where disclosure is permitted. No refresh. |
| 404 | Missing or concealed resource under the applicable policy. Same public code and message for both. No refresh. |
| 409 | Authorized domain conflict, stale precondition or idempotency payload mismatch. Reconcile before retry. |
| 429 | Rate/budget limit. Include `Retry-After` when deterministic and safe, honor it and avoid enumerating target-specific details. |
| 503 | Authority, provider or backend dependency unavailable. Bounded retry only when safe, no credential purge solely from an outage. |

All proposed paths below are boundary examples under `/api/v1`. Labels on #6 routes record branch implementation only. Other status families such as 413/415 and 500 remain valid where defined. The group request limiter already has a separate 413 producer. Current `GroupErrors` maps `GroupProblem`, validation, integrity conflict and unexpected failure. Authentication and body-limit filters produce their own responses. Consolidation must preserve intentional concealment and domain semantics.

| Surface | Route or operation | Authorization and concealment candidate |
| --- | --- | --- |
| Auth | **PROPOSED** challenge/verify, refresh/logout, `/users/me` | Anonymous challenge and verify never disclose existing account; 400/429/503 generic before proof. Refresh uses refresh proof and generic 401. Own profile requires valid session; no arbitrary user ID on self route. |
| Contacts | **PROPOSED** exact E.164 lookup | Authenticated, rate-limited and policy-filtered. Hidden, blocked and nonexistent targets share the same empty response. No raw phone or searchable list in result/logs. |
| Friends | **PROPOSED** request/list/accept/reject | Limit to participants and permitted target. Conceal inaccessible targets/requests, 409 on a known conflict only after entitlement has been checked. |
| DIRECT | **PROPOSED** open/create and send | Trusted actor with friendship/block policy and canonical conversation. Conceal inaccessible peer/conversation unless policy permits generic 403. Preserve original write identity. |
| GROUP | #6 branch `/groups` and `/group-invitations` | Group reads require ACTIVE, invitation actions use recipient/OWNER authority as applicable. Conceal outside group or recipient scope. Authorized already-processed invitation can use 409. |
| History/replay | **PROPOSED** conversation history and resync | Recheck membership and approved visibility floor, including rejoin period. Conceal missing and inaccessible history. Do not reveal hidden cursor ranges. |
| Media | **PROPOSED** upload/download | Require ownership or current conversation visibility. Conceal inaccessible file. Authorized size/type validation can return 413/415. Use separate upload deadlines. |
| Translation | **PROPOSED** translate visible message | Recheck original message visibility and locale/provider policy. Conceal inaccessible original. Provider outage returns safe 503 and preserves original content. |

## F. Shared transport cancellation and timeout

**PROPOSED — R20–R21.** TV1 owns shared HTTP client settings, auth policy, request admission/registration and sanitized diagnostics. Separate Auth/refresh call path from protected requests to avoid callback or interceptor recursion. Share connection infrastructure only where safe. TV2 and TV3 own domain attempts and adapters, enforce account/epoch on callback application and never close a shared dispatcher/pool for one account.

Credential acquisition is not dispatch. Track operation and cancellation ownership before handing off to a worker. Serialize actual enqueue permission with invalidation or fence it through a work item and revalidate at vendor enqueue boundary. Network I/O runs outside locks and off UI. Record at most one client terminal outcome and distinct server-outcome knowledge.

| Lifecycle observation | Meaning and accounting |
| --- | --- |
| ADMITTED | Current account UUID/epoch, credential revision and policy checked. Register cancel handle or owned pending handle before scheduling. Cancellation before enqueue suppresses result and retires transport only once worker ownership proves no enqueue. |
| DISPATCHED | Worker invocation has begun. Track separately whether enqueue permission was claimed and whether transport accepted it. Queued or claimed work is not proof of an on-wire request. Apply latest session/scope gate before protected admission. |
| CANCEL_REQUESTED | Deadline, logout, switch or user cancel closes logical access immediately and requests underlying cancellation asynchronously. If enqueue is racing, retain ownership so a late returned handle is cancelled. No logical effect can be applied to a newer epoch. |
| TERMINATED | A transport terminal callback or equivalent verified never-enqueued/terminated evidence for the owned operation. Exactly-once accounting covers normal completion and late/duplicate callbacks. A completed future alone is not proof of socket or transport termination. Release additional application work reservations only after their own settlement. |

Timeout can choose a client-visible failure before physical termination. Keep retiring work charged with a bounded cleanup/diagnostic policy when termination is unknown. If a write may have reached the server, treat its effect as unknown until reconciled. Neither `Future.cancel`, OkHttp cancellation request, nor a closure callback proves server rollback. Do not block UI awaiting HTTP, WS close, resource disposal or lease retirement.

| Candidate | Value and review gate |
| --- | --- |
| HTTP connect | **10 s** as an attempt bound. Measure DNS, TLS and slow network separately, and keep the total call deadline authoritative. |
| HTTP read / write | **20 s / 20 s** for ordinary calls. Treat as transport I/O bounds, not independent allowances added to the total. Validate slow streams and proxy behavior. |
| Normal HTTP call | **30 s** end to end for the admitted normal operation, including applicable queue/retry budget. Existing Groups owns a 30 s admitted deadline and 5 s shutdown diagnostic. Align deadline start and remaining budget with Groups, avoiding contradictory timers and premature release. TV2 lease budgets/settlement do not yet impose verified wire deadlines. |
| WSS handshake | **10 s** explicit upgrade deadline through authorization. Clear the handshake-only deadline on success or use a WebSocket client path designed for it. The normal HTTP call timeout does not bound the entire WS lifetime. |
| Reconnect | Exponential **1–30 s with jitter**, one attempt active per scope. Ratify a finite attempt/time budget and terminal conditions. Backoff stops for invalidated epochs, confirmed revoke and manual disconnect. Bound and test cancellation of scheduled retries. |
| Media | Separate policy for size, per-transfer limits, progress/cancel and resumable/streaming behavior. Never automatically replay an unknown upload as a new write. TV2 and TV3 review attachment authorization. |

All values above are candidates. Confirm actual OkHttp/Retrofit behavior, deadline measurement and feature acceptance on representative networks before freezing them.

## Cross-contract alignment and decision gates

**NOT AVAILABLE** at this snapshot: full revised Messaging Wire Contract V1 and the 30-decision TV2 register on the GitHub refs, PRs and review artifacts inspected. The #9 draft and source support a bounded alignment review. No statement about the contents of the reported 21 OPEN or 9 CONFLICT decisions is inferred. R01–R25 below are existing TV1 IDs. R26 remains the separate Foundation release gate in the [decision register](cross-team-decision-register.md).

| Existing decisions | Alignment and review gate |
| --- | --- |
| R01–R04, R10 | **AGREED DIRECTION** for shared authority/UUID, **PROPOSED / BLOCKING** for Auth method, provider, issuer/audience, signing and principal claims. Exact OTP policy and production delivery/cost remain open. |
| R05–R08, R18–R21 | **PROPOSED / BLOCKING** for production transport. TV2/TV3 confirm expiry, revocation ordering, idempotency/replay, account admission/cancellation and actual timeouts. Existing CredentialProvider signatures stay unchanged. |
| R09, R11–R17 | **OPEN / BLOCKING** for relevant production domain wire. Ratify consented error/concealment, DIRECT eligibility, group history, membership, cursor and policy differences. **CONFLICT** is reserved for a verified incompatible pair of decisions. None can be classified against the missing revised register. One current design gap is separate Groups error producers versus the proposed shared renderer. |
| R22–R23 | **AGREED DIRECTION** for owner and account/view boundaries. **OPEN / BLOCKING** for production host signatures and observable admission. Foundation code can continue as **NON-BLOCKING** under separate authorization. |
| R24–R25 | **OPEN / BLOCKING** for shared schema/backend rollout. Verify V1 lineage separately. **NON-BLOCKING** for this documentation and independent stabilization planning. |

Review questions for TV2: Confirm raw WebSocket versus STOMP, the planned upgrade and failure semantics, replay cursor ownership and stable message idempotency. Can the transport report enqueue, terminal callback and cancellation separately under a bounded lease? Supply the revised wire and decision register by exact ref.

Review questions for TV3: Confirm ACTIVE membership revoke ordering for send/replay/media and concealment behavior. Confirm current Groups request accounting can adopt the shared epoch/credential gate without double timeout, shared pool disposal or replacing TERMINATED/SETTLED with a single flag. Confirm the separate Foundation stabilization direction.

## Acceptance and test matrix

Design only. No test source or production implementation is delivered here. When implemented, use test-only credentials/fixtures and inspect server traces with redaction. Distinguish local UI assertions, actual transport observations and authoritative database outcomes.

| Test | Setup and trigger | Required observation |
| --- | --- | --- |
| T01 Upgrade proof | Missing credential, malformed proof, expired or revoked credential | No protected connection ID or subscribe/delivery, generic pre-upgrade 401. |
| T02 Issuer/audience | Otherwise valid proof with mismatched issuer or audience, absent required claim | Denied before upgrade and before HTTP protected admission, no auth payload in error. |
| T03 Active expiry | Delay timer while expiry passes and inbound/outbound work is queued | Cursors and protected delivery stop at cutoff. Close requested, no implicit authorization on reconnect. |
| T04 Active session revoke | Revoke while active and race new upgrade, sends and delivery | New HTTP/WS admission denied after authoritative commit. Existing queued work fenced at final admission. Verify connection signaling latency and unavailable-authority behavior. |
| T05 Membership revoke | Remove ACTIVE member with replay/send queued | Conversation traffic and attachments denied, unaffected authorized conversations still work. No refresh. |
| T06 Concurrent 401 | Messaging and Groups fail with same revision and account epoch | Exactly one refresh flight. Older rejected revision uses newer available revision. Different epoch cannot join. |
| T07 Rotation lost response | Commit refresh rotation and drop response | Account cannot blindly reuse a consumed token, follow approved recovery or definitive reauth. No stale credential becomes current. |
| T08 Cancel waiter | Two waiters share refresh. Cancel one before queued delivery. | No cancelled waiter callback. Other waiter completes. Callback already running still checks scope. |
| T09 Replay bound | Repeated 401 and write with preserved original identity | At most one wire replay after eligible rejection. ClientMessageId/key and payload unchanged. Non-replayable write does not retry. |
| T10 Authorization/concealment | Protected request receives 403 or concealed 404 | No refresh or replay. Missing and concealed responses do not leak resource existence. |
| T11 Timeout after write commit | Server accepts an idempotent write and response is dropped | UI marks outcome unknown and reconciles. No duplicate record. Timeout/cancellation does not report rollback. |
| T12 Offline logout | Active access and refresh retained prior to loss of network | Local Home/credentials close immediately. Server revoke status remains unconfirmed and UI does not present it as done. |
| T13 Switch while in-flight | A owns HTTP/WS and changes to B before response | Block new A dispatch, request A cancellation and reject all late A mutations in B. A response may still reflect server completion. |
| T14 Stale callback | Old epoch callback was already queued or running | Recheck epoch at actual transport admission and result application. No stale success or reopening old scope. |
| T15 Termination accounting | Normal success, pre-enqueue cancel, post-dispatch cancel, duplicate callback, delayed/no terminal event | Release each reservation once on appropriate evidence. Unknown transport remains charged. No dispatcher shutdown or blocking UI close. |
| T16 Reconnect budget | Repeated network failure, then expiry, then definitive revoke | One reconnect flight with bounded jitter/backoff. Respect stop conditions and resume authorized sync only for current epoch. |
| T17 Credential redaction | Exercise HTTP, upgrade, refresh failure, retries and crash-path logging with fixture credentials | No raw headers, tokens, phone or sensitive payload in logs, analytics, URL or user-visible errors. |
