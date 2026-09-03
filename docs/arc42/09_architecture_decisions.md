# 9. Architecture Decisions (ADRs)

This section records significant architectural decisions (ADRs) made in Cheesse, capturing the context, options considered, decisions, and consequences.

---

## ADR-001: Kotlin Multiplatform & Compose Multiplatform for Cross-Platform UI

- **Status**: Accepted
- **Date**: 2026-09-03
- **Context**: Cheesse requires cross-platform execution on Desktop (JVM), Web (Wasm), Android, and iOS. Implementing distinct native frontends (SwiftUI, Jetpack Compose, React/TypeScript) would quadruple development and maintenance overhead.
- **Decision**: Adopt Kotlin Multiplatform (KMP) and Compose Multiplatform for common code, networking, and UI presentation across all 4 platforms.
- **Consequences**:
  - *Positive*: 100% of UI components, game models, and network serialization logic are shared.
  - *Positive*: Native performance and look-and-feel across all supported devices.
  - *Negative*: WebAssembly (Wasm) tooling requires modern browser support; iOS requires Xcode integration.

---

## ADR-002: Reactive Spring Boot WebFlux & Spring Data R2DBC

- **Status**: Accepted
- **Date**: 2026-09-03
- **Context**: Real-time chess matches require keeping hundreds or thousands of concurrent WebSocket connections open. Traditional blocking servlet threads (Tomcat, standard JDBC) consume excessive system memory and degrade under thread exhaustion.
- **Decision**: Build the `:server` backend using reactive Spring WebFlux and Spring Data R2DBC with Project Reactor and Netty.
- **Consequences**:
  - *Positive*: Ultra-low memory usage and high concurrency handling via Netty's event loop.
  - *Positive*: Asynchronous database I/O to PostgreSQL without blocking application threads.
  - *Negative*: Developers must adhere strictly to non-blocking patterns; no blocking libraries can be introduced.

---

## ADR-003: WebSocket Protocol for In-Game Synchronization

- **Status**: Accepted
- **Date**: 2026-09-03
- **Context**: Moves in a chess game require instant synchronization with minimal transport latency. Polling via HTTP REST introduces unnecessary network overhead and latency delays.
- **Decision**: Use WebSockets (`/ws/game-session/{id}?playerToken={token}`) for bidirectional in-game message dispatch, while retaining REST for stateless lobby operations (`/games`, `/games/join`).
- **Consequences**:
  - *Positive*: Sub-millisecond messaging overhead with full-duplex transmission.
  - *Positive*: Immediate server push for `GameStart`, move updates, and game over states.
  - *Negative*: Persistent connections require connection lifecycle tracking and keep-alive handling.

---

## ADR-004: Unified Multiplatform Chess Engine Abstraction via Shared `ChessEngineAdapter`

- **Status**: Accepted (Updated)
- **Date**: 2026-09-03 (Updated 2026-09-15)
- **Context**: In online and local chess, exposing third-party chess libraries (`chess-core`) directly across WebSocket handlers, screen models, and Compose UI composables tightly couples the entire application to third-party types (`Piece`, `Square`, `GameResult`). Furthermore, untrusted clients must not be trusted for move validation in multiplayer games, yet client UIs require rich board inspection (legal moves, piece positions, captured pieces, and game termination).
- **Decision**: Centralize chess engine operations behind a multiplatform `ChessEngineAdapter` interface (`DefaultChessEngineAdapter`) located in `:shared` with domain models (`ChessPiece`, `ChessPieceType`, `ChessSquare`, `ChessMove`, `ChessGameResult`, `ChessBoardState`). In `:server`, inject `ChessEngineAdapter` into `GameService` for authoritative validation. In `:composeApp`, inject it into `GameScreenModel` and `OfflineGameSession`, removing direct dependencies on `chess-core` from client modules.
- **Consequences**:
  - *Positive*: Total cheat immunity: illegal moves and turn violations are validated authoritatively on the backend.
  - *Positive*: Zero library leakage: Compose UI components and ScreenModels depend only on clean domain models.
  - *Positive*: Single source of truth: shared adapter handles move validation, board state extraction, and captured piece math for all platforms.
  - *Positive*: Unit testable: ScreenModels and backend services can be tested with fakes without executing full engine calculations.
  - *Positive*: Future engine replaceability: swapping `chess-core` for Stockfish or native engines only requires modifying `DefaultChessEngineAdapter`.

