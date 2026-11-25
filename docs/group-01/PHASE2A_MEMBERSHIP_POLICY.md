# Phase 2A — Groups membership/access policy
Ngày25/11/2026. Authorization source: Hoàng Phúc's Phase2A instruction. Architectural directions D01–D10 approved there; Phase1 files remain historical, unchanged. This document does not create REST/schema contracts or grant ADMIN permissions not approved.

## Implemented today — PR6 branch
- Conversation has GROUP type and shared UUID. Group create atomically creates OWNER/ACTIVE and separate PENDING invitations.
- V1 member status supports ACTIVE/LEFT/REMOVED. **PENDING is invitation state, not an ACTIVE member row**. No contract or migration changed to add a PENDING member enum.
- Group list/detail/members require active membership; outgoing invitation inspection/sending is OWNER-only in GROUP-01. GroupDirectory validates existence/eligibility but default503 pending TV1 adapter.
- Invitation accept/reject is controlled by JWT invitee identity. These invitation control endpoints must be accessible to eligible invitees before membership; this does NOT authorize reading group history.
- Accept locks invitation, validates directory/inviter membership and inserts MEMBER/ACTIVE atomically. Already-processed invite returns409, not an invented idempotent200. Reject creates no membership.
- Group creation key is per-user/payload. Existing Postgres tests cover retry/concurrent create, acceptance, pending uniqueness, rollback, owner authorization. Test identity is test-only, not real JWT proof.
- Backend source unchanged in Phase2A. Test suite25 PASS, including13 real PostgreSQL Testcontainers.

## Approved direction awaiting separate schema/API implementation
Rejoin must create a new membership period; history begins at latest membership period; removed users need a new explicit invitation; only ACTIVE users access group resources; backend authorization is authoritative.

Current schema unique(conversation_id,user_id) and GroupService.respond reject any existing membership, including LEFT/REMOVED. Thus **rejoin/period/history is NOT implemented**, despite approved direction. No Message/history/media/WS endpoint exists in this backend. These require TV2 shared Conversation/Message/sequence review, approved forward migrations and verified migration environment state. Phase2A does not simulate them in Android or edit V1.

## Role/history matrix — no implied new rights
| Action | Current OWNER/ACTIVE | Current ADMIN/ACTIVE | Current MEMBER/ACTIVE | PENDING invite /LEFT /REMOVED |
|---|---|---|---|---|
| Group metadata read/list | Allowed | Allowed | Allowed | No group resource access |
| Send/inspect group invitations | Allowed | Forbidden in GROUP-01 | Forbidden | Forbidden |
| Accept/reject own pending invitation | By invitee identity, not role | Same | Same | Eligible invitee only; does not grant access before accept |
| Message/history/attachment read or send | Not implemented | Not implemented | Not implemented | Must be denied when implemented |
| Remove/leave/change roles | Not implemented | Not implemented | Not implemented | No API |
| Rejoin/new period | Not implemented | Not implemented | Not implemented | Removed requires new explicit invite; no reuse of old accepted invite |

## OPEN decisions / dependencies
| Policy | Alternatives to review | Owner/dependency |
|---|---|---|
| Group disband | Owner leaves only after transfer; or explicit disband flow | OPEN — Hoàng Phúc +TV1 privacy/TV2 Message retention |
| OWNER succession/last owner | Explicit atomic transfer; designated successor; reject leave | OPEN — no implicit succession implemented |
| ADMIN powers | Keep OWNER-only management; or narrow reviewed invite/remove rights | OPEN — no ADMIN escalation from enum or Phase1 recommendation |
| Historical data retention | Keep server content with access control; retention/deletion policy | OPEN — not chosen by latest-period visibility direction |
| Attachments after removal | Revalidate each download; short-lived links with explicit residual window | OPEN — TV2 media +TV3 policy +TV1 security |
| Invitation expiry/reissue/retry | Current no expiry/409 processed; add expiry/versioned retry semantics | OPEN — forward migration/API review required |
| Membership-period bootstrap/cursor migration | Known existing period only with uncertainty flags; approved visibility backfill | OPEN — no fabricated historical timestamps/sequence |

Enforcement after implementation must define transactional admission/revoke order. After revoke commit, newly admitted read/send/subscribe/download must fail; already delivered bytes/client copies cannot be recalled. Android scope invalidation is a local safety boundary, not server membership revocation.

Next separate task: owners finalize role/history contract and migration registry, then TV3 GROUP-02 +TV2 pipeline authorization with concurrent/negative tests. Do not call Phase2A GROUP-02 complete.
