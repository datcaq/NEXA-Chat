# TV3 Foundation audit — 26/11/2026

Documentation handoff only. IMPLEMENTED means present at the named revision, not merged or production accepted. PARTIAL means a usable subset/port; MISSING means absent in inspected sources; PROPOSED means a design without runtime acceptance.

## Preflight and provenance

Fetched origin successfully. Remote is https://github.com/datcaq/NEXA-Chat.git. Main is `8b730dccc171c6d55fd8ae127448ed2b6c5f23a4`. Original checkout was feature/group-01-create-group at `2e804c033631049683cffd1d7500bb7406658bfa`, with no tracked edits. Four untracked docs/PHASE1_*.md were preserved byte-for-byte and excluded from staging. Dedicated docs/tv3-day01-close is based on this main; no feature merge, rebase or reset.

No equivalent canonical docs/architecture/tv3 files exist on main. This package consolidates the earlier untracked Phase1 proposal into the requested handoff location and links immutable Phase2A evidence rather than duplicating its implementation reports. The four historical drafts remain untouched; their old “all decisions proposed / PR13 changes requested / Groups lifecycle gaps” snapshot is superseded by this dated audit. They are not required links for reviewers of this branch.

| PR | Verified current HEAD | State and review scope |
| --- | --- | --- |
| #6 | `2e804c033631049683cffd1d7500bb7406658bfa` | OPEN/DRAFT/unmerged, no official reviews. Backend is not on main. |
| #9 | `ea239b9f66a153ac9f848fd089e5d4dd8b8874e4` | OPEN/DRAFT/unmerged, no official reviews. Body claims local DESIGN3/R4 acceptance, not whole-contract approval. |
| #13 | `5f8c081c6fadeb135fea8adcc9f22f493c13dce7` | OPEN/DRAFT/unmerged. TV2 APPROVED exact FIX1 HEAD; old finding TV2-R13-01 CLOSED. |
| #16 | `fae99ae0cf96f620da426b89abbd3c42920702e8` | OPEN/DRAFT/unmerged, eight Markdown files, no official reviews. Proposals pending owner approval. |

All five historical user commit references were checked as Git commit objects; d2141e2560d8d6ba198942c944a69a963c133923 remains available but is no longer #6 HEAD. API pull metadata/reviews, PR16 files and PR9/16 issue/review comments were inspected on 26/11/2026. Main/remote branch trees were checked separately from feature source. No teammate message or official review submitted.

