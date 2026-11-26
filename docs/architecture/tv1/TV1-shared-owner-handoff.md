# TV1 — CONTRACT-01E.2 Shared Decision Handoff

26/11/2026. Human business policy decisions confirmed; NOT a shared contract freeze.

- **Main changes:** OTP + password (replaces OTP-only draft); Message Requests for strangers (replaces friends-only-only assumption; does not authorize unrestricted messaging prior to Accept); default opt-in E.164 discovery; `nexa-api` as TV1 preferred JWT audience; TV3 as TV1-preferred Flyway coordinator.
- **Preserved:** verified UUID and session boundaries, canonical conceal404, frozen payload identity, bounded refresh/reconnect, existing Session V2, no silent old-period inheritance, current ACL on retained history, explicit crossed friend acceptance.
- **Owner routing:** TV1 owns auth/social/privacy; TV2 Messaging schema/Direct/request wire; TV3 membership/backend registry; shared owner approvals still pending.
- **Decision statuses:** TV1 draft 25 AGREE WITH CONDITIONS and 5 NEEDS EVIDENCE D08/D09/D10/D23/D25; upstream FIX3 untouched 21 OPEN/9 CONFLICT/0 APPROVED.
- **New targeted review blockers:** AUTH-01/AUTH-02 password login wire/secure reset; D05 request permissions/state/anti-spam; D08 registry and real database lineage.
- **PR #13:** official approval reported; this handoff does not merge any PR. PR #16 can receive docs only.
