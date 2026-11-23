package xyz.daaren.cheesse.ui.game.board

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.domain.chess.ChessPiece
import xyz.daaren.cheesse.domain.chess.ChessSquare
import xyz.daaren.cheesse.ui.theme.Motion

/**
 * Where a piece stands and where it should visually glide in from.
 * [animationKey] changes with every move, so a new arrival on the same square animates again.
 */
internal data class PiecePlacement(
    val piece: ChessPiece,
    val square: ChessSquare,
    val startOffset: Offset = Offset.Zero,
    val fromLocalDrop: Boolean = false,
    val animationKey: Any? = null,
)

internal data class BoardLayout(
    val squareSize: Dp,
    val squarePx: Float,
    val rotation: PlayerColor,
)

/**
 * Drag callbacks. [onRelease] gets the drag distance, or null when the gesture was cancelled,
 * and returns true when the drop became a move.
 */
internal class PieceDragHandler(
    val canDrag: (ChessSquare) -> Boolean,
    val onPickUp: (ChessSquare) -> Unit,
    val onRelease: (ChessSquare, Offset?) -> Boolean,
)

@Composable
internal fun BoardPiece(
    placement: PiecePlacement,
    layout: BoardLayout,
    handler: PieceDragHandler,
) {
    // Keyed on the move rather than the offset, so resizing or rotating doesn't replay the glide.
    val slide = remember(placement.piece, placement.animationKey) { Animatable(placement.startOffset, Offset.VectorConverter) }
    LaunchedEffect(slide) {
        if (slide.value != Offset.Zero) slide.animateTo(Offset.Zero, if (placement.fromLocalDrop) Motion.snapBack else Motion.pieceMove)
    }
    val drag = remember { mutableStateOf(Offset.Zero) }
    var dragging by remember { mutableStateOf(false) }
    val lift by animateFloatAsState(if (dragging) 1.12f else 1f, Motion.press)
    val scope = rememberCoroutineScope()
    val currentHandler by rememberUpdatedState(handler)
    val square = placement.square

    Box(
        Modifier
            .size(layout.squareSize)
            .offset { (squareOffset(square, layout.rotation, layout.squarePx) + slide.value + drag.value).round() }
            .zIndex(if (dragging || slide.isRunning) 1f else 0f)
            .graphicsLayer {
                scaleX = lift
                scaleY = lift
            }.then(
                if (handler.canDrag(square)) {
                    Modifier.pointerInput(square) {
                        detectDragGestures(
                            onDragStart = {
                                dragging = true
                                currentHandler.onPickUp(square)
                            },
                            onDrag = { change, amount ->
                                change.consume()
                                drag.value += amount
                            },
                            onDragEnd = {
                                dragging = false
                                val accepted = currentHandler.onRelease(square, drag.value)
                                scope.settle(accepted, drag, slide)
                            },
                            onDragCancel = {
                                dragging = false
                                currentHandler.onRelease(square, null)
                                scope.settle(false, drag, slide)
                            },
                        )
                    }
                } else {
                    Modifier
                },
            ),
    ) {
        PieceGlyph(placement.piece, Modifier.fillMaxSize(0.84f).align(Alignment.Center))
    }
}

/** After a rejected drop the piece springs back to its square; an accepted one is replaced by the new position. */
private fun CoroutineScope.settle(
    accepted: Boolean,
    drag: MutableState<Offset>,
    slide: Animatable<Offset, *>,
) {
    val released = drag.value
    drag.value = Offset.Zero
    if (accepted) return
    launch {
        slide.snapTo(released)
        slide.animateTo(Offset.Zero, Motion.snapBack)
    }
}