---

## ADR-005: Anonymous Token-Based Matchmaking & Authentication

- **Status**: Accepted
- **Date**: 2026-09-03
- **Context**: Requiring registration, email confirmation, and passwords imposes friction that discourages casual players from creating and joining games.
- **Decision**: Implement token-based anonymous authentication. A `joinToken` is public and shared out-of-band to claim a seat. A separate `playerToken` is kept private by each participant to authenticate WebSocket move submissions.
- **Consequences**:
  - *Positive*: Zero friction: matches can be created and joined in seconds.
  - *Positive*: High security: opponent cannot impersonate the other player or guess the 128-bit UUID `playerToken`.
  - *Negative*: If a user clears their browser cache or loses their session without saving the URL, recovering an ongoing match requires re-obtaining the token.

---

## ADR-006: Voyager Navigation & ScreenModel MVI State Management

- **Status**: Accepted
- **Date**: 2026-09-03
- **Context**: Standard Jetpack Compose navigation is Android-centric and cumbersome to configure across multiplatform targets (iOS, Desktop JVM, Wasm).
- **Decision**: Adopt Voyager navigation (`cafe.adriel.voyager`) and `ScreenModel` architecture coupled with Kodein DI.
- **Consequences**:
  - *Positive*: Unified multiplatform screen transitions, backstack management, and ViewModel lifecycles.
  - *Positive*: Clean unidirectional data flow (MVI) with reactive `StateFlow` bindings.
  - *Negative*: ScreenModels must be instantiated via Voyager's DI integration (`rememberScreenModel`).

---

## ADR-007: Persist and Replay Full Move History as the Game State

- **Status**: Proposed
- **Date**: 2026-10-25
- **Context**: `ChessEngineAdapter` operated on FEN strings only. Every call loaded the FEN into a fresh `chess-core` `ChessGame`, which discards all earlier positions, so threefold repetition could never be detected (risk R-02). The stored PGN was also wrong: it only contained the last move.
- **Options Considered**:
  1. Store the current FEN only (status quo): simple, but repetition is undetectable and the PGN is broken.
  2. Store the PGN and replay it via SAN: the column already exists, but existing rows hold broken PGN, `chess-core`'s `loadPgn` does not rebuild position history, and SAN parsing is more fragile than UCI.
  3. Store the start FEN plus the list of UCI moves and replay them for every engine call: needs two new columns and O(n) replay per call, but is exact and works from any start position.
  4. Keep a live `ChessGame` per game in memory: fast, but breaks on restart and with multiple server instances (see R-01).
- **Decision**: Use option 3. `ChessGameHistory(startFen, movesUci)` in `:shared` is the input to `applyMove` and `getBoardState`. The server persists it in `games.start_fen` and `games.move_history` (with `fen` and `pgn` kept as derived caches) and broadcasts it in `ServerMessage.Move` via the optional `startFen` / `movesUci` fields. Clients keep a `ChessGameHistory` instead of a FEN. The adapter rejects moves once the game is over (`ChessMoveResult.GameOver`) and reports no legal moves for finished games.
- **Consequences**:
  - *Positive*: Threefold repetition is detected on server and client; the server stops accepting moves after any draw.
  - *Positive*: PGN now contains the whole game for games starting from the standard position.
  - *Positive*: Backwards-compatible protocol: older clients ignore the new fields, and newer clients fall back to the FEN when talking to an older server.
  - *Negative*: Each engine call replays the game (see R-05), and `ServerMessage.Move` grows with game length.
  - *Negative*: Games already in progress at migration time only count repetitions from their position at migration; `chess-core`'s PGN generation assumes the standard start position.