[Exact-head FIX1 approval](https://github.com/datcaq/NEXA-Chat/pull/13#pullrequestreview-6062016319) resolves the earlier P2 only. It explicitly does not approve production Host adapters, transport admission, physical termination, merge or architecture freeze.

## Current architecture

| Area | Status | Evidence and limitation |
| --- | --- | --- |
| Android Java/XML, Auth preview, Home four tabs | IMPLEMENTED foundation on main | S01–S04; launcher does not authenticate or mount production modules. |
| Auth/Session | PARTIAL | Registration challenge and Session V2/CredentialProvider contracts exist. Real OTP/login, secure store, refresh/revoke, User/Friend APIs and production SessionRepository are MISSING. |
| Coordinator FIX1 | IMPLEMENTED foundation on #13 | S05 catches recoverable hook failures, fences before replacement, retains bounded retiring/cleanup ownership. Real resource factory/host adapters still MISSING. |
| Groups Android | IMPLEMENTED foundation on #6 | Scope UUID+local epoch; bounded callbacks/requests, rejection accounting, independent terminal UI lane; view recreation keeps account ownership. No true Auth/credential/directory/Chat adapter. |
| Groups server | IMPLEMENTED GROUP-01 subset on #6 | Single Spring Boot Java21 runtime, controller/service/JPA/DTO projections, owner creation, invitation transactions, active-member reads, write budget/paging. Default production directory returns503. |
| Messaging | PARTIAL foundation on #9 | Account controller/router/lease/outbox/ACK logic and RAM fake; no backend Message/REST/WS/Room runtime. Shared Conversation pipeline remains direction. |
| Shared security | PARTIAL | Global chain/decoder still in Groups, local UUID subject parsing. TrustedPrincipal/sid authority and common request error renderer are PROPOSED/MISSING. TV1 owns follow-up. |
| Database | IMPLEMENTED candidate V1 on #6 | Six tables; UUID users skeleton; DIRECT/GROUP root, unique member pair, at-most-one active owner, invitations/creation keys/write budgets. No Message, periods, delivered sequence or canonical DIRECT pair. |
| Flyway | PARTIAL | One V1, no explicit custom location/history settings in application.yml; dependencies use Boot default integration. Historical ephemeral PostgreSQL migrate verified. Shared/prod history UNKNOWN. |
| Production integration | MISSING | Auth→Home→directory/friend→DIRECT/GROUP real persisted/realtime multi-account E2E, removal/media/WS ACL and Translation are not established. |

Android scope invalidation protects local state; it cannot authorize server membership. Server User UUID, server sid and local epoch are different identities. A public profile/eligible flag never proves current server permission.

## Test evidence and attribution

No build/test runtime was rerun for this Markdown-only milestone. Existing logs/XML were inspected read-only; no database, Docker or emulator was started. S19 section13 contains commands, environment, prior failures and fixture limitations for implementation delivered in #6 at `2e804c033631049683cffd1d7500bb7406658bfa`.

| Evidence | Result | What it proves / does not prove |
| --- | --- | --- |
| Groups Phase2A.1, 25/11, S19 §13.8 | Reported93 JVM =86 Groups+7 baseline; final Gradle build log PASS | Historical implementation run, not today's rerun. Current checkout has changed to main; do not sum with main counts. |
| External phase2a1-instrumentation-passed/results.xml | Inspected13 tests,0 failure/error/skip; Groups10/Home2/Example1 | API37 emulator fixture UI/lifecycle only. Earlier11-test/10-failure and13-test/3-failure runs remain separate failed evidence. No real Auth/HTTP/WS E2E. |
| External phase2a1-backend.log | Inspected25 tests,0 failure/error/skip;12 service+13 Postgres16; BUILD SUCCESS | Testcontainers V1 validate1/apply1. Test-only UUID decoder/directory in S16; does not verify real JWT or shared DB. |
| PR13 TV2 exact-head approval | Reviewer attests50/50 JVM,3/3 UI,15 supplemental probes | Separate reviewer evidence for FIX1, not rerun locally. Original2/3 UI failure retained; root cause/provenance limits explicitly unresolved historically. |
| PR9 author body at current HEAD | Claims183 JVM/3 foundation UI | Author-reported foundation evidence, not transport verification or a fresh local run. |

External artifacts are under D:/Nexa-Chat/test-artifacts; not shipped or represented as repository assets. Full deployment, backup/restore, existing DB upgrade/restart, API26–36 UI and real production security tests remain MISSING/UNKNOWN. Testcontainers annotation allows skips without Docker; future acceptance requires actual0 skipped, not only Maven exit0.

## Evidence index

Each Sxx points to source at an immutable SHA. Other handoff docs reuse these IDs. The backend paths describe #6 candidates, not files present on this main-based branch.

| ID | Immutable source |
| --- | --- |
| S01 | [app/src/main/java/com/nexa/chat/MainActivity.java](https://github.com/datcaq/NEXA-Chat/blob/8b730dccc171c6d55fd8ae127448ed2b6c5f23a4/app/src/main/java/com/nexa/chat/MainActivity.java#L1) |
| S02 | [app/src/main/java/com/nexa/chat/feature/auth/AuthRepository.java](https://github.com/datcaq/NEXA-Chat/blob/8b730dccc171c6d55fd8ae127448ed2b6c5f23a4/app/src/main/java/com/nexa/chat/feature/auth/AuthRepository.java#L1) |
| S03 | [app/src/main/java/com/nexa/chat/feature/auth/session/SessionRepository.java](https://github.com/datcaq/NEXA-Chat/blob/8b730dccc171c6d55fd8ae127448ed2b6c5f23a4/app/src/main/java/com/nexa/chat/feature/auth/session/SessionRepository.java#L1) |
| S04 | [app/src/main/java/com/nexa/chat/feature/auth/session/CredentialProvider.java](https://github.com/datcaq/NEXA-Chat/blob/8b730dccc171c6d55fd8ae127448ed2b6c5f23a4/app/src/main/java/com/nexa/chat/feature/auth/session/CredentialProvider.java#L1) |
| S05 | [app/src/main/java/com/nexa/chat/core/session/SessionCoordinator.java](https://github.com/datcaq/NEXA-Chat/blob/5f8c081c6fadeb135fea8adcc9f22f493c13dce7/app/src/main/java/com/nexa/chat/core/session/SessionCoordinator.java#L1) |
| S06 | [app/src/main/java/com/nexa/chat/feature/messaging/DAY1_DECISIONS.md](https://github.com/datcaq/NEXA-Chat/blob/ea239b9f66a153ac9f848fd089e5d4dd8b8874e4/app/src/main/java/com/nexa/chat/feature/messaging/DAY1_DECISIONS.md#L1) |
| S07 | [backend/src/main/java/com/nexa/chat/backend/groups/GroupConfiguration.java](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/java/com/nexa/chat/backend/groups/GroupConfiguration.java#L1) |
| S08 | [backend/src/main/java/com/nexa/chat/backend/groups/GroupController.java](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/java/com/nexa/chat/backend/groups/GroupController.java#L1) |
| S09 | [backend/src/main/java/com/nexa/chat/backend/groups/GroupService.java](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/java/com/nexa/chat/backend/groups/GroupService.java#L1) |
| S10 | [backend/src/main/resources/db/migration/V1__shared_conversations_and_group_invitations.sql](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/resources/db/migration/V1__shared_conversations_and_group_invitations.sql#L1) |
| S11 | [backend/src/main/java/com/nexa/chat/backend/groups/GroupDirectory.java](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/java/com/nexa/chat/backend/groups/GroupDirectory.java#L1) |
| S12 | [app/src/main/java/com/nexa/chat/feature/groups/repository/PeopleDirectory.java](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/app/src/main/java/com/nexa/chat/feature/groups/repository/PeopleDirectory.java#L1) |
| S13 | [app/src/main/java/com/nexa/chat/feature/groups/model/GroupModels.java](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/app/src/main/java/com/nexa/chat/feature/groups/model/GroupModels.java#L1) |
| S14 | [backend/src/main/java/com/nexa/chat/backend/groups/GroupMapper.java](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/java/com/nexa/chat/backend/groups/GroupMapper.java#L1) |
| S15 | [backend/src/main/java/com/nexa/chat/backend/groups/GroupErrors.java](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/java/com/nexa/chat/backend/groups/GroupErrors.java#L1) |
| S16 | [backend/src/test/java/com/nexa/chat/backend/groups/GroupPostgresTest.java](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/test/java/com/nexa/chat/backend/groups/GroupPostgresTest.java#L1) |
| S17 | [docs/group-01/PHASE2A_MEMBERSHIP_POLICY.md](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/docs/group-01/PHASE2A_MEMBERSHIP_POLICY.md#L1) |
| S18 | [docs/group-01/PHASE2A_BACKEND_MANIFEST.md](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/docs/group-01/PHASE2A_BACKEND_MANIFEST.md#L1) |
| S19 | [docs/group-01/PHASE2A_LIFECYCLE_REPORT.md](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/docs/group-01/PHASE2A_LIFECYCLE_REPORT.md#L1) |
| S20 | [app/src/main/java/com/nexa/chat/feature/groups/GroupsIntegration.java](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/app/src/main/java/com/nexa/chat/feature/groups/GroupsIntegration.java#L1) |
| S21 | [app/src/main/java/com/nexa/chat/feature/groups/data/RemoteGroupRepository.java](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/app/src/main/java/com/nexa/chat/feature/groups/data/RemoteGroupRepository.java#L1) |
| S22 | [backend/src/main/java/com/nexa/chat/backend/groups/GroupRequestLimit.java](https://github.com/datcaq/NEXA-Chat/blob/2e804c033631049683cffd1d7500bb7406658bfa/backend/src/main/java/com/nexa/chat/backend/groups/GroupRequestLimit.java#L1) |

## Handoff map

- [PR16_TV3_REVIEW](PR16_TV3_REVIEW.md)
- [DECISION_REGISTER](DECISION_REGISTER.md)
- [OWNERSHIP_MATRIX](OWNERSHIP_MATRIX.md)
- [IMPLEMENTATION_READINESS](IMPLEMENTATION_READINESS.md)
- [GROUPS_CONTRACT_READINESS](GROUPS_CONTRACT_READINESS.md)
- [BACKEND_FOUNDATION_PREPARATION](BACKEND_FOUNDATION_PREPARATION.md)
- [FLYWAY_VERIFICATION_PLAN](FLYWAY_VERIFICATION_PLAN.md)
- [TV2_DEPENDENCY_TRACKER](TV2_DEPENDENCY_TRACKER.md)

## Documentation validation

Checked104 Markdown links: local targets exist; immutable GitHub file/ref/line ranges resolve against fetched Git objects; exact-head PR13 review URL was verified through GitHub API. Seven40-character referenced object IDs exist (six commits and V1 blob). No executable, Word, schema or migration file is included; all nine staged additions are Markdown. Complete staged diff and ownership/proposed-vs-approved/test attribution were reviewed; git diff --check and staged whitespace checks pass. No production build was needed or run.

Switching from the Groups branch to main leaves prior backend/target build outputs untracked because main has no backend/.gitignore. Those pre-existing artifacts and the four Phase1 drafts are preserved and excluded; this branch's working tree is not claimed globally clean. No new application output was generated by this task.
