# Groups contract readiness

26/11/2026. Actual source is PR6 `2e804c033631049683cffd1d7500bb7406658bfa`, not main. Sxx links: [audit](FOUNDATION_AUDIT.md). Policy history: S17 and S19 §13.2; [decision authority](DECISION_REGISTER.md). **GROUP-02 is not implemented or complete.**

## Verified GROUP-01 rules

| Rule | Source / implemented behavior |
| --- | --- |
| Create GROUP | S09 create: one transaction creates Conversation UUID, OWNER/ACTIVE and separate PENDING invitation records; key is per-user/normalized payload |
| Pending is not membership | S10 invitation statuses separate from member ACTIVE/LEFT/REMOVED; no PENDING member enum |
| Active group reads | S09 detail/members requireMember; S10/S09 list query uses ACTIVE. OWNER/ADMIN/MEMBER can read only if ACTIVE |
| Invitation control | Outgoing inspection/send OWNER-only in current GROUP-01. Incoming filtered by JWT invitee. Pending invitee can act on own invitation without group resource access |
| Accept/reject | S09 respond locks invitation, checks invitee identity (concealed404 for another actor), pending status, existence/budget; accept rechecks eligibility/inviter ACTIVE and inserts MEMBER/ACTIVE with invitation update atomically; reject no membership |
| Processed repeat |409 INVITATION_PROCESSED; no invented idempotent200 |
| Directory | S11 requires authoritative existence/discoverability/both block directions; S07 production default503. Test-only adapter in S16 is not production privacy enforcement |
| Rejoin today | Any existing conversation/user member row, including LEFT/REMOVED, causes409; unique pair in V1. Enum values do not supply leave/remove APIs |
| Android | Scope/user/epoch/lifecycle and bounded requests verified by fixture tests; PeopleDirectory/eligible and hidden buttons do not enforce server ACL |

## Unresolved lifecycle areas

Approved direction below means historical scoped direction, not schema/API approval. D08 is not frozen. No Message/media/WS endpoints currently exist, so deny-after-removal guarantees are required future behavior, not demonstrated implementation.

| Area | Currently implemented | Approved direction | Missing implementation / open owner choice | TV1 dependency | TV2 dependency | Database migration dependency |
| --- | --- | --- | --- | --- | --- | --- |
| 1 Rejoin LEFT/REMOVED | Rejected409 if any member row exists | New period, explicit new invite after removal; old accepted invite grants nothing | Decide LEFT reentry, removal vs ban and authorized reinviter; TV3 policy | Account eligibility, block/privacy | Message visibility/cursor reset | Approved period/current projection +unique active constraint/backfill |
| 2 Membership periods | One row per pair with joinedAt/leftAt | Separate periods, latest join default | Choose period identity, open/close bounds, zero-message group and uncertain legacy history; TV3 | Privacy review | Exact server sequence boundary/lookup | Preserve V1, reviewed forward migration only |
| 3 Historical visibility | No Message history query | Latest period floor default | Inclusive/exclusive floor, leave/removal upper bound, retention vs access are separate decisions; TV3+TV2 | Retention/privacy | History pagination/replay filter/read-delivered mapping | Period bounds +message sequence mapping; no guessed backfill |
| 4 Group disband | No API/state | Least privilege only | Who may disband, read-after-disband/retention, pending invites and restore; TV3 decision | Privacy/deletion | Conversation/message/fanout state | State/invariant additions only after approval |
| 5 OWNER succession | At-most-one active OWNER index; no transfer | No new succession policy approved | Reject last-owner leave vs explicit atomic transfer vs disband; choose target eligibility; TV3 | Eligible active account | Route/event/update handling | At-least-one invariant not provided by index; transactional transfer/locks reviewed |
| 6 ADMIN permissions | Enum exists; read ACTIVE; no invite management | Least privilege; no invented elevated rights | Explicit action/target hierarchy/role transitions; keep OWNER-only GROUP01 until approved; TV3 | Abuse/privacy | Event/client permission projections | May require role history/version; do not add schema by assumption |
| 7 Invite expiry/reissue | PENDING unique, no expiry; processed409; CANCELLED enum no cancellation route | Removed requires explicit new invitation | Expiry clock/status, cancellation, repeated response/reissue/key/version and accept race; TV3 | Block/friend recheck authority | Event/error DTO semantics | Expiry/version/index changes and compatibility review |
| 8 Message after removal | No removal/message API | Server denies new access when inactive | Send/read/edit/reply/receipt/replay entitlement and earlier-admitted write guarantee; TV3 policy | sid/identity remains valid for other groups | Enforce on all message/query/fanout paths | ACL version/period bounds and lock order with sequence |
| 9 Attachment after removal | No media endpoint | Same active/visible-message authorization | Revalidate download vs short-lived URL with explicit residual window; no recall of prior client copies; TV3+TV2 | Security/key/storage privacy | Upload/download/storage reference lifecycle | Attachment ownership/Message relation and any ACL version reviewed |
| 10 Revocation ordering | ACTIVE check only; no transition/revoke signal | Backend authority; after revoke commit deny newly admitted group operations | Choose linearization point and membership-vs-send locks; final queued delivery fence, reliable after-commit invalidation, propagation bounds; TV3+TV2 | Session revoke is separate; authority outage fails closed | WS subscription/replay/delivery generation checks | Transaction/version/outbox strategy approved with concurrency tests |

## Required acceptance, not executed here

Concurrent accept/accept, accept/reject, block/accept, remove/send, remove/history, rejoin/old-invite and queued WS/media delivery must prove one authorized outcome with no pre-join leakage. Verify nonexistent vs concealed resource shape, authorized409, no token refresh on403/404, explicit timeout-after-commit reconciliation, and unchanged shared runtime ownership. Directory projection never replaces these checks.

TV3 owns transition/ACL policy, TV2 owns Message/REST/WS/media enforcement, TV1 owns identity/social authority; all affected owners approve shared schema. Do not implement membershipPeriodId, ADMIN rights or new enums just to unblock compilation. [Readiness](IMPLEMENTATION_READINESS.md) and [TV2 tracker](TV2_DEPENDENCY_TRACKER.md) name remaining gates.
