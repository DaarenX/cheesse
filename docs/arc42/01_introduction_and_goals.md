# 1. Introduction and Goals

Cheesse is a modern, cross-platform multiplayer chess application built on Kotlin Multiplatform (KMP) and Compose Multiplatform, backed by a reactive Spring Boot service with PostgreSQL persistence. It allows players to play chess locally (pass-and-play) or online in real-time with peer-to-peer matchmaking through shareable join tokens.

---

## 1.1 Requirements Overview

### 1.1.1 Core Functional Goals

- **Local Pass-and-Play (`OfflineGameSession`)**:
  - Two players can play on a single shared screen on any supported client platform.
  - Turn rotation, legal move calculation, piece capture tracking, and game over state detection (checkmate, stalemate, draw rules) occur entirely locally without server round-trips.

- **Online Multiplayer Matchmaking (`MultiplayerGameSession`)**:
  - **Lobby Creation**: A user can create a multiplayer game, specifying a color preference (`WHITE`, `BLACK`, or `RANDOM`). The backend allocates a unique game instance, generates an anonymous `playerToken` for the host, and a shareable `joinToken`.
  - **Lobby Joining**: An opponent enters the shareable `joinToken` to join the waiting game. The backend validates token validity, ensures no duplicate joins occur, assigns the remaining color, and generates the joiner's `playerToken`.

- **Real-Time Synchronized Gameplay**:
  - Both players connect via persistent WebSockets to `/ws/game-session/{id}?playerToken={playerToken}`.
  - When both players are connected, the server automatically broadcasts a `ServerMessage.GameStart` event.
  - Moves are sent as UCI strings (`e2e4`, `g1f3`, `e7e8q` for promotion) over WebSockets.
  - Server strictly validates turn sequence, move legality, and maintains authoritative FEN and PGN states.
  - Verified move updates are broadcast to both clients instantly as FEN snapshots.

- **Interactive Chessboard UI**:
  - Drag-and-drop piece manipulation with responsive pixel-to-board coordinate mapping.
  - Move target indicator dots (circular previews for silent moves, larger rings for captures).
  - Manual board perspective rotation (rotate board 180° for White or Black perspective).
  - Pawn promotion piece selection dialog (Queen, Rook, Bishop, Knight).
  - Game over dialog announcing outcome (Checkmate with winner, Stalemate, Threefold repetition, Fifty-move rule, Insufficient material).
  - Configurable server URL in application settings with dynamic client reconnection.

---

## 1.2 Quality Goals

| Priority | Quality Goal | Motivation & Metric |
|:---|:---|:---|
| **1** | **Low Latency & High Responsiveness** | Real-time chess moves must synchronize between clients within `< 100ms` on typical networks. The backend utilizes non-blocking reactive I/O (Spring WebFlux + R2DBC + Project Reactor) to achieve high session concurrency with minimal thread footprint and memory overhead. |
| **2** | **Multiplatform Consistency & Code Reuse** | Common chess domain rules, network DTOs, serialization protocols, and UI components must be shared seamlessly across Desktop (JVM), Web (Wasm), Android, and iOS. UI behavior and piece movements must behave identically on all target platforms. |
| **3** | **Server-Authoritative Game Integrity** | Clients cannot manipulate turn order, game board state, or make illegal moves. All state transitions, legality checks, and player authentication are strictly validated on the backend before being committed to PostgreSQL and broadcast. |
| **4** | **Frictionless Anonymous Onboarding** | Players can create lobbies and join games instantly without requiring email registration, password creation, or third-party OAuth authentication. Secure random UUID tokens prevent unauthorized move injection. |

---

## 1.3 Stakeholders

| Role / Stakeholder | Goal & Perspective | Expectations from Architecture |
|---|---|---|
| **End Users / Chess Players** | Play frictionless chess games locally or with friends online. | Instant UI feedback, clear board visuals, low lag, no forced registration. |
| **Application Maintainers** | Maintain and evolve the codebase across 4 target platforms. | Unified Kotlin codebase, maximum code sharing, clean separation of concerns, fast feedback test suites. |
| **Backend & Infrastructure Operators** | Host the backend server at minimal resource cost. | Lightweight non-blocking reactive stack, low memory footprint, Docker containerization, easy PostgreSQL setup. |
| **Open Source Contributors** | Add features (move history, chess clocks, Elo matchmaking). | Standardized architecture (arc42), clear conventions (Ktlint, Gradle version catalogs), modular boundaries. |
