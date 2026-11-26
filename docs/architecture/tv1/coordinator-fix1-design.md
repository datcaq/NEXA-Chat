# CORE-04A FIX1 Design and Regression Matrix

**PROPOSED / PENDING APPROVAL** · Owner TV1 · Reviewer TV2, with TV3 integration review

[Architecture handoff and source evidence](README.md) · [Session lifecycle](jwt-session-lifecycle.md) · [App Shell integration](composition-root-integration.md)

## Confirmed finding and scope

PR #13 remains at `1c459c517cbfea06d1c071554a05c894157e157e` with official changes requested. FIX1 is a design, not an implementation at this HEAD. [TV2-R13-01 review](https://github.com/datcaq/NEXA-Chat/pull/13#pullrequestreview-6050049996)

SessionRepository requires observers to return promptly without throwing. Coordinator's `onSessionChanged()` calls `reconcile()`, which can propagate from factory `open`, from cleanup of an obsolete candidate or from retire of an old scope. It detaches the old scope pointer before cleanup. The Architecture Review 01 Java probe reproduced:

```text
FACTORY escaped=1 destination=CHECK_SESSION
CLEANUP escaped=1 destination=AUTH
```

Both destinations were fail-closed in the probe. Clearing the coordinator pointer does not prove that a feature's protected operations have been fenced. The probe establishes an observer contract violation, not a crash or cross-account exposure. Preserve severity P2. Initial observation may run inline before the subscription handle is returned. See E2 and E5 in the [evidence index](README.md#source-evidence-index).

At PR #6 HEAD `45c90a7`, Groups closes its own logical scope before scheduling bounded asynchronous cleanup and tracks cancellation separately from termination. That narrows one feature adapter risk. It does not resolve #13's factory, old/obsolete or inline failure paths. Groups preparation/cleanup capacity and request budget can still reject ownership transfer, and actual integration with a shared credential authority remains unverified. See E8 and E13 in the [evidence index](README.md#source-evidence-index).

## Internal recovery contract

1. **Preparation and partial ownership.** Factory returns a prepared scope or typed recoverable failure. No protected I/O during preparation. Factory owns allocations until successful adoption and releases partial allocations in reverse order where practical. If disposal admission rejects before ownership transfer, keep responsibility and retry cleanup with bounded work.
2. **Optional versus account failure.** A recoverable Messaging lease capacity or optional module error becomes module unavailable while the authorized Home scope remains usable. Failed required account preparation keeps Home closed, preserves validated session state and provides bounded retry on the matching epoch. Distinguish a fail-closed account gate from backend credential revocation.
3. **Logical invalidation.** Coordinator and adapters share an authoritative admission gate. Close it before any replacement scope becomes eligible. Gate applies to transport admission, queued result application and UI mutation. If the old gate cannot be proven closed, quarantine old scope and deny new protected scope access until invalidation is verified. Keep its handles owned. A caught exception is no evidence of logical invalidation.
4. **Physical cleanup.** After gate closure, schedule callback/subscription cancellation, requests, sockets and lease cleanup on their owners. Report expected failures using redacted diagnostics and retain incompletely cleaned handles under a capacity limit. New access can proceed only when the old scope is logically isolated and new scope independently valid. Do not wait for cancellation/termination on UI.
5. **Obsolete candidate.** Re-read snapshot after preparation and after reentrant hooks. Reject stale candidate activation, close its gate and dispose independently. Cleanup of the obsolete candidate cannot cancel the current account's transport or erase its state.
6. **Inline and subscription acquisition.** Mark initialization state explicitly. Recoverable factory error must not escape the initial observer and lose the returned subscription. If close happens inline, close the acquired handle exactly once on return. If observe itself fails before handing over a subscription, roll back started state and handle owned resources. The repository owns partial acquisition side effects for a subscription it did not return.
7. **Scoped exception policy.** Handle known recoverable failures at the factory, old cleanup and obsolete cleanup boundaries. No blanket catch around reconciliation, no blanket suppression of assertion or fatal JVM errors. Report expected errors and isolate each cleanup item so one error cannot skip the remainder.
8. **Reentrancy and idempotence.** Serialize transitions on the owner executor, coalesce reentrant notifications and reconcile from the latest snapshot after the current transition. Close gate and unsubscribe at most once. Never execute arbitrary external callbacks while holding the session-state lock.
9. **Retry.** Allow explicit retry or bounded backoff while UUID/epoch and lifecycle still match. Deduplicate concurrent retries, cancel them on close/epoch change and avoid automatic retry from duplicate observation.

Keep public Session V2 unchanged for FIX1. Add the smallest internal CORE-04A gate or typed adapter result needed to prove isolation. If TV2 or TV3 production adapters must adopt new shared lifecycle or transport hooks, require their approval before the change. A coordinator unit test alone cannot establish protection of actual transport admission.

## Regression matrix

| ID | Initial state and trigger | Injected failure | Destination and account scope | Resource ownership, observer, retry and invariant |
| --- | --- | --- | --- | --- |
| F01 | CHECKING → authenticated A | Required factory failure after partial allocation | CHECK_SESSION, A inactive | Factory releases partial work. Observer remains active and does not throw. Bounded retry only for current A. No protected I/O during preparation. |
| F02 | A authenticates, optional Messaging created | Capacity/worker startup fails | HOME with A, Messaging unavailable | TV2 owns failed lease reclamation. Observer stays active. Retry module with backoff, and never invalidate valid A for an optional failure. |
| F03 | A still preparing, latest snapshot becomes B | Obsolete A cleanup fails physically | A never publishes. B only after A logical gate is confirmed closed | Cleanup worker owns A handle. No delayed A UI or IO. Retry cleanup only, never reopen A. |
| F04 | Active A → authenticated B | Cannot establish logical invalidation of A | CHECK_SESSION, A quarantined and B inactive | Keep A ownership. No exception escapes observer. Retry gate closure boundedly. Never two active accounts. |
| F05 | Active A → logout | Physical cleanup fails after gate closes | AUTH, A remains logically closed | Cleanup ownership retained, idempotent close/unsubscribe, no stale reopen. Diagnostics without payloads. |
| F06 | observe delivers A inline | Factory fails or calls close inline | CHECK_SESSION, no protected A screen | On recoverable error acquire and retain subscription. On inline close close returned handle once. Retry only while coordinator is alive. |
| F07 | Initial subscription acquisition | observe fails before returning handle | CHECK_SESSION, no scope activates | No started-state or scope leak. Repository cleans partial registration. Retry start only after a defined failure. |
| F08 | A active → CHECKING → UNAVAILABLE → authenticated A same epoch | Duplicate queued callbacks and temporary outage | Gate suspended, then HOME with same scope | No duplicate lease or refresh. Observer remains live. No effects while suspended, resume only for matching UUID/epoch. |
| F09 | Close repeated while observation or hook is queued/reentrant | Late result, cleanup or executor path fails recoverably | No protected destination after close | Retire and unsubscribe once. Coalesce reentrancy, drop late work and prevent cleanup from mutating new epoch. |

Acceptance also needs real-adapter tests for logical admission fencing, no blocking UI close and bounded termination accounting. Keep separate evidence for Coordinator, MessagingExecution and Groups request lifecycle. See the [decision register](cross-team-decision-register.md).
