# 6. Runtime View

The runtime view explains the dynamic behavior and interactions between Cheesse's building blocks during primary operating scenarios.

---

## 6.1 Scenario 1: Game Creation & Lobby Waiting

This scenario illustrates how a host player initiates a multiplayer game, obtains credentials, and establishes a WebSocket connection while waiting for an opponent.

```mermaid
sequenceDiagram
    autonumber
    actor Host as Host Player
    participant UI as HomeScreen UI
    participant Model as HomeScreenModel
    participant Service as ChessServerService
    participant Backend as GameController (:server)
    participant GS as GameService
    participant Repo as GameRepository
    participant DB as PostgreSQL
    participant WSHandler as GameSessionWebSocketHandler

    Host->>UI: Clicks "New Game", selects Color
    UI->>Model: createGame(colorPreference)
    Model->>Service: createGame(colorPreference)
    Service->>Backend: POST /games {"color": "..."}
    Backend->>GS: createGame(colorPreference)
    GS->>Repo: save(GameEntity(joinToken, white/black player))
    Repo->>DB: INSERT INTO games ...
    DB-->>Repo: Saved Game ID
    Repo-->>GS: Saved Game
    GS-->>Backend: CreatedGame(gameId, joinToken, playerId, playerToken, color)
    Backend-->>Service: 201 Created (CreateGameResponse)
    Service-->>Model: CreateGameResponse
    Model->>Service: connectToGameSession(gameId, playerToken)
    Service->>WSHandler: WebSocket Connect /ws/game-session/{id}?playerToken={token}
    WSHandler->>GS: authenticateParticipant(gameId, playerToken)
    GS-->>WSHandler: AuthenticatedParticipant
    WSHandler->>WSHandler: Register session in sessionsByGameId[gameId]
    Model->>UI: Update HomeUiState(waitingLobby = WaitingLobby(joinToken))
    UI-->>Host: Displays "Share this join token: <UUID>"
```

---

## 6.2 Scenario 2: Opponent Joins & Match Starts

This scenario demonstrates the joining flow, mutual WebSocket presence detection, and game start event broadcast.

```mermaid
sequenceDiagram
    autonumber
    actor Guest as Opponent
    participant GuestUI as Guest HomeScreen UI
    participant GuestModel as Guest HomeScreenModel
    participant GuestService as Guest ChessServerService
    participant Backend as GameController (:server)
    participant GS as GameService
    participant Repo as GameRepository
    participant DB as PostgreSQL
    participant WSHandler as GameSessionWebSocketHandler
    participant HostService as Host ChessServerService

    Guest->>GuestUI: Enters joinToken, clicks "JOIN"
    GuestUI->>GuestModel: joinGame(token)
    GuestModel->>GuestService: joinGame(token)
    GuestService->>Backend: POST /games/join {"token": "..."}
    Backend->>GS: joinGame(token)
    GS->>Repo: assignWhitePlayer(...) or assignBlackPlayer(...)
    Repo->>DB: UPDATE games SET ... WHERE id = :id AND ..._id IS NULL
    DB-->>Repo: 1 row affected
    GS-->>Backend: JoinedGame(gameId, playerId, playerToken, color)
    Backend-->>GuestService: 200 OK (JoinGameResponse)
    GuestService-->>GuestModel: JoinGameResponse
    GuestModel->>GuestService: connectToGameSession(gameId, playerToken)
    GuestService->>WSHandler: WebSocket Connect /ws/game-session/{id}?playerToken={token}
    WSHandler->>WSHandler: Register Guest session in sessionsByGameId[gameId]
    WSHandler->>WSHandler: notifyGameStartIfReady(gameId)
    Note over WSHandler: activeSessions == 2 & both players assigned
    WSHandler-->>HostService: WebSocket Broadcast: ServerMessage.GameStart
    WSHandler-->>GuestService: WebSocket Broadcast: ServerMessage.GameStart
    HostService-->>GuestModel: gameStartEvents.emit(MultiplayerGameSession)
    GuestService-->>GuestModel: gameStartEvents.emit(MultiplayerGameSession)
    Note over GuestUI,Host: Both clients automatically push GameScreen(session)
```

---

## 6.3 Scenario 3: Real-Time Move Execution & Validation

This scenario details how a player makes a move on the board, how the server verifies it authoritatively, and how the new state is propagated.

```mermaid
sequenceDiagram
    autonumber
    actor Player as White Player
    participant UI as GameScreen UI
    participant Model as GameScreenModel
    participant Session as MultiplayerGameSession
    participant WSHandler as GameSessionWebSocketHandler
    participant GS as GameService
    participant Engine as ChessEngineAdapter
    participant Repo as GameRepository
    participant OpponentSession as Opponent MultiplayerGameSession

    Player->>UI: Drags White Pawn e2 -> e4
    UI->>Model: makeMove("e2e4")
    Model->>Session: sendMove("e2e4")
    Session->>WSHandler: ClientMessage.Move("e2e4")
    WSHandler->>GS: executeMove(gameId, playerId, WHITE, "e2e4")
    GS->>Repo: findById(gameId)
    Repo-->>GS: Current GameEntity (FEN, start FEN, move history)
    GS->>Engine: activePlayerColor(fen)
    Note over GS,Engine: Checks if active color matches WHITE
    GS->>Engine: applyMove(history, "e2e4")
    Note over GS,Engine: Replays history, rejects with GameOver if the game already ended
    Engine-->>GS: ChessMoveResult.Success(newHistory, newFen, newPgn)
    GS->>Repo: save(gameEntity.withHistory(newHistory, newFen, newPgn))
    Repo-->>GS: Saved Game
    GS-->>WSHandler: MoveExecutionResult.Success(savedGame)
    WSHandler-->>Session: WebSocket Broadcast: ServerMessage.Move(newFen, startFen, movesUci)
    WSHandler-->>OpponentSession: WebSocket Broadcast: ServerMessage.Move(newFen, startFen, movesUci)
    Session->>Model: updates Flow emits ChessGameHistory
    OpponentSession->>Model: updates Flow emits ChessGameHistory
    Model->>UI: Renders updated piece positions, highlights, and flips turn
```

---

## 6.4 Scenario 4: Local Pass-and-Play Gameplay

For local offline gameplay, no network activity occurs:

1. User clicks **"New local Game"** on `HomeScreen`.
2. `HomeScreenModel.createLocalGame()` instantiates an `OfflineGameSession`.
3. `OfflineGameSession` is initialized with an empty `ChessGameHistory` (standard opening FEN, no moves) and delegates move evaluation to `ChessEngineAdapter`.
4. When either player moves:
   - `GameScreenModel.makeMove(moveUci)` invokes `OfflineGameSession.sendMove(moveUci)`.
   - `OfflineGameSession.isTurnAllowed(color)` returns `true` regardless of turn color.
   - `chessEngineAdapter.applyMove(currentHistory, moveUci)` evaluates legality locally.
   - On success, `_updates.emit(result.history)` triggers recomposition on `GameScreen`.
