package xyz.daaren.cheesse.ui.game.board

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.domain.chess.ChessPiece
import xyz.daaren.cheesse.ui.game.getIcon
import xyz.daaren.cheesse.ui.theme.CheesseTheme

private const val DIAGONAL = 0.7071f

/** A chess piece drawn as a filled glyph with a thin outline and a soft drop shadow. */
@Composable
fun PieceGlyph(
    piece: ChessPiece,
    modifier: Modifier = Modifier,
) {
    val painter = painterResource(piece.getIcon())
    val colors = CheesseTheme.boardColors
    val isWhite = piece.color == PlayerColor.WHITE
    val fill = if (isWhite) colors.whitePiece else colors.blackPiece
    val outline = if (isWhite) colors.pieceOutline else colors.whitePiece.copy(alpha = 0.45f)
    val description = "${piece.color.name.lowercase()} ${piece.type.name.lowercase()}"
    Canvas(modifier.semantics { contentDescription = description }) {
        drawGlyph(painter, Color.Black.copy(alpha = 0.28f), Offset(0f, 1.5.dp.toPx()))
        outlineOffsets(1.dp.toPx()).forEach { drawGlyph(painter, outline, it) }
        drawGlyph(painter, fill, Offset.Zero)
    }
}

private fun DrawScope.drawGlyph(
    painter: Painter,
    color: Color,
    offset: Offset,
) {
    translate(offset.x, offset.y) {
        with(painter) { draw(size, colorFilter = ColorFilter.tint(color)) }
    }
}

private fun outlineOffsets(width: Float): List<Offset> {
    val d = width * DIAGONAL
    return listOf(
        Offset(width, 0f),
        Offset(-width, 0f),
        Offset(0f, width),
        Offset(0f, -width),
        Offset(d, d),
        Offset(-d, d),
        Offset(d, -d),
        Offset(-d, -d),
    )
}
