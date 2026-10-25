package xyz.daaren.cheesse.ui.game

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.StateFlow
import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.domain.chess.ChessGameResult
import xyz.daaren.cheesse.domain.chess.ChessMove
import xyz.daaren.cheesse.domain.chess.ChessPiece
import xyz.daaren.cheesse.domain.chess.ChessSquare

interface GameScreenModelApi : ScreenModel {
    val gameState: StateFlow<GameState>

    fun makeMove(moveUci: String)

    data class GameState(
        val piecePositions: List<Pair<ChessPiece, ChessSquare>>,
        val legalMoves: List<ChessMove>,
        val turn: PlayerColor,
        val capturedPieces: List<ChessPiece>,
        val gameResult: ChessGameResult = ChessGameResult.Ongoing,
    )
}
