# NEXA-Chat
NEXA Chat - Android real-time messaging application with group chat, multimedia, multilingual translation and smart features.

## GROUP-01

Android Java/XML group screens and a Spring Boot/PostgreSQL invitation domain are available on this feature branch.
Main at 1945282 includes Auth UI preview and the Home four-tab foundation, but no production authentication, contacts or messaging implementation.
Production adapters must be supplied by TV1/TV2; missing integrations fail closed.
MainActivity and its Auth layout are retained from TV1. Groups exposes an authenticated-account integration point for home_panel_groups.

- [Feature contract and integration guide](docs/group-01/README.md)
- [Verification report and screenshots](docs/group-01/verification.md)
- [Groups account scope and Session V2 adapter](docs/group-01/session-v2-integration.md)
- [Backend baseline split proposal awaiting team approval](docs/group-01/backend-baseline-proposal.md)

Backend: Java 21, Maven 3.6.3+, Docker (for PostgreSQL integration tests).
Run `mvn verify` in `backend/`. Android uses the existing Gradle wrapper and SDK 37:
`gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:connectedDebugAndroidTest`.
An emulator/device is required for connected tests.

## AI assistance

Codex assisted with GROUP-01 implementation, migration, tests and documentation.
Hoàng Phúc and the human reviewers must inspect the code, understand the transaction/security boundaries,
run the recorded checks in their environment and approve the integration contracts before merging.
