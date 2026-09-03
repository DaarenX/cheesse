# 11. Risks and Technical Debt

This section outlines known technical risks, architectural trade-offs, and tracked technical debt in Cheesse, along with planned mitigation strategies.

---

## 11.1 Identified Technical Risks

| Risk ID | Risk Title | Severity | Impact | Mitigation Strategy |
|---|---|---|---|---|
| **R-01** | **In-Memory Session Registry (Horizontal Scaling Limit)** | Medium | `GameSessionWebSocketHandler` keeps WebSocket sessions in an in-memory `ConcurrentHashMap<Long, MutableSet<WebSocketSession>>`. If the backend is scaled horizontally across multiple instances behind a load balancer, two players in the same game connected to different backend instances cannot exchange messages. | Introduce a Redis Pub/Sub or RabbitMQ messaging broker to distribute WebSocket broadcasts across multiple backend nodes. |
| **R-02** | **Threefold Repetition Detection via FEN Alone** | Resolved | A FEN string does not capture earlier positions, so threefold repetition was never detected. | Resolved by ADR-007: games persist `start_fen` + `move_history` and the adapter replays the full history. Games started before the migration only count repetitions from the position they had at migration time. |
| **R-05** | **Full History Replay per Move** | Low | Every `applyMove` / `getBoardState` call replays the entire game, so cost grows linearly with game length (chess-core regenerates legal moves per replayed move). Negligible for normal game lengths. | If profiling shows a problem, cache a live `ChessGame` per game session inside the adapter layer. |
| **R-03** | **Unbounded Database Growth from Abandoned Lobbies** | Medium | When users create games but never share the `joinToken`, or when players abandon games midway through, entries remain in the `games` table indefinitely. | Implement a background scheduled cleanup job (`@Scheduled`) to evict or archive inactive games older than 24 hours. |
| **R-04** | **Reconnection & Network Flakiness** | Medium | If a mobile client loses cellular connection briefly, the WebSocket drops. Currently, the client must re-open the match manually. | Implement automatic client-side exponential backoff reconnection in `ChessServerService` with session resumption tokens. |

---

## 11.2 Tracked Technical Debt

### 1. Game Clock & Time Constraints
- **Current State**: Games have no time limit; players can take unlimited time to move.
- **Remediation Plan**: Introduce a game time control parameter (e.g., 3+2 Blitz, 10+0 Rapid) at lobby creation. Track remaining player clock time server-side and deduct duration between move timestamps.

### 2. Resignation & Draw Offer Protocol
- **Current State**: Players can only end games through checkmate or board-detected draws.
- **Remediation Plan**: Add `ClientMessage.Resign` and `ClientMessage.OfferDraw` / `AcceptDraw` variants to the shared message protocol.

### 3. Shareable Direct Links
- **Current State**: Opponents must manually copy and paste the join token into a text field.
- **Remediation Plan**: Support deep linking (e.g., `https://cheesse.app/join/{token}`) so opponents can join instantly with one click.
