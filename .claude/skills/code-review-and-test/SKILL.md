---
name: code-review-and-test
description: Cheesse-specific review checklist (KMP source sets, serialization contracts, WebFlux non-blocking, R2DBC schema sync, chess engine access). Use when reviewing a change or before declaring a task done.
---

# Code Review and Test

Commands live in `AGENTS.md` §2. Definition of done: `./gradlew build` passes (that is what CI runs, plus an iOS `xcodebuild`). Server tests need Docker running for Testcontainers.

## Checklist

- [ ] **KMP source sets**: no platform APIs in `commonMain`. Platform code goes in `jvmMain`, `androidMain`, `iosMain` or `webMain`.
- [ ] **Chess engine access**: no `xyz.daaren.chess.core.ChessGame` outside `DefaultChessEngineAdapter` (`shared/.../domain/chess/`).
- [ ] **Serialization contracts**: changes to `ClientMessage` / `ServerMessage` (`GameMoveMessage.kt`), `GameRequests.kt` or `GameResponses.kt` keep `@Serializable` intact and stay compatible with already-deployed clients (the web client is served from GitHub Pages and may be older than the server).
- [ ] **Non-blocking server**: no `.block()`, `Thread.sleep()` or blocking I/O inside WebFlux publishers or suspend functions.
- [ ] **Schema sync**: field changes in `GameEntity.kt` are mirrored in `server/src/main/resources/schema.sql` and `GamePersistenceMapper.kt`.
- [ ] **REST errors**: new failure cases are domain exceptions in `GameExceptions.kt` mapped in `GameExceptionHandler`, not try/catch in `GameController`.
- [ ] **Tests**: new endpoints / WebSocket flows have integration tests in `GameAuthIntegrationTest` (or a sibling class); shared logic is tested in `commonTest`.
- [ ] **Docs**: if the change touches an area in the arc42 mapping table, run the `arc42-documentation-maintenance` skill.
