package xyz.daaren.cheesse.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import xyz.daaren.cheesse.api.ClientMessage
import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.api.ServerMessage
import xyz.daaren.cheesse.domain.chess.ChessEngineAdapter
import xyz.daaren.cheesse.domain.chess.ChessGameHistory
import xyz.daaren.cheesse.domain.chess.ChessMoveResult
import xyz.daaren.cheesse.domain.chess.DefaultChessEngineAdapter

interface GameSession {
    val playerColor: PlayerColor
    val updates: Flow<ChessGameHistory>

    fun isTurnAllowed(color: PlayerColor): Boolean

    suspend fun sendMove(moveUci: String)
}

class OfflineGameSession(
    private val chessEngineAdapter: ChessEngineAdapter = DefaultChessEngineAdapter(),
    initialHistory: ChessGameHistory = ChessGameHistory(),
) : GameSession {
    private val _updates = MutableStateFlow(initialHistory)
    override val updates = _updates.asSharedFlow()

    override val playerColor: PlayerColor = PlayerColor.WHITE

    override fun isTurnAllowed(color: PlayerColor): Boolean = true

    override suspend fun sendMove(moveUci: String) {
        val result = chessEngineAdapter.applyMove(_updates.value, moveUci)
        if (result is ChessMoveResult.Success) {
            _updates.emit(result.history)
        }
    }
}

data class MultiplayerGameSession(
    val id: Long,
    val playerToken: String,
    override val playerColor: PlayerColor,
    val gameSessionConnection: GameSessionConnection,
) : GameSession {
    override val updates = gameSessionConnection.updates.filterIsInstance<ServerMessage.Move>().map { it.toHistory() }

    override fun isTurnAllowed(color: PlayerColor): Boolean = color == playerColor

    override suspend fun sendMove(moveUci: String) {
        gameSessionConnection.sendMove(ClientMessage.Move(moveUci = moveUci))
    }
}

// Servers that predate history in ServerMessage.Move only send the FEN; repetition detection then starts there.
private fun ServerMessage.Move.toHistory(): ChessGameHistory =
    startFen?.let { ChessGameHistory(it, movesUci) } ?: ChessGameHistory(startFen = fen)

class GameSessionConnection(
    val updates: Flow<ServerMessage>,
    val sendMove: suspend (ClientMessage.Move) -> Unit,
)
