package xyz.daaren.cheesse.ui.game

import cafe.adriel.voyager.core.model.ScreenModel
import io.github.alluhemanth.chess.core.board.Square
import io.github.alluhemanth.chess.core.move.Move
import io.github.alluhemanth.chess.core.piece.Piece
import kotlinx.coroutines.flow.StateFlow

interface GameScreenModelApi : ScreenModel {
    val gameState: StateFlow<GameState>

    fun makeMove(move: Move)

    data class GameState(
        val piecePositions: List<Pair<Piece, Square>>,
        val legalMoves: List<Move>,
    )
}
