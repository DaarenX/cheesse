package xyz.daaren.cheesse.ui.game.board

import androidx.compose.ui.geometry.Offset
import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.domain.chess.ChessMove
import xyz.daaren.cheesse.domain.chess.ChessPieceType
import xyz.daaren.cheesse.domain.chess.ChessSquare
import kotlin.math.abs

fun ChessSquare.displayCol(boardRotation: PlayerColor): Int = if (boardRotation == PlayerColor.WHITE) colIndex else 7 - colIndex

fun ChessSquare.displayRow(boardRotation: PlayerColor): Int = if (boardRotation == PlayerColor.WHITE) 7 - rowIndex else rowIndex

/** Top-left pixel position of [square] on a board drawn from [boardRotation]'s side. */
fun squareOffset(
    square: ChessSquare,
    boardRotation: PlayerColor,
    squarePx: Float,
): Offset = Offset(square.displayCol(boardRotation) * squarePx, square.displayRow(boardRotation) * squarePx)

/**
 * Maps every square a piece landed on during [lastMove] to the square it came from,
 * including the rook's jump when the king castled.
 */
fun moveOrigins(
    lastMove: ChessMove,
    movedType: ChessPieceType?,
): Map<ChessSquare, ChessSquare> {
    val origins = mutableMapOf(lastMove.to to lastMove.from)
    val isCastling = movedType == ChessPieceType.KING && abs(lastMove.to.colIndex - lastMove.from.colIndex) == 2
    if (isCastling) {
        val kingside = lastMove.to.file == 'g'
        val rank = lastMove.to.rank
        val rookFrom = ChessSquare(if (kingside) 'h' else 'a', rank)
        val rookTo = ChessSquare(if (kingside) 'f' else 'd', rank)
        origins[rookTo] = rookFrom
    }
    return origins
}
