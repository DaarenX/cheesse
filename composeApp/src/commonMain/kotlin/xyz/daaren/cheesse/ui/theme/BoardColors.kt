package xyz.daaren.cheesse.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Colors of the cheese-wedge board, kept apart from the Material scheme. */
@Immutable
data class BoardColors(
    val lightSquare: Color,
    val darkSquare: Color,
    val frame: Color = CheeseColors.Rind,
    val lastMove: Color = CheeseColors.Butter.copy(alpha = 0.45f),
    val selected: Color = CheeseColors.HoleOrange,
    val check: Color = Color(0xFFE5533D),
    val moveHint: Color = Color(0xFF5A3A12).copy(alpha = 0.35f),
    val whitePiece: Color = CheeseColors.Ivory,
    val blackPiece: Color = CheeseColors.Cocoa,
    val pieceOutline: Color = Color(0xFF4A2F12),
)

internal val LightBoardColors = BoardColors(lightSquare = Color(0xFFFFF3D1), darkSquare = Color(0xFFE0A84A))

internal val DarkBoardColors = BoardColors(lightSquare = Color(0xFFEAD9AE), darkSquare = Color(0xFFB98632))

val LocalBoardColors = staticCompositionLocalOf { LightBoardColors }
