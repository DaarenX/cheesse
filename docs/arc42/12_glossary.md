# 12. Glossary

This glossary defines the domain and technical terminology used throughout Cheesse's architecture and codebase.

---

## 12.1 Chess Domain Terminology

| Term | Definition |
|---|---|
| **FEN (Forsyth–Edwards Notation)** | A standard compact notation system for describing a particular board position of a chess game in a single line of text (e.g., piece placement, active color, castling availability, en passant target, halfmove clock, and fullmove number). |
| **PGN (Portable Game Notation)** | A standard plain text format for recording chess matches (both the moves and related metadata such as players, event, and result). |
| **UCI (Universal Chess Interface)** | An open communication protocol that represents moves using algebraic coordinate pairs indicating the starting and ending squares (e.g. `e2e4`, `g1f3`, or `e7e8q` for promotion). |
| **Checkmate** | A game position in which a player's king is under immediate attack (in check) and there is no legal move to escape the attack. Results in an immediate win for the attacker. |
| **Stalemate** | A game position in which the player whose turn it is to move has no legal moves and their king is not in check. Results in a draw. |
| **Threefold Repetition** | A rule stating that a game is drawn if the identical board position (including turn and castling/en-passant rights) occurs three times during the match. |
| **Promotion** | When a pawn reaches the eighth rank, it is converted to the player's choice of a Queen, Rook, Bishop, or Knight of the same color. |

---

## 12.2 Technical & Architectural Terminology

| Term | Definition |
|---|---|
| **Join Token** | A unique 128-bit UUID generated when a multiplayer lobby is created. Shared out-of-band by the host player so an opponent can claim the open seat. |
| **Player Token** | A private 128-bit UUID issued exclusively to an authenticated participant (Host or Guest). Required as a query parameter when connecting to the match WebSocket to prevent unauthorized move injection. |
| **Kotlin Multiplatform (KMP)** | A technology developed by JetBrains enabling developers to write single-source Kotlin logic that compiles to multiple targets (JVM, JavaScript, Wasm, iOS native, Android). |
| **Compose Multiplatform** | A declarative UI toolkit for Kotlin, extending Google's Jetpack Compose to Desktop, Web (Wasm), Android, and iOS. |
| **Spring WebFlux** | The non-blocking reactive web framework introduced in Spring Framework 5, built on Project Reactor and Netty. |
| **R2DBC** | Reactive Relational Database Connectivity. A non-blocking API specification for relational databases in asynchronous, event-driven architectures. |
| **Voyager** | A multiplatform navigation library for Compose supporting tabs, bottom sheets, screens, transitions, and `ScreenModel` lifecycle management. |
| **ScreenModel** | Voyager's multiplatform equivalent of Android's `ViewModel`, managing screen-scoped coroutine lifecycles and reactive states. |
| **Kodein DI** | A lightweight, fast multiplatform dependency injection framework for Kotlin. |
| **ChessEngineAdapter** | A multiplatform domain port/adapter in `:shared` isolating third-party chess library operations (`chess-core`) from the server, client ViewModels, and Compose UI layers. |
