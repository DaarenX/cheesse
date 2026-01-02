package xyz.daaren.cheesse.game

import cafe.adriel.voyager.core.model.screenModelScope
import io.github.alluhemanth.chess.core.ChessGame
import io.github.alluhemanth.chess.core.move.Move
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class GameScreenModel : GameScreenModelApi {
    private val internalGame = ChessGame()
    override val gameState =
        MutableStateFlow(
            GameScreenModelApi.GameState(
                piecePositions = internalGame.getBoard().getAllPieces(),
                legalMoves = internalGame.getLegalMoves(),
            ),
        )

    override fun makeMove(move: Move) {
        internalGame.makeMove(move)
        screenModelScope.launch {
            gameState.emit(
                GameScreenModelApi.GameState(
                    piecePositions = internalGame.getBoard().getAllPieces(),
                    legalMoves = internalGame.getLegalMoves(),
                ),
            )
        }
    }
}
