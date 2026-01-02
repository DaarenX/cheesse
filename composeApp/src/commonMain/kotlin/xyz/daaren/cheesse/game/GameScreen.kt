package xyz.daaren.cheesse.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cheesse.composeapp.generated.resources.Res
import cheesse.composeapp.generated.resources.arrow_back
import cheesse.composeapp.generated.resources.chess_bishop_2
import cheesse.composeapp.generated.resources.chess_king_2
import cheesse.composeapp.generated.resources.chess_knight
import cheesse.composeapp.generated.resources.chess_pawn
import cheesse.composeapp.generated.resources.chess_queen
import cheesse.composeapp.generated.resources.chess_rook
import cheesse.composeapp.generated.resources.compose_multiplatform
import io.github.alluhemanth.chess.core.board.Square
import io.github.alluhemanth.chess.core.move.Move
import io.github.alluhemanth.chess.core.piece.Piece
import io.github.alluhemanth.chess.core.piece.PieceColor
import io.github.alluhemanth.chess.core.piece.PieceType
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

class GameScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel<GameScreenModelApi>()
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Cheesse") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                painter = painterResource(Res.drawable.arrow_back),
                                contentDescription = "Back Arrow Icon",
                            )
                        }
                    },
                )
            },
        ) { paddingValues ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Chessboard(viewModel)
            }
        }
    }

    @Composable
    private fun Chessboard(viewModel: GameScreenModelApi) {
        BoxWithConstraints(
            modifier =
                Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth()
                    .padding(8.dp),
        ) {
            ChessBoardBackground(Modifier.fillMaxSize())
            val squareSize = minWidth / 8

            // Needed to show all target squares for selected piece
            var selectedPiece by remember { mutableStateOf<Square?>(null) }
            // TODO rename???

            val game by viewModel.gameState.collectAsState()

            game.piecePositions.forEach { (piece, square) ->
                val legalMoves = game.legalMoves.filter { it.from == square }

                DraggablePiece(
                    modifier = Modifier.align(AbsoluteAlignment.TopLeft).size(squareSize),
                    piece = piece,
                    square = square,
                    squareSize = squareSize,
                    legalMoves = legalMoves,
                    startMovePreview = { selectedPiece = it },
                    endMovePreview = { selectedPiece = null },
                    onMove = { viewModel.makeMove(it) },
                )
            }

            if (selectedPiece != null) {
                game.legalMoves.filter { it.from == selectedPiece }.map { it.to }.forEach { square ->
                    Image(
                        modifier =
                            Modifier
                                .align(AbsoluteAlignment.TopLeft)
                                .size(squareSize)
                                .offset {
                                    val pxSize = squareSize.toPx()
                                    IntOffset(
                                        (pxSize * square.colIndex()).roundToInt(),
                                        (pxSize * square.rowIndex()).roundToInt(),
                                    )
                                },
                        painter = painterResource(Res.drawable.compose_multiplatform),
                        contentDescription = "test",
                    )
                }
            }
        }
    }

    @Composable
    private fun ChessBoardBackground(modifier: Modifier = Modifier) {
        val darkSquareColor = Color(0xFF769656)
        val lightSquareColor = Color(0xFFEEEED2)
        Canvas(modifier) {
            val squareSize = size.width / 8f
            (0..7).forEach { row ->
                (0..7).forEach { col ->
                    val isDark = (row + col) % 2 != 0
                    drawRect(
                        color = if (isDark) darkSquareColor else lightSquareColor,
                        topLeft = Offset(col * squareSize, row * squareSize),
                        size = Size(squareSize, squareSize),
                    )
                }
            }
        }
    }

    @Composable
    private fun DraggablePiece(
        piece: Piece,
        square: Square,
        squareSize: Dp,
        legalMoves: List<Move>,
        startMovePreview: (Square) -> Unit,
        endMovePreview: () -> Unit,
        onMove: (Move) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        var offset by remember { mutableStateOf(Offset.Zero) }

        Box(
            modifier =
                modifier
                    .offset {
                        val pxSize = squareSize.toPx()
                        IntOffset(
                            (pxSize * square.colIndex() + offset.x).roundToInt(),
                            (pxSize * square.rowIndex() + offset.y).roundToInt(),
                        )
                    }.pointerInput(square, legalMoves) {
                        detectDragGestures(
                            onDragStart = {
                                startMovePreview(square)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offset += dragAmount
                            },
                            onDragEnd = {
                                val pxSize = squareSize.toPx()
                                val colMove = (offset.x / pxSize).roundToInt()
                                val rowMove = (offset.y / pxSize).roundToInt()

                                val rank = (square.rowIndex() + rowMove).coerceIn(0..7) + 1
                                val file = 'a' + (square.colIndex() + colMove).coerceIn(0..7)

                                val targetSquare = Square("$file$rank")
                                // TODO promotion selection
                                val move = legalMoves.find { it.to == targetSquare }

                                if (move != null) {
                                    onMove(move)
                                }

                                offset = Offset.Zero
                                endMovePreview()
                            },
                            onDragCancel = {
                                offset = Offset.Zero
                                endMovePreview()
                            },
                        )
                    },
        ) {
            Image(
                modifier = Modifier.align(Alignment.Center).fillMaxSize(0.8f),
                painter = painterResource(piece.getIcon()),
                contentDescription = piece.toString(),
                colorFilter = ColorFilter.tint(if (piece.color == PieceColor.WHITE) Color.White else Color.Black),
            )
        }
    }
}

private fun Piece.getIcon(): DrawableResource =
    when (pieceType) {
        PieceType.PAWN -> Res.drawable.chess_pawn
        PieceType.KNIGHT -> Res.drawable.chess_knight
        PieceType.BISHOP -> Res.drawable.chess_bishop_2
        PieceType.ROOK -> Res.drawable.chess_rook
        PieceType.QUEEN -> Res.drawable.chess_queen
        PieceType.KING -> Res.drawable.chess_king_2
    }

private fun Square.colIndex(): Int = file.value - 'a'

private fun Square.rowIndex(): Int = rank.value - 1
