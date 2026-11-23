package xyz.daaren.cheesse.ui.game

import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.domain.chess.ChessPiece
import xyz.daaren.cheesse.domain.chess.ChessPieceType

fun ChessPieceType.materialValue(): Int =
    when (this) {
        ChessPieceType.PAWN -> 1
        ChessPieceType.KNIGHT, ChessPieceType.BISHOP -> 3
        ChessPieceType.ROOK -> 5
        ChessPieceType.QUEEN -> 9
        ChessPieceType.KING -> 0
    }

/** The opponent pieces [color] has taken, cheapest first. */
fun capturedBy(
    color: PlayerColor,
    captured: List<ChessPiece>,
): List<ChessPiece> = captured.filter { it.color != color }.sortedBy { it.type.materialValue() }

/** How many material points [color] is ahead by, or 0 when not ahead. */
fun materialLead(
    color: PlayerColor,
    captured: List<ChessPiece>,
): Int {
    val gained = capturedBy(color, captured).sumOf { it.type.materialValue() }
    val lost = captured.filter { it.color == color }.sumOf { it.type.materialValue() }
    return (gained - lost).coerceAtLeast(0)
}
