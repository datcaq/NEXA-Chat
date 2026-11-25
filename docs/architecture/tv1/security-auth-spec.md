# Security and Auth Implementation Specification

**PROPOSED / PENDING APPROVAL** · Owner TV1

[Architecture handoff and source evidence](README.md) · [JWT and session lifecycle](jwt-session-lifecycle.md) · [Decision register](cross-team-decision-register.md)

## Requirements and verified baseline

MASTER SPEC AUTH-01 requires registration with canonical E.164, server-verified OTP, a verified user and session, and profile completion. AUTH-02 requires server authentication before Home, safe restore, expiry handling and logout. The login credential alternatives in the specification remain open. **OTP-only login is not frozen.** A password is not assumed for the proposal. Account recovery, phone changes and reassigned numbers need a separate ownership policy. A registration OTP must never take over an existing account. The server issues a stable user UUID independent of the phone number.

On main, the launcher is Auth preview. AuthRepository starts registration challenges only. AuthViewModel scopes its asynchronous work to the current attempt and does not convert a challenge into an authenticated session. PR #14 is Contacts UI Foundation and adds no user search or Auth API. PR #6 consumes JWTs and does not issue them. See E1–E3, E6 and E9 in the [evidence index](README.md#source-evidence-index).

## Proposed end-to-end flow

| Step | Actor and Android | Server / database | Success, error and retry |
| --- | --- | --- | --- |
| Start registration | User selects calling region or enters an international number. ViewModel validates E.164, creates a new attempt, and hands it to a repository adapter. | Auth service validates E.164 again and reserves destination and send budgets. Create an opaque REGISTER challenge tied to the number, purpose, expiry, provider reference and attempt allowance. | Return 202 with challenge ID and server timestamps. Invalid syntax is 400, throttling is 429 with `Retry-After`, temporary provider failure is 503. A retry uses the same send idempotency key. Pre-verification responses must avoid exposing account existence. |
| Start login | Same presentation boundary with an independent LOGIN attempt. This is the OTP-only candidate. | Bind the challenge to LOGIN. Verify may establish a session only for an eligible existing account. | Use comparable public response and error behavior for eligible and ineligible numbers. Do not provision a user on LOGIN. |
| Resend | Enable the action after the server-provided cooldown. | Revalidate purpose, expiry, status and per-number budget. Adopt provider-specific reuse or generation semantics without resetting aggregate attempts. | 202 with current projection. Duplicate action or ambiguous timeout must not send again under the same key. Server decides whether a new opaque ID is needed. |
| Verify | Submit challenge ID and OTP as a string. A stale UI attempt cannot apply the response. | Atomically reserve an attempt, verify outside a long DB transaction, then lock and consume the challenge once. Bind approved provider proof to the challenge and attempt. Atomically provision verified user or select eligible login user, then create session and initial refresh family. Use unique canonical E.164. | 201 registration or 200 login. Wrong/expired/exhausted/consumed challenges use safe public errors. Resolve ambiguous provider responses using authoritative provider status when available. Lost issuance response follows a deliberate reauthentication policy, never blind reuse of consumed proof to mint a second session. |
| Profile initialization | After verified session commit, route incomplete registration to onboarding. | Self-profile is read/updated through authenticated User service. Initial user can carry an incomplete profile and verified-at timestamp. | Retry profile save without duplicating the user. Approve required fields and the transition from onboarding to Home. |

### OTP state and protection

Use a state progression such as reserved/pending → verified and consumed, expired or exhausted. Persist purpose, target, expiry, provider reference, generation, attempt state and redacted audit metadata. Serialize concurrent verification and consumption. Account lock/eligibility and budget decisions are server-side. Apply budgets per challenge, number, IP and provider/environment. A resend must not reset the overall attempt budget. Rate limits and log redaction apply to failures as well as successful sends.

Prefer a provider-managed code lifecycle so our database never contains the code. If the team chooses locally generated codes, generate them with a CSPRNG and store a keyed HMAC over challenge, purpose, generation and code. Keep the key outside the database and use constant-time comparison. An unkeyed hash alone provides weak offline protection for a short code. Never log or store plaintext OTP or credentials. Strip sensitive data from provider errors and telemetry. A local deterministic adapter belongs in isolated test source or a test-only process with separate key material and database. Production with missing configuration fails closed.

## Provider candidates

Provider, budget, country coverage and actual delivery are **PENDING APPROVAL / UNVERIFIED IN PRACTICE**. Published prices are reference rates, not a validated project cost or delivery commitment.

