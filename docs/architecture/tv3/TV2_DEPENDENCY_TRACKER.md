# TV2 dependency tracker — fresh check 26/11/2026

PR9 remains OPEN/DRAFT/unmerged at `ea239b9f66a153ac9f848fd089e5d4dd8b8874e4`; no official review records. Inspected current branch tree, DAY1_DECISIONS, repository/model source, PR body, review/inline/issue comments (empty), all GitHub PR titles/heads and fetched remote branch document trees. Only README and DAY1_DECISIONS are Markdown on Messaging branch. No separate revised full Wire V1 or30-item register was found in these sources. This is a bounded repository/PR evidence conclusion, not a claim that no private/offline document exists.

TV1 R01–R26 and TV3 D01–D10 are distinct registers and cannot substitute for TV2's30 decisions. No inference about reported21 OPEN/9 CONFLICT contents is made. Directional/local DESIGN3 approvals do not freeze REST/WS or production schema.

## Status definitions

AVAILABLE = exact artifact exists in inspected GitHub scope; PARTIAL = source/design subset; MISSING = required artifact not found; BLOCKED = consuming implementation cannot proceed because prerequisites missing; NEEDS OWNER APPROVAL = candidate exists but affected owners have not approved that contract. Primary status below records artifact availability; separate gate column avoids conflating missing and unapproved.

| Dependency | Primary status | Evidence / exact gap | Gate / required TV2 handoff | Owner/dependent |
| --- | --- | --- | --- | --- |
| DAY1_DECISIONS and local domain/repository | AVAILABLE | Explicitly PROPOSED, fake RAM, no JSON/REST/WS/Room; internal port exists | NEEDS OWNER APPROVAL for promoted shared signatures; independent foundation may remain separate | TV2; TV1/TV3 consumers |
| Revised Messaging Wire Contract V1 | MISSING | No complete revised version found in current PR/branch/comment artifacts | BLOCKED production transport/schema freeze; supply exact file/ref/version, REST+WS examples/nullability/errors/idempotency/auth/paging/reconnect | TV2; TV1 security +TV3 membership |
| 30-item Decision Register | MISSING | DAY1 document and TV1 register are not the requested full30-item register | Provide all IDs/statuses/options/owners/dependencies and precise approval refs; do not invent status totals | TV2; affected all3 |
| Conversation/Message schema mapping | PARTIAL | Java domain String IDs, UUID root in PR6, Message fields and ACK composition; no approved SQL↔wire mapping | NEEDS OWNER APPROVAL; UUID validation/JSON names/nullability/attachment cardinality/reply/FK/DIRECT pair/message model | TV2 schema; TV3 members; TV1 identity |
| Server sequence policy | PARTIAL | ACK long serverSequence per conversation; base/range explicitly TBD; fake order | NEEDS OWNER APPROVAL; transactional allocator, range/wire encoding, same-key payload/ACK, rollback, commit-publish and join/revoke ordering | TV2; TV3 boundary |
| Read/Delivered cursor | PARTIAL | markRead(ServerMessage id+seq), fake monotonic read; V1 UUID last_read_message_id; no delivered wire | NEEDS OWNER APPROVAL; separate read/delivered semantics, member/period scope, visibility floor/ceiling, aggregate receipt privacy/backfill | TV2; TV3 migration +TV1 privacy |
| WS handshake/reconnect | MISSING production contract | No WS implementation/envelope/endpoint; TV1 addendum only proposed auth direction | BLOCKED until TV2 rawWS/STOMP/endpoint/error-close/reconnect budget and TV1 sid/expiry authority reviewed | TV2 transport +TV1 Auth |
| History/replay | PARTIAL | Local exclusive beforeSequence, ascending results/null nextCursor; no event replay log/resync/retention contract | NEEDS OWNER APPROVAL; distinct cursors, event scope/order/dedupe/gaps/reset/retention and membership filtering | TV2; TV3 periods/history |
| Membership integration requirements | PARTIAL | DAY1 lists pending GROUP/history/block; PR6 ACTIVE current behavior +TV3 direction | BLOCKED shared implementation until approved period policy/admission/version/revoke event and every Message/media/replay path defined | TV3 policy, TV2 data paths; TV1 privacy |
| Real transport timeout/cancel/termination | MISSING adapter contract | CompletionStage/source quiescence is not actual physical transport termination | BLOCKED production lifecycle acceptance; return tracked owned handles, actual enqueue/terminal evidence, deadline and retiring accounting | TV2 adapter; TV1 shared HTTP; TV3 compatibility |
| Host/navigation proposal | PARTIAL | MessagingView(controller,router), selected/resumed/back and Groups Activity port differ | NEEDS OWNER APPROVAL; TV1 host bridge maps Conversation UUID/epoch; existing proposed boolean entry is not frozen | TV1 composition; TV2 router; TV3 caller |

## Exact inspected TV2 source

- [app/src/main/java/com/nexa/chat/feature/messaging/DAY1_DECISIONS.md](https://github.com/datcaq/NEXA-Chat/blob/ea239b9f66a153ac9f848fd089e5d4dd8b8874e4/app/src/main/java/com/nexa/chat/feature/messaging/DAY1_DECISIONS.md)
- [app/src/main/java/com/nexa/chat/feature/messaging/repository/ConversationRepository.java](https://github.com/datcaq/NEXA-Chat/blob/ea239b9f66a153ac9f848fd089e5d4dd8b8874e4/app/src/main/java/com/nexa/chat/feature/messaging/repository/ConversationRepository.java)
- [app/src/main/java/com/nexa/chat/feature/messaging/repository/MessagePage.java](https://github.com/datcaq/NEXA-Chat/blob/ea239b9f66a153ac9f848fd089e5d4dd8b8874e4/app/src/main/java/com/nexa/chat/feature/messaging/repository/MessagePage.java)
- [app/src/main/java/com/nexa/chat/feature/messaging/model/Message.java](https://github.com/datcaq/NEXA-Chat/blob/ea239b9f66a153ac9f848fd089e5d4dd8b8874e4/app/src/main/java/com/nexa/chat/feature/messaging/model/Message.java)
- [app/src/main/java/com/nexa/chat/feature/messaging/model/ServerAcknowledgement.java](https://github.com/datcaq/NEXA-Chat/blob/ea239b9f66a153ac9f848fd089e5d4dd8b8874e4/app/src/main/java/com/nexa/chat/feature/messaging/model/ServerAcknowledgement.java)
- [app/src/main/java/com/nexa/chat/feature/messaging/model/ServerMessage.java](https://github.com/datcaq/NEXA-Chat/blob/ea239b9f66a153ac9f848fd089e5d4dd8b8874e4/app/src/main/java/com/nexa/chat/feature/messaging/model/ServerMessage.java)

## Required handoff and TV3 action boundary

TV2 publishes one exact-ref contract packet and register with unresolved choices explicit. TV1 reviews identity/refresh/expiry/revoke/errors; TV3 reviews group root, eligibility, membership periods/visibility/revocation and schema mapping. Then all affected owners record decisions before shared schema/transport implementation. This task neither authors TV2 wire nor fixes sequence widths, JSON names, receipt aggregates, event retention or protocol on TV2's behalf.

TV3 has prepared [Groups gaps](GROUPS_CONTRACT_READINESS.md) and [baseline manifest](BACKEND_FOUNDATION_PREPARATION.md) for that review. Their readiness remains BLOCKED by identified contract/history gates; docs preparation can proceed independently. [Audit](FOUNDATION_AUDIT.md) distinguishes historical183 JVM author report from real backend/WS/E2E acceptance.
