# Ownership and integration matrix

26/11/2026. Role assignments follow the user's milestone; implementation/review gates below preserve [approved directions](DECISION_REGISTER.md). They are a handoff process, not proof that owners already approved new signatures or schema. Source presence on PR6 does not make all backend components TV3-owned.

| Component | Primary owner | Required reviewers / approval | Integration dependency and responsibility |
| --- | --- | --- | --- |
| Auth/OTP, JWT keys/decoder/claims, TrustedPrincipal, sessions/revoke/secure storage | TV1 | TV1 approves identity/security; TV2 transport +TV3 consumer review; affected shared behavior all3 | One source of verified UUID/sid; implement server authority and one security boundary; TV3 never provides fake user/token fallback |
| User provisioning/self/public DTO, friends/block/discovery/privacy | TV1 | TV1 policy approval; TV3 Groups eligibility; TV2 DIRECT effects | Authoritative minimal public projection and transaction-consistent block/existence; no private fields through picker |
| MainActivity/Home/Composition Root/navigation/shared HTTP | TV1 | TV2 Messaging host/router +TV3 Groups entry approve affected adapter contract | TV1 mounts modules and owns shared pool/gate; modules only close owned calls/leases; latest-session enqueue fencing |
| SessionCoordinator FIX1 | TV1 | TV2 exact-head5f8c081 approval verified; TV3 reviews future resource hooks | Retain coordinator while quarantined handles remain; runtime factory/admission separate from approved foundation |
| Messaging runtime/controller/router/outbox/REST/WS | TV2 | TV2 wire/runtime approval; TV1 auth/host; TV3 membership/history | Real repository/physical cancel and transport settlement; host selected/resumed/back; no independent GROUP pipeline |
| Conversation root/DIRECT pair/Message schema/server sequence | TV2 | TV1 identity/security, TV3 Groups/member schema; affected all3 before shared migration | Shared UUID and canonical direct pair; transactional persistence/idempotency; TV3 creates GROUP using approved application boundary |
| Read/delivered/history/replay/Message/receipt DTOs | TV2 | TV3 period/revoke/privacy bounds; TV1 claims/errors/abuse | TV2 owns wire and every read/write/fanout/replay path, consuming TV3 authorization; no freeze by TV3 docs |
| Groups/invitations/idempotency/group UI | TV3 | TV1 directory/security; TV2 Conversation/wire | OWNER-only invitations current; accept invitee-authorized transaction; production directory pending |
| Membership/roles/periods/rejoin/group ACL | TV3 | TV2 sequence/history/media/WS; TV1 privacy/security; all affected schema owners | TV3 defines authoritative transitions, TV2 enforces on Message paths; ADMIN/succession/expiry still open |
| Backend bootstrap/build/config/health runbook | TV3 preparation/integration coordinator | TV1 security/deps; TV2 runtime compatibility; manifest approved before code | One deployable; preparation only here. No second backend or automatic feature extraction |
| Existing global security in GroupConfiguration | TV1 intended shared owner; physically in PR6 | TV1 implementation approval; TV2/TV3 consumption review | TV3 must not move chain/decoder during infra extraction; TV1 separately adopts approved hunks |
| Global error/correlation | TV1 shared security implementation; TV2 wire convention | TV3 business concealment/status; all3 shared contract approval | Security/auth/filter/advice share sanitized request ID; domain-specific codes retain semantics |
| PeopleDirectory Android adapter | TV1 data/adapter; TV3 port consumer | TV3 projection/paging/account lifecycle; TV2 if peer mapping changes | TV1 maps public DTO/opaque cursor, matching UUID/epoch; close only adapter-owned requests |
| GroupDirectory server | TV1 authoritative identity/social adapter; TV3 eligibility port/policy | TV3 transactional invitation behavior; TV2 shared block semantics | No Android-provided eligibility, no remote network proof under long locks; define local transaction/consistency boundary |
| Shared JPA repositories | TV2 ConversationRepository; TV3 MemberRepository/membership and Groups-specific repos | TV1 users; TV2/TV3 mutually approve shared queries/locking | Current Groups GROUP-only query stays domain-scoped; no service-to-service arbitrary repository mutation |
| Migration registry / single history | TV3 coordination; each table owner owns semantics | TV1 users/auth; TV2 Conversation/Message; TV3 members/invites; all3 shared migration approval +target env owner | Preserve V1 blob; verify sanitized environment history; atomic version allocation and compatibility/backfill tests |
| Media | TV2 storage/transport | TV3 membership/download visibility; TV1 privacy/security | Recheck download ACL or explicitly approve signed URL residual window; unavailable now |
| Translation | TV3 feature/provider integration | TV2 original Message/media boundary; TV1 identity/preferences | Must use authorized visible original; preserve content; no production delivery claimed |
| QA/E2E | Each module owner; TV3 coordinates evidence | Independent teammate review; all3 joint acceptance | Exact SHA/build/environment/XML; no fixture-to-production or compilation-to-device inference |

## Shared acceptance packet

Before shared implementation, TV1 approves identity/JWT/principal/auth boundaries/security/UserDirectory/friend-block; TV2 approves Conversation/Message schema/sequence/REST/WS/read-delivered/history-replay/membership integration; TV3 approves Groups/member/invite/ACL/backend implementation scope. Record exact HEAD and conditions. Joint table/interface reviews do not transfer runtime ownership.

TV3 can author infrastructure preparation and Groups docs, and supply a candidate manifest. This task does not grant TV3 permission to edit TV1/TV2 code, mutate GroupService/Controller for extraction, merge Draft PRs or deploy migrations. [Manifest](BACKEND_FOUNDATION_PREPARATION.md) makes candidate hunks reviewable; [readiness](IMPLEMENTATION_READINESS.md) provides gates.
