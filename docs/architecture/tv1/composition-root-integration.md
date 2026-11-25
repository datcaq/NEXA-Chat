# Composition Root and Home Integration

**PROPOSED / PENDING APPROVAL** · TV1 owns cross-module integration

[Architecture handoff and source evidence](README.md) · [Session lifecycle](jwt-session-lifecycle.md) · [FIX1 design](coordinator-fix1-design.md)

## Confirmed implementation

- MainActivity loads the Auth preview layout. Home has tab containers and saved tab selection. PR #15 changes only the `androidTest` host and does not establish production WindowInsets behavior. PR #14 is Contacts UI Foundation only, without real directory requests, friendship state or production Home mounting. **E1, E4, E6**.
- Messaging #9 has account-scoped Controller/Router, retained execution lease, `MessagingView.setHostState(selected,resumed)`, `handleBack()`, DIRECT coalescing and stale intent guards. Its internal `ChatNavigator` uses string IDs and void return. Account runtime close/cancel is distinct from physical completion. The in-memory FakeConversationRepository is under the feature branch's main source set and must be excluded from a production release. **E7**.
- Groups #6 still exposes `GroupsIntegration.open(Activity)`, a separate GroupsActivity and `ChatNavigator.open(Activity,String)`. Current `45c90a7` adds bounded admission, logical close before asynchronous cleanup, a tracked request lifecycle and fail-closed dependency replacement. Credentialed Retrofit, real PeopleDirectory and TV1 navigation adapters remain unintegrated. Compiled instrumentation is not device execution. **E8, E13**.

## Ownership and lifecycle

| Scope | Owner and components | Dependency and boundary |
| --- | --- | --- |
| Application/process | TV1 Composition Root, SessionRepository runtime, CredentialProvider and shared HTTP pool | One identity source and owner dispatcher. Separate Auth/refresh path to avoid interceptor recursion. No shared credential-bearing cookies or private response cache between accounts. |
| Application/process | TV2 MessagingExecution | TV1 wires the runtime. TV2 owns workers, lease admission, retirement and bounded capacity. Wait for termination only off UI. |
| Account | TV1 SessionCoordinator and account registry, Contacts/Friends/Profile runtime | One account keyed by server UUID and local epoch. Sources are canonical verified snapshot and secure credential runtime. |
| Account | TV2 MessagingController, MessagingRouter and repository adapter | Retain across view recreation. Bind lease, credentials and route state to current account. TV2 owns internal routing and outbox/ACK behavior. |
| Account | TV3 Groups scope/repository and TV1 PeopleDirectory adapter | TV1 supplies latest access projection, real directory/credential adapters and navigation bridge. Only cancel this adapter's calls on close. TV3 owns Groups internal navigation. |
| Activity/view | TV1 MainActivity/Home, TV2 MessagingView, TV3 GroupsActivity | Attach scoped dependencies to views, propagate selection/resume/pause, unsubscribe at view teardown and route system Back only to the active surface. |
| Operation | Owner of HTTP call, WS subscription, search, media, or translation | Credential and epoch checked at admission. TV3 Translation uses shared server-authorized Message input, preserves original content and surfaces provider errors. |

## Authenticated gate

| State or transition | Render and access |
| --- | --- |
| CHECKING | Session check screen, protected content and admission closed. A matching retained scope may be suspended. |
| SIGNED_OUT | Auth, retire prior scope. |
| UNAVAILABLE | Explicit retry UI. Hide protected information and suspend same-epoch scope. |
| AUTHENTICATED, preparing or mismatched scope | Keep protected UI closed. Prepare isolated scope, then reread authoritative snapshot. |
| AUTHENTICATED, matching UUID/epoch and safe scope | Mount Home or registration profile completion as applicable. A failed optional module gets an unavailable surface. |
| Logout, rejection or switch | Close old admission, unmount old surfaces and cancel work. Only publish a replacement scope after isolation is confirmed. |

No fake session opens production Home. Scope preparation is not authorization for REST or WebSocket dispatch. Runtime adapter tests must prove admission at the credential/transport boundary, separately from Coordinator tests.

## Mount, host state and navigation

Mount `MessagingView(context, controller, controller.getRouter())` into the Messages panel for the active account. Pass selection and resume state, stop presentation on pause or tab exit, and remove subscriptions on view unmount. Hidden/paused tabs do not emit READ/TYPING. ACK reconciliation remains account-scoped. Delegate System Back through the active view's `handleBack()` and let Home handle the remaining routes. Rotation recreates View while retaining Controller/Router for the same epoch. Process recreation performs secure restore and creates a new epoch before restoring any protected route.

For initial Groups integration, TV1 mounts an entry in the Home Groups panel and opens GroupsActivity only when the account gate is active. Use the installed Groups dependencies and account-scoped PeopleDirectory. Do not assume Groups has an embeddable view. Guard admission and handle replacement/cancellation following the bounded Groups lifecycle.

Proposed port `com.nexa.chat.core.navigation.ConversationEntry`, owned by TV1 and called on the owner/main executor:

```java
boolean openDirect(UUID peerUserId)
boolean openConversation(UUID conversationId)
```

These are candidate signatures only. `true` means the command was admitted for current account UUID/epoch. Backend create/authorization and route confirmation are asynchronous. Contacts hands a server-issued peer UUID to TV1 then TV2's `openDirectAndNavigate`. Preserve one pending DIRECT operation per peer/account, caller ownership and stale navigation-intent checks. Groups hands a server-issued conversation UUID to the same bridge from its Activity port. TV1 owns tab/cross-module routing, TV2 owns conversation-detail routing, TV3 owns Groups screens. Backend independently checks canonical direct pair, friendship/block and ACTIVE membership.

## Isolation and teardown order

1. TV1 invalidates epoch admission and switches UI to Auth/check state before rendering another account.
2. TV1 unmounts old views and unregisters observers, closes credential waiters and cancels tracked HTTP calls. TV2 invalidates controller/router, requests WS close, retires execution lease after actual terminal accounting. TV3 closes Groups scope, pending results and Activity, then requests Retrofit cancellation through owned handles. Preserve shared thread/connection pools.
3. Clear private RAM and saved UI state. Delete old credentials. Persisted drafts/cache may be purged or safely partitioned by user UUID per approved policy. Account B must never read A's records or restore A's route.
4. Physical cleanup can finish asynchronously. Track ownership, timeout, callbacks and termination separately. Any late callback must recheck epoch and generation at mutation time. A server operation may have committed before cancellation. Membership revoke and session revoke are distinct.

Acceptance tests cover logout, switch, refresh/suspend/resume, callback queued at close, no account A→B leakage, concurrent UI/transport admission and input under rotation. Test Groups close and stale request delivery against its latest branch, not only the earlier synchronous cleanup implementation. See E2, E5, E7, E8 and E13 in the [evidence index](README.md#source-evidence-index).

## Production WindowInsets acceptance

The production Shell needs its own verification for edge-to-edge, status/navigation bars, display cutout, mandatory gestures, three-button navigation, rotation, small viewport and IME open/close. Check Home tab and active input hit targets, no double-padding across host/module inset handling, accessible title and no loss of selected tab on recreation. PR #15 establishes only the specific instrumentation fixture behavior.
