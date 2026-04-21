package xyz.daaren.cheesse.ui.game

import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alluhemanth.chess.core.ChessGame
import io.github.alluhemanth.chess.core.move.Move
import io.github.alluhemanth.chess.core.piece.PieceColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import xyz.daaren.cheesse.api.ClientMessage
import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.api.ServerMessage
import xyz.daaren.cheesse.data.GameSession

class GameScreenModel(
    private val gameSession: GameSession,
) : GameScreenModelApi {
    private val internalGame = ChessGame()

    override val gameState = MutableStateFlow(internalGame.toGameState())

    init {
        screenModelScope.launch {
            gameSession.gameSessionConnection.updates.collect {
                if (it is ServerMessage.Move) {
                    internalGame.loadFen(it.fen)
                    gameState.emit(internalGame.toGameState())
                }
            }
        }
    }

    override fun makeMove(move: Move) {
        internalGame.makeMove(move)
        screenModelScope.launch {
            gameSession.gameSessionConnection.sendMove(ClientMessage.Move(move.toUci()))
            gameState.emit(internalGame.toGameState())
        }
    }

    private fun ChessGame.toGameState(): GameScreenModelApi.GameState =
        GameScreenModelApi.GameState(
            piecePositions = getBoard().getAllPieces(),
            legalMoves = if (getCurrentPlayer().toPlayerColor() == gameSession.playerColor) getLegalMoves() else emptyList(),
        )

    private fun PieceColor.toPlayerColor(): PlayerColor =
        when (this) {
            PieceColor.WHITE -> PlayerColor.WHITE
            PieceColor.BLACK -> PlayerColor.BLACK
        }
}
