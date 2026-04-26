package xyz.daaren.cheesse.ui.game

import cafe.adriel.voyager.core.model.ScreenModel
import io.github.alluhemanth.chess.core.board.Square
import io.github.alluhemanth.chess.core.game.GameResult
import io.github.alluhemanth.chess.core.move.Move
import io.github.alluhemanth.chess.core.piece.Piece
import kotlinx.coroutines.flow.StateFlow
import xyz.daaren.cheesse.api.PlayerColor

interface GameScreenModelApi : ScreenModel {
    val gameState: StateFlow<GameState>

    fun makeMove(moveUci: String)

    data class GameState(
        val piecePositions: List<Pair<Piece, Square>>,
        val legalMoves: List<Move>,
        val turn: PlayerColor,
        val capturedPieces: List<Piece>,
        val gameResult: GameResult = GameResult.Ongoing,
    )
}
