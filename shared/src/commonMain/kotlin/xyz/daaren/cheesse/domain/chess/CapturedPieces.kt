package xyz.daaren.cheesse.domain.chess

import xyz.daaren.cheesse.api.PlayerColor

private val standardPieceSet =
    List(8) { ChessPieceType.PAWN } +
        listOf(
            ChessPieceType.KNIGHT,
            ChessPieceType.KNIGHT,
            ChessPieceType.BISHOP,
            ChessPieceType.BISHOP,
            ChessPieceType.ROOK,
            ChessPieceType.ROOK,
            ChessPieceType.QUEEN,
        )

internal fun capturedPieces(piecesOnBoard: List<ChessPiece>): List<ChessPiece> =
    PlayerColor.entries.flatMap { color ->
        capturedForColor(piecesOnBoard.filter { it.color == color }, color)
    }

private fun capturedForColor(
    onBoard: List<ChessPiece>,
    color: PlayerColor,
): List<ChessPiece> {
    val remaining = onBoard.map { it.type }.toMutableList()
    return standardPieceSet
        .filterNot { type -> remaining.remove(type) }
        .map { type -> ChessPiece(type, color) }
}
