---
name: bug-triage-and-fix
description: Reproduce a Cheesse bug with a failing test (server integration test or shared commonTest), find the cause along the REST/WebSocket call path, then fix. Use for bug reports, failing tests, or broken multiplayer sync/move validation.
---

# Bug Triage and Fix

## 1. Reproduce with a failing test

**Server (REST / WebSocket / persistence)** — add a test to `server/src/test/kotlin/xyz/daaren/cheesse/GameAuthIntegrationTest.kt` (or a sibling class). It is a `@SpringBootTest` with Testcontainers Postgres, so Docker must be running.
- HTTP: the `webTestClient()` helper in that class.
- WebSocket: the private helpers `openWebSocket(...)` and `exchangeWebSocketMessage(...)` in that class.

```bash
./gradlew :server:test --tests "xyz.daaren.cheesse.GameAuthIntegrationTest.<test name>"
```

**Chess logic / serialization** — add a test in `shared/src/commonTest/`, e.g. next to `xyz.daaren.cheesse.domain.chess.ChessEngineAdapterTest`:
```bash
./gradlew :shared:jvmTest --tests "xyz.daaren.cheesse.domain.chess.ChessEngineAdapterTest"
```

**Client state** — `composeApp/src/commonTest/` (`GameSessionTest`, `GameScreenModelTest`), run with `./gradlew :composeApp:jvmTest`.

**Manual**: `docker compose up -d && ./gradlew :server:bootRun`, then hit `http://localhost:8080` or `ws://localhost:8080/ws/game-session/{id}?playerToken={token}`.

## 2. Find the cause

Call paths:
- REST: `GameController` → `GameService` → `GameRepository` (errors mapped by `GameExceptionHandler`).
- WebSocket: `GameSessionWebSocketHandler` authenticates the `playerToken` query param via `GameService.authenticateParticipant`, then passes moves to `GameService.executeMove`, which checks turn and legality via `ChessEngineAdapter` and persists. Results become `SessionAction.Broadcast` / `Reply` / `Close`.
- Live sessions are tracked in memory per game (`sessionsByGameId`), so they are lost on restart and not shared across instances.

Moves are UCI strings (`e2e4`); board state is FEN.

## 3. Fix and verify

Make the targeted test pass, then run `./gradlew build`.
