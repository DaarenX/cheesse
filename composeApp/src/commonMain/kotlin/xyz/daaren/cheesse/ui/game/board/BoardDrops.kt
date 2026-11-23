package xyz.daaren.cheesse.ui.game.board

import androidx.compose.ui.geometry.Offset
import xyz.daaren.cheesse.domain.chess.ChessSquare
import xyz.daaren.cheesse.ui.game.GameScreenModelApi.GameState
import kotlin.math.roundToInt

/** A move the player just made by dragging, and how far from the target square they let go. */
internal data class LocalDrop(
    val uci: String,
    val residual: Offset,
)

internal data class DroppedPiece(
    val from: ChessSquare,
    val drag: Offset,
) {
    /** The square under the dropped piece, or null when it was dropped off the board. */
    fun targetSquare(layout: BoardLayout): ChessSquare? {
        val col = from.displayCol(layout.rotation) + (drag.x / layout.squarePx).roundToInt()
        val row = from.displayRow(layout.rotation) + (drag.y / layout.squarePx).roundToInt()
        if (col !in 0..7 || row !in 0..7) return null
        return ChessSquare.fromDisplay(col, row, layout.rotation)
    }

    fun residual(
        target: ChessSquare,
        layout: BoardLayout,
    ): Offset = drag - (squareOffset(target, layout.rotation, layout.squarePx) - squareOffset(from, layout.rotation, layout.squarePx))
}

/** Every piece with the offset it should glide in from, based on the last move. */
internal fun piecePlacements(
    gameState: GameState,
    layout: BoardLayout,
    localDrop: LocalDrop?,
): List<PiecePlacement> {
    val lastMove = gameState.lastMove ?: return gameState.piecePositions.map { (piece, square) -> PiecePlacement(piece, square) }
    val movedType =
        gameState.piecePositions
            .firstOrNull { it.second == lastMove.to }
            ?.first
            ?.type
    val origins = moveOrigins(lastMove, movedType)
    val droppedHere = localDrop?.takeIf { it.uci == lastMove.uci }
    return gameState.piecePositions.map { (piece, square) ->
        val origin = origins[square] ?: return@map PiecePlacement(piece, square)
        val drop = droppedHere?.takeIf { square == lastMove.to }
        val start =
            drop?.residual
                ?: (squareOffset(origin, layout.rotation, layout.squarePx) - squareOffset(square, layout.rotation, layout.squarePx))
        PiecePlacement(piece, square, start, fromLocalDrop = drop != null, animationKey = lastMove)
    }
}