| Provider | Cost and delivery | Test and integration notes |
| --- | --- | --- |
| Twilio Verify | Prior review found public pricing of USD 0.05 per successful verification plus destination/channel fees. SMS attempts may still incur channel costs. | Candidate to pilot with consenting verified recipients and permitted destinations. Measure delivery, latency, retries and fees in Vietnam and the intended demo destinations. Use a server-only adapter, secret injection, timeout and quota. [Pricing](https://www.twilio.com/en-us/verify/pricing) · [Trial](https://www.twilio.com/docs/usage/trials/try-out-verify) |
| Vonage Verify | Prior review found USD 0.06084 per successful verification in the Conversion plan plus applicable charges. Confirm plan and destination costs. | Alternative with managed verification workflows. Test the same numbers and error cases before comparing total cost. [Pricing](https://www.vonage.com/communications-apis/verify/pricing/) |
| Local test provider | No SMS cost or delivery evidence. | Deterministic success, expiry, invalid code, rate-limit and timeout. Separate from production configuration. |

Proposed defaults are **6 digits, 5 minutes, 5 attempts per challenge and 60 seconds resend cooldown**. Validate provider behavior, delivery latency, aggregate budgets and fraud protection before adopting them. Prefer RS256 with an environment-specific issuer and a common API audience once approved. `nexa-chat` and `nexa-chat-api` are candidates. A discovery-based decoder needs configured issuer metadata/JWKS, while a local decoder can validate configured values with trusted verification keys. PR #6 currently defaults to `nexa-api`. Access 15 minutes and refresh 30 days are starting points, evaluated in the [lifecycle proposal](jwt-session-lifecycle.md).

## Proposed REST contract

Use `/api/v1`. Field labels below describe JSON unless noted. UUIDs and UTC timestamps are strings, required and non-null unless stated. Credential strings are opaque to Android presentation. Endpoint labels, status mapping and DTO fields await TV1/TV2/TV3 review.

| Method/path | Permission and input | Success response |
| --- | --- | --- |
| `POST /auth/registration-challenges` or `/auth/login-challenges` | Public. `{ "phoneE164": "<canonical E.164>" }` plus `Idempotency-Key`. Purpose comes from the route. | 202, `{challengeId, expiresAt, resendAvailableAt}`. |
| `POST /auth/challenges/{id}/resend` | Public challenge proof, quota and idempotency key. No client override of purpose or target. | 202, current challenge projection. |
| `POST /auth/registrations/verify` or `/auth/logins/verify` | Public, `{challengeId, otp}`. OTP is a string of the approved length and alphabet. | 201 or 200, `{user:{id,profileComplete},sessionId,accessToken,accessExpiresAt,refreshToken,refreshExpiresAt}`. |
| `POST /auth/refresh` | Public with respect to access JWT, proof is `{refreshToken}`. | 200, rotated token pair with timestamps and same session ID. |
| `POST /auth/logout` | Proposed refresh proof so expired access JWT does not block logout. | Idempotent 204 for a valid or already revoked session under the approved response policy. |
| `GET /users/me`, `PATCH /users/me` | Authenticated Bearer. PATCH accepts only allowlisted profile fields. Nullable fields such as bio require explicit clear semantics. Phone and credential changes use separate workflows. | 200 self-profile. A public profile returned by directory uses a separate minimal DTO. |

Common error envelope `{code,message,requestId}`. Map malformed requests to 400, missing/invalid/expired/revoked credentials to 401, authenticated insufficient permission to 403, concealed resources to 404, non-sensitive business conflict to 409, quota to 429 and dependency failure to 503. Credential responses use `Cache-Control: no-store`. Avoid reflecting raw inputs or provider payloads. Owner tests cover duplicate challenge, concurrent consumption, resend budgets, expiry, replay, account enumeration and outage.

## Backend trust and domain authorization

TV1 owns a single security boundary. Move the shared filter chain, decoder, validated principal mapping and common error rendering out of Groups in an approved backend follow-up. Use explicit method-and-path Auth allowlisting, authenticated routes for approved protected surfaces and deny by default. Verify signature, allowed algorithm/key ID, issuer, audience, required expiry, time validity and canonical UUID subject. Resolve a trusted `(userId, sessionId)` once on the server for both REST and WebSocket. Use one request ID across filter, advice and sanitized diagnostics. Keep private keys in deployment secret storage. Rotate by signing with the new key and retaining trusted verification keys through the access lifetime plus skew unless emergency rotation requires earlier revocation. Never accept key endpoints or identity claims supplied independently by clients.

The current Groups controller parses `sub` locally, authenticates all routes and has a separate error path. Domain services must still check friendship/block policy and ACTIVE membership at read/write, subscription, delivery, media or translation admission. Neither a valid JWT nor a PeopleDirectory eligibility hint grants conversation access. See E8–E11 in the [evidence index](README.md#source-evidence-index). [Spring JWT validation](https://docs.spring.io/spring-security/reference/6.5/servlet/oauth2/resource-server/jwt.html) · [RFC 8725](https://www.rfc-editor.org/rfc/rfc8725.html)
