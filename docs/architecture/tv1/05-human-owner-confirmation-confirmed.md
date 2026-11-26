# TV1 Human Owner Confirmation — 26/11/2026

**Status:** TV1 business-policy selection confirmed by user; shared decisions remain pending. Source: user's chat confirmation, not TV2/TV3 signature.

- [x] Auth: OTP + password, not OTP-only. Registration OTP proof; password-based regular login. Password reset requires separate verified workflow; endpoints pending.
- [x] Recovery: step-up and separate lost-SIM recovery with independent proof; no automatic account reclaim via recycled phone number.
- [x] JWT audience: TV1 chooses `nexa-api` pending TV3 security config and TV2 WSS concurrence.
- [x] JWT lifetimes: 15 minutes access / 30 days refresh / 30 seconds skew are pilot values, not frozen.
- [x] Message Requests enabled as a desired feature; state machine, permission after Accept and exact API/DTO need TV2/TV3 decision. No unrestricted DIRECT before acceptance.
- [x] Crossed friend invitations require explicit acceptance; no auto-friend.
- [x] Retain legitimately visible history after block/unfriend, recheck current ACL; details pending.
- [x] Discovery: E.164 exact lookup, default hidden/opt-in and no hidden/absent/blocked leakage.
- [x] D08: TV1 supports TV3 coordinating shared migration registry; affected owners approve schema changes, database lineage unknown.
- [x] Privacy: canonical concealed 404 branch parity per FIX3.
- [x] Navigation: TV1 App Shell, TV2 Messaging runtime, TV3 caller, one shared port pending.
- [x] Transport: native WSS bearer direction; timeout/terminal resource evidence pending.

## Explicit unresolved owners

**TV1 domain detail pending:** password hash implementation and parameters; OTP provider & SMS budget; recovery proof; exact password login wire; discovery quotas and consent UI; JWT key issuer/session lifecycle implementation. These are not yet implementation approvals.

**All three / TV2 and TV3 pending:** D08 approval, D05 Message Requests protocol and DIRECT eligibility, D09/D10/D25 Groups policy, D07 Host ports, D11 WSS interop, shared schema/migrations, D23 provenance.

## Handoff

Use this record together with `01-formal-owner-response-confirmed.md` in PR #16 for targeted CONTRACT-01E.2; do not mark remaining upstream decision IDs approved. No code/schema/migration changes authorized.
