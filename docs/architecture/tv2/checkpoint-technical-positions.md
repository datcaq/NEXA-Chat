# Checkpoint Technical Positions — Historical Export

> **Local publication candidate — identified public-safe derivative; owner clearance and final publication authorization pending.**
> Source P is a historical snapshot. Source measurements identify the internal original, not these derivative bytes. See [README](README.md) and [artifact manifest](artifact-manifest.md). Historical findings/status statements below remain dated evidence; later status is separately described in README. This is a derivative of an exported conversation response, including its progress statement and four technical cards; it was not an original directly generated Markdown artifact.

Historical exported progress statement (summary): FIX3 requires shared authority boundaries for sends, membership changes and block/revoke writers; an external version check is insufficient. TV2 recommended ordered database authority locking with transactional outbox for mutation ordering and separate final-response/fanout admission approval. Outbox alone does not prove revocation safety. The checkpoint candidates draw on FIX3/B/C, require affected-owner sign-off and preserve register statuses and resolved FIX3 guarantees.

## Card 1 — D09/D13: Message sequence and membership periods
### 1. Recommended alternative
Use a transactional per-conversation message counter held through commit, with membership join/leave/remove serialized against the same conversation authority boundary.
Recommend:
•
Join captures finalized committed message watermark J; latest-period history begins at J+1, inclusive.
•
Leave/remove captures finalized watermark L; the closed period’s historical interval ends at L, inclusive.
•
Stable membership identity survives rejoin; each accepted rejoin creates a fresh, nonreused period.
•
Message order, durable event order and per-account event order remain separate domains.
This develops the existing FIX3 sequence candidate; it does not select approved storage structures.
### 2. Why TV2 recommends it
A counter serialized through commit makes a finalized watermark understandable: a lower allocated message cannot commit later behind an already published higher boundary. Sharing that boundary with membership transitions gives concurrent join/send and remove/send an explicit order.
A duplicate committed retry returns existing authorized evidence without allocating another message sequence, event, attachment binding or membership period.
### 3. Required invariants
serverSequence orders newly created messages. Edit, recall, receipts and authority mutations use durable event ordering without changing the message’s original sequence. Event coverage cannot be calculated from message sequence; timestamps cannot establish commit order.
For a period, eligible history is the approved interval intersected with actual committed message/tombstone identities and current disclosure policy. Clients must not wait for every integer or interpret numeric gaps as visible messages.
An active period may have a future-open floor with no eligible messages yet. A closed period where L < J+1 has empty history: represent that explicitly, without emitting a reversed interval or treating an empty interval set as unrestricted history.
GROUP requester access remains current ACTIVE-only. A closed interval is audit/history information, not permission for an inactive requester. Rejoin starts independent receipt evidence at the FIX3 zero sentinel; never initialize receipts to J or carry a previous-period maximum. Period/history revision fences apply to history, receipts, replay, media and retained projections. Former-subject receipt visibility remains D10 policy.
DIRECT period/history semantics require separate TV1/TV2 selection; GROUP latest-period policy does not automatically govern retained DIRECT history.
### 4. Adversarial races
•
Join versus send: send commits first and contributes to J, so it precedes the new floor. Join wins first and the subsequent message is eligible. No timestamp-based ambiguity.
•
Remove versus send: removal wins first and the send is denied. Send wins first and may commit within the closing interval; later delivery still obeys current authorization.
•
Duplicate retry versus new send: duplicate returns its existing authorized result; only the genuinely new message advances message order.
•
Rejoin versus delayed receipt: an old-period receipt cannot update the fresh period, even when numeric cursors coincide.
•
Allocation rollback: failed creation publishes no committed message, ACK or event; the advertised watermark remains safe.
### 5. Trade-offs
Conversation serialization limits hot-conversation throughput and increases contention during membership transitions. It is simpler to verify than preallocated sequences plus commit-range tracking. Large GROUP workloads still require bounded contention and fanout design.
### 6. Questions for TV1
Confirm retained DIRECT history, block/unfriend effects and any exceptional old-period disclosure. Confirm that authorship alone cannot bypass the full-ACK/history gate.
### 7. Questions for TV3
Select J+1/L inclusivity, empty-period representation, rejoin eligibility, exceptions and conservative legacy backfill. Confirm that role changes do not create periods.
### 8. Conditions for scoped approval
Approve interval algebra, serialization point, revision publication, empty-period behavior and DIRECT exclusions before dependent schema/query work. D10 audience and D25 acceptance integration must remain explicit dependencies.
### 9. Later implementation evidence
Deterministic PostgreSQL transactions with barriers and bounded waits for both race orders, rollback and concurrent retries. Assert committed messages, counters, periods and events—not merely HTTP outcomes. Add history/replay/receipt tests for empty periods, rejoin, stale tokens and overflow.
## Card 2 — D05/D26: Canonical DIRECT and Message Requests
### 1. Recommended alternative
Choose alternative C: a separate Message Request resource, no introductory message in initial scope, and explicit recipient acceptance followed by current authorization verification and canonical DIRECT creation/opening.
Chat consent and friendship are separate facts. Acceptance does not automatically create friendship.
Use one canonical unordered pair of server UUIDs, with database-enforced uniqueness and one shared comparator.
### 2. Why TV2 recommends it
This keeps pending contact consent outside ordinary Messaging history, receipts, media and replay. It avoids creating a restricted conversation whose existence might accidentally be interpreted as full authority.
Canonical pair identity prevents opposite-direction actions or later reauthorization from creating duplicate DIRECT conversations.
### 3. Required invariants
Before accepted authorization, no ordinary Message creation, Media, READ, TYPING or full DIRECT access. A local navigation command or existing conversation UUID never establishes server permission.
Recommend one effective pending Message Request per unordered pair, retaining sender/recipient direction. A crossed submission must not auto-accept: reconcile to an authorized existing request and require an explicit acceptance action by its recipient. Exact response/conflict semantics remain an owner choice. Friend Request identities and transitions remain separate.
Acceptance should atomically validate the request and current account/block/consent facts, establish the selected chat grant, create-or-get the canonical conversation and required participation facts, terminalize the request, and persist durable publication intent. Existing conversation history must not become visible merely because the pair is reused.
Request operations need their own approved idempotency identity; do not reuse clientMessageId by assumption.
Recommend treating explicit chat-consent revocation as a separate transition. Unblock should not automatically restore consent or friendship. Unfriend’s effect on independent chat consent remains a TV1 policy decision. Existing authorized history requires its own current disclosure policy.
### 4. Adversarial races
•
Opposite-direction requests: one effective pending request; no automatic friendship or chat grant.
•
Duplicate accepts: one grant and one canonical conversation; retries observe an authorized selected result without repeating effects.
•
Block versus accept: block first prevents grant/create. Accept first may commit, then block prevents subsequent interaction and fences pending delivery.
•
Accept versus cancel/decline: one serialized terminal outcome; a stale action cannot reverse it.
•
Lost acceptance response: committed acceptance remains committed. Reconcile under the original operation identity and current authorization; never create another request or conversation blindly.
•
Existing conversation versus new consent: reauthorization cannot expose previously unauthorized history or duplicate the canonical pair.
### 5. Trade-offs
A separate resource introduces request lifecycle, persistence, inbox and notification work. Omitting intro content reduces initial validation, moderation and retention complexity. Independent consent/friendship is clearer but requires explicit revoke/unfriend behavior.
### 6. Questions for TV1
Select consent duration/revocation, unfriend/unblock behavior, request cancellation/reissue, spam limits, retention and reporting. Confirm the crossed-request rule and whether existing accepted consent suppresses further requests.
### 7. Questions for TV3
Confirm request/GroupInvitation separation, no GROUP grants, shared participation compatibility and acceptance publication requirements. Confirm whether the proposed C/no-intro scope is acceptable; its full adoption is not inferred from the supplement.
### 8. Conditions for scoped approval
All affected owners select the resource model, authority/state transitions, crossed-request behavior, transaction boundary, history entitlement and unknown-commit response policy. No endpoints, enums or DTOs are approved through this card.
### 9. Later implementation evidence
Multi-instance PostgreSQL tests for pair uniqueness, opposite-direction operations, acceptance/block/cancel races, rollback and lost responses. Assert no half-grant/membership, no pre-accept Messaging privileges, authorized rereads and suppressed notifications after revocation.
## Card 3 — D14/D20: Idempotency and unknown commit
### 1. Recommended alternative
Preserve FIX3’s account-scoped key (authenticated sender UUID, clientMessageId), immutable frozen request identity and original membership-period binding.
Use transactional server uniqueness. Reconcile uncertain outcomes using the same key and frozen request, subject to current authorization and the complete disclosure gate.
### 2. Why TV2 recommends it
Transport retries cannot reliably determine whether the first transaction committed. Stable identity prevents duplicate messages, while frozen equality distinguishes replay from a changed operation.
Separating immutable creation evidence from current content prevents retries from resurrecting edited or recalled text.
### 3. Required invariants
Freeze the original schema, conversation path and prescribed payload fields, preserving body code points, attachment order and reply target. Server timestamps, token, local epoch and current content do not redefine frozen equality. A digest may assist lookup; it does not replace exact equality under the existing policy.
A committed, identical and fully disclosable retry returns the same immutable creation ACK plus current authorized projection. It allocates no new sequence/event/binding and does not rerun new-send-only READY, quota, rollout or elapsed-window checks. Current authorization, safe schema handling and disclosure remain mandatory.
Retain originMembershipPeriodId across refresh, rotation and recovery. It is provenance/precondition, not authority. Never rewrite P1 pending work into P2.
Preserve FIX3’s canonical concealed denial for hidden committed P1 and absent/uncommitted retired-P1 requests: equivalent semantic status/body/header/recovery behavior, no ACK/conflict/reconciliation hint, no SENT or rollback inference, no refresh triggered by404 and no automatic rekey.
Recall/tombstone and current-version floors dominate stale ACKs, pages and replay. A creation ACK does not restore original content. Purge cannot free an identity for reuse.
### 4. Adversarial races
•
Commit then response loss: same-key retry reconciles one message; timeout remains unknown until authorized committed evidence is available.
•
Two identical sends: one transaction creates; the other returns the same authorized identity without new allocation.
•
Changed payload under the same key: authorized disclosure permits conflict; concealed history must not expose key existence through409.
•
Removal/rejoin after lost ACK: old-period hidden commitment and absent old-period work receive the same concealed profile; neither gains new-period send authority.
•
Recall versus retry: retry returns current tombstone/redaction, never frozen original text.
•
Logout during recovery: old callbacks cannot mutate a new epoch, even for the same UUID.
### 5. Trade-offs
Identity reservation and frozen evidence require retention and purge policy. Concealment can leave the client unable to determine an old operation’s outcome. That uncertainty is preferable to a commitment-existence oracle or silent duplicate send.
### 6. Questions for TV1
Confirm the observable concealment profile, retention/privacy policy, auth-replay eligibility and distinction between definitive credential rejection and unavailable authority.
### 7. Questions for TV3
Confirm period binding and any explicitly approved old-period exceptions. No exception is inferred from authorship, former role or rejoin.
### 8. Conditions for scoped approval
Retain the FIX3 branch and disclosure guarantees, including exact origin binding and permanent identity protection. Any metadata-only reconciliation alternative requires separate privacy/wire approval.
Do not broaden Android automatic retry eligibility or introduce a scheduler through this decision. Auth replay remains separately bounded.
### 9. Later implementation evidence
Concurrent identical/conflicting transactions, response-drop fault injection, media cleanup, recall and old-period paired cases. Inspect committed rows/events/bindings and response semantics. Timing/resource-side-channel resistance requires separate evidence; semantic parity alone does not prove it.
## Card 4 — D08: Shared transaction strategy
### 1. Recommended alternative
Recommend A: ordered database authority locking with transactional outbox as the initial mutation-ordering candidate for one Spring Boot/PostgreSQL backend.
Use one cross-domain transaction for each atomic business operation. Domain ownership remains unchanged; TV3 coordinates the registry administratively.
### 2. Why TV2 recommends it
It provides explicit serialization points using a shared database across backend instances. It fits FIX3’s counter, idempotency and membership model and is easier to validate initially than a fully optimistic cross-domain protocol.
Comparison:
•
A: clearer ordering and rollback behavior; greater contention and potentially expensive GROUP authority sets.
•
B — optimistic versioning: potentially less blocking, but every relevant writer must participate in conflict detection. Independent version checks or preflight reads do not prevent write skew. Requires bounded whole-transaction retries and equivalent publication proof.
•
C — equivalent serialization: acceptable only with demonstrated shared, multi-instance ordering. JVM locks or a single publisher do not supply that guarantee.
FIX3’s existing “strategy B” terminology includes persisted sequencing/fencing; it must not be equated automatically with generic optimistic locking.
### 3. Required invariants
Follow the FIX3 conceptual ordering:
1.
Complete affected-account authority gates, in one canonical UUID order.
2.
Relationship/block authority.
3.
Sender idempotency or canonical DIRECT/request coordination.
4.
Conversations in canonical order.
5.
Membership/period/invitation entities in an agreed fixed rank.
6.
Counters/messages, receipts, attachments and account publication structures in declared order.
These are logical lock categories, not approved tables or DDL.
Preflight discovers candidate roots but cannot authorize commit. Revalidate the complete affected set after locking; if it expands or requires a lower-ranked lock, roll back and restart. Do not optimize GROUP sends to sender-only locking without equivalent authority/fanout proof.
TV1 account/sid revocation and relationship writers, TV3 membership writers and TV2 Messaging writers must participate. Cross-domain services join the same transaction; no independent inner commit or REQUIRES_NEW escape.
Persist domain effects and durable event/publication intent atomically. Publish committed facts asynchronously with duplicate-safe consumers. No OTP/provider/storage/broker/WebSocket network I/O while database locks are held.
Outbox alone does not solve revocation. Final response/replay/media/fanout admission needs an agreed shared revocation fence across instances. Rechecking once and then enqueueing unguarded is insufficient. Do not claim bytes already admitted to transport can be recalled.
### 4. Adversarial races
•
Block/revoke versus send: whichever wins the shared authority boundary determines mutation admission; later response/delivery still checks present disclosure.
•
Membership transition versus allocation: the same ordering determines J/L and whether the message belongs to the period.
•
Authority-set discovery changes: stale preflight must restart, not append out-of-order locks.
•
Deadlock or uniqueness loser: roll back the failed transaction and retry in a fresh usable transaction with the same logical identity and renewed authorization.
•
Commit then publisher crash: durable intent survives; later publication may duplicate delivery attempts but not business effects.
•
Two backend instances: database coordination, not process-local state, determines the winner.
### 5. Trade-offs
Coarse gates create contention and large-GROUP costs. Require bounded group/work sizes, lock waits, transaction duration and retries. B/C remain alternatives if A cannot meet capacity while preserving invariants.
### 6. Questions for TV1
Identify authoritative account/sid/social roots, revoke-completion semantics and every writer that must participate. Select final transport admission consistency and outage behavior.
### 7. Questions for TV3
Confirm membership/invitation lock rank, complete affected-set discovery, maximum supported scope and integration of current invitation-first behavior into the global order.
### 8. Conditions for scoped approval
Approve mutation ordering only with a concrete ownership/transaction manifest. Final admission/publication guarantees need their own explicit design before claiming full revocation compliance. Full ADR D-08, deployed lineage and migration authorization remain separate gates.
### 9. Later implementation evidence
Deterministic PostgreSQL tests with separate transactions, backend identities, barriers, bounded waits and actual blocking observations; both winning schedules; deadlock/retry exhaustion; multi-instance uniqueness; outbox crash/recovery; and measured final-admission revocation behavior.
## One-page meeting summary — decisions TV2 wants
TV2 seeks the following scoped decisions, each recorded with selected alternative, exclusions, conditions, evidence owner and attributable affected-owner confirmations.
1.
D09/D13 ordering: transactional per-conversation counter through commit; duplicate retries allocate nothing; message/event/account sequence domains remain distinct. Select J+1 join floor, inclusive L close ceiling, empty-period representation and fresh rejoin semantics. Keep DIRECT policy separate.
2.
D05/D26 consent and identity: select C with no intro initially; separate chat consent and friendship; no pre-accept ordinary Messaging privileges. Approve canonical unordered UUID-pair uniqueness, explicit crossed-request handling and atomic acceptance/grant/create-or-open.
3.
Social-policy boundaries: TV1 must select consent revocation, unfriend/unblock behavior, spam/reporting and retention. Existing conversation identity must never substitute for current permission or grant unrelated history.
4.
D14/D20 preservation: immutable account-scoped message key, frozen payload and origin period; transactional uniqueness; same-key unknown-commit reconciliation; full current disclosure before ACK/conflict; canonical hidden/absent denial; no rekey, old-period rebinding or tombstone resurrection.
5.
D08 physical strategy: choose A as the initial database mutation candidate, with complete ordered authority participation and transactional outbox. Keep B/C available only on equivalent proof. Separately resolve final response/fanout admission; do not treat an outbox as a revocation barrier.
6.
Owner boundaries: TV1 retains Auth/User/Friend/Security; TV2 Conversation/Message/Sequence/runtime; TV3 Groups/Membership/Backend Foundation and administrative registry coordination. Shared changes require affected-owner approval.
7.
Dependencies retained: D10 audience/denominator, D25 acceptance-to-period, exact DTO/error/cursor mapping, route-specific compatibility, full ADR D-08 and UNKNOWN deployed lineage remain explicit. No new endpoint, schema or migration follows from the meeting.
Design sign-off need not await future runtime execution. It must identify every unresolved baseline choice that blocks dependent implementation and retain later tests as NOT RUN.
The requested outcome is a reviewable design record—not contract freeze, Day1 Exit, Foundation Sync, migration allocation or Day2 activation.
