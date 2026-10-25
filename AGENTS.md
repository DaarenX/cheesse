# Repository Agent Guide (AGENTS.md)

## 1. System Overview
- **Purpose**: Cheesse is a cross-platform multiplayer chess app: real-time games over WebSockets, a Compose Multiplatform client (Android, iOS, Desktop JVM, Web Wasm) and a reactive Spring Boot backend on PostgreSQL.
- **Stack**: Kotlin Multiplatform, Compose Multiplatform, Spring Boot (WebFlux + R2DBC), PostgreSQL, Voyager (navigation), Kodein DI, Ktor Client, `xyz.daaren:chess-core` (own chess engine fork). Versions live in `gradle/libs.versions.toml`.
- **Modules**:
  - `shared/`: API DTOs and WebSocket messages (`api/`), the chess engine abstraction (`domain/chess/ChessEngineAdapter`, `DefaultChessEngineAdapter`), `kotlinx.serialization` models.
  - `server/`: Spring WebFlux backend. REST in `api/game/`, WebSocket in `websocket/game/GameSessionWebSocketHandler.kt`, business logic in `domain/game/GameService.kt`, R2DBC persistence in `persistence/game/`.
  - `composeApp/`: Compose Multiplatform client. Screens + Voyager `ScreenModel`s in `ui/`, networking in `data/` and `network/`. Web source set is `webMain`.
  - `androidApp/`: Android host (`MainActivity`). `iosApp/`: Xcode project wrapping `MainViewController`.

## 2. Commands
- **Definition of done**: `./gradlew build` passes. CI (`.github/workflows/pr-check.yml`) runs exactly that plus an iOS `xcodebuild`. It includes ktlint and the arc42 sync check.
- **Build**: `./gradlew assemble` (skip tests) · `./gradlew :server:bootJar` · `./gradlew :androidApp:assembleDebug` · `./gradlew :composeApp:wasmJsBrowserDistribution` · `./gradlew packageUberJarForCurrentOS`
- **Run**:
  - Database: `docker compose up -d`
  - Server: `./gradlew :server:bootRun` (port 8080)
  - Web client: `./gradlew :composeApp:wasmJsBrowserDevelopmentRun` (port 3000)
  - Desktop client: `./gradlew :composeApp:run`
- **Tests** (server tests use Testcontainers, so Docker must be running):
  - All: `./gradlew test`
  - Server: `./gradlew :server:test --tests "xyz.daaren.cheesse.GameAuthIntegrationTest.<test name>"`
  - Shared: `./gradlew :shared:allTests`, or one class: `./gradlew :shared:jvmTest --tests xyz.daaren.cheesse.domain.chess.ChessEngineAdapterTest`
  - Client: `./gradlew :composeApp:jvmTest --tests xyz.daaren.cheesse.GameSessionTest`
- **Lint**: `./gradlew ktlintCheck` / `./gradlew ktlintFormat`
- **arc42 unified doc**: `./gradlew arc42Assemble` (regenerate) / `./gradlew arc42Check` (verify)

## 3. Conventions
- **Style**: ktlint (`.editorconfig` allows PascalCase for `@Composable`). One JVM target everywhere (see the module build files). The server compiles with `-Xjsr305=strict`.
- **REST errors**: domain exceptions in `domain/game/GameExceptions.kt` are mapped to HTTP statuses by `GameExceptionHandler` (`@RestControllerAdvice`). `GameController` delegates straight to `GameService` with no try/catch or `ResponseStatusException`.
- **WebSocket errors**: recoverable turn/move errors → `SessionAction.Reply(ServerMessage.Error(...))`; unauthenticated → close with `1008 POLICY_VIOLATION`; malformed payload → close with `1007 BAD_DATA`.
- **Messages**: `ClientMessage` / `ServerMessage` are `@Serializable` sealed classes in `shared/.../api/GameMoveMessage.kt`. Keep them backwards-compatible; deployed web clients can be older than the server.
- **Client state**: `ScreenModel`s expose state as `StateFlow` and handle errors without crashing composables.
- **Chess engine access**: all move validation, turn detection and FEN/PGN parsing goes through `ChessEngineAdapter`. `xyz.daaren.chess.core.ChessGame` may only be used inside `DefaultChessEngineAdapter`.
- **Tests**: JUnit 5 + `kotlin.test`, backtick names (``fun `websocket accepts authenticated players only`()``). New endpoints and WebSocket flows need integration tests in `server/src/test/` (`@SpringBootTest(RANDOM_PORT)` + Testcontainers Postgres). Shared logic is tested in `commonTest`.

## 4. Skills
Skills live in `.claude/skills/<name>/SKILL.md` (Claude Code loads them automatically; other agents should read the relevant file):
- `code-review-and-test`: project-specific review checklist.
- `bug-triage-and-fix`: reproduce-first debugging, call paths, test helpers.
- `arc42-documentation-maintenance`: which arc42 chapters to update for which code.
- `adr-management`: ADR format and procedure.

Update a skill only when it was wrong or missed a step that cost time. Don't add skills for one-off procedures.

## 5. Architecture Documentation
- Architecture is documented in [`docs/arc42/`](docs/arc42/index.md) (arc42 chapters 1–12).
- **ADRs live in [`docs/arc42/09_architecture_decisions.md`](docs/arc42/09_architecture_decisions.md). This project has no `docs/adr/` directory and none should be created.**
- When changing REST endpoints, WebSocket messages, domain models, persistence or deployment config, update the matching chapters (see the `arc42-documentation-maintenance` skill), then run `./gradlew arc42Assemble`. Never edit `cheesse-architecture-arc42.md` by hand.
