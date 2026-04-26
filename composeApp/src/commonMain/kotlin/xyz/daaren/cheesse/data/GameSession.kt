package xyz.daaren.cheesse.data

import io.github.alluhemanth.chess.core.ChessGame
import io.github.alluhemanth.chess.core.utils.FenUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import xyz.daaren.cheesse.api.ClientMessage
import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.api.ServerMessage
import xyz.daaren.cheesse.makeUciMoveWorkaround

interface GameSession {
    val playerColor: PlayerColor
    val updates: Flow<String> // TODO data class for fen + last move / sealed class for move and error?

    suspend fun sendMove(moveUci: String)
}

class OfflineGameSession : GameSession {
    private val _updates = MutableStateFlow(FenUtils.DEFAULT_FEN)
    override val updates = _updates.asSharedFlow()

    private val internalChessGame = ChessGame()

    override val playerColor: PlayerColor = PlayerColor.WHITE

    override suspend fun sendMove(moveUci: String) {
        if (internalChessGame.makeUciMoveWorkaround(moveUci)) {
            _updates.emit(internalChessGame.getFen())
        }
    }
}

data class MultiplayerGameSession(
    val id: Long,
    val playerToken: String,
    override val playerColor: PlayerColor,
    val gameSessionConnection: GameSessionConnection,
) : GameSession {
    override val updates = gameSessionConnection.updates.filterIsInstance<ServerMessage.Move>().map { it.fen }

    override suspend fun sendMove(moveUci: String) {
        gameSessionConnection.sendMove(ClientMessage.Move(moveUci = moveUci))
    }
}

class GameSessionConnection(
    val updates: Flow<ServerMessage>,
    val sendMove: suspend (ClientMessage.Move) -> Unit,
)
