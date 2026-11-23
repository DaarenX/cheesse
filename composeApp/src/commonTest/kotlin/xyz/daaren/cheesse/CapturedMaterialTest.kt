package xyz.daaren.cheesse

import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.domain.chess.ChessPiece
import xyz.daaren.cheesse.domain.chess.ChessPieceType
import xyz.daaren.cheesse.ui.game.capturedBy
import xyz.daaren.cheesse.ui.game.materialLead
import kotlin.test.Test
import kotlin.test.assertEquals

class CapturedMaterialTest {
    private val captured =
        listOf(
            ChessPiece(ChessPieceType.QUEEN, PlayerColor.BLACK),
            ChessPiece(ChessPieceType.PAWN, PlayerColor.BLACK),
            ChessPiece(ChessPieceType.KNIGHT, PlayerColor.WHITE),
            ChessPiece(ChessPieceType.PAWN, PlayerColor.BLACK),
        )

    @Test
    fun `captured by a side lists opponent pieces ordered by value`() {
        val types = capturedBy(PlayerColor.WHITE, captured).map { it.type }
        assertEquals(listOf(ChessPieceType.PAWN, ChessPieceType.PAWN, ChessPieceType.QUEEN), types)
    }

    @Test
    fun `captured by the other side contains only own color pieces`() {
        assertEquals(listOf(ChessPiece(ChessPieceType.KNIGHT, PlayerColor.WHITE)), capturedBy(PlayerColor.BLACK, captured))
    }

    @Test
    fun `material lead is the value difference for the leading side`() {
        assertEquals(8, materialLead(PlayerColor.WHITE, captured))
    }

    @Test
    fun `material lead is zero for the trailing side`() {
        assertEquals(0, materialLead(PlayerColor.BLACK, captured))
    }

    @Test
    fun `material lead is zero without captures`() {
        assertEquals(0, materialLead(PlayerColor.WHITE, emptyList()))
    }
}
