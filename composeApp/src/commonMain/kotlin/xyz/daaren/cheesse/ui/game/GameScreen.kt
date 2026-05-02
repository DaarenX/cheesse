package xyz.daaren.cheesse.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
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
import io.github.alluhemanth.chess.core.game.GameResult
import io.github.alluhemanth.chess.core.move.Move
import io.github.alluhemanth.chess.core.piece.Piece
import io.github.alluhemanth.chess.core.piece.PieceColor
import io.github.alluhemanth.chess.core.piece.PieceType
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.data.GameSession
import kotlin.math.roundToInt

class GameScreen(
    val gameSession: GameSession,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel<GameSession, GameScreenModelApi>(arg = gameSession)
        val gameState by viewModel.gameState.collectAsState()
        var boardRotation by remember { mutableStateOf(gameSession.playerColor) }

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
                    actions = {
                        Text(
                            text = "Next: ${gameState.turn}",
                            modifier = Modifier.padding(end = 16.dp),
                        )
                        IconButton(
                            onClick = { boardRotation = if (boardRotation == PlayerColor.WHITE) PlayerColor.BLACK else PlayerColor.WHITE },
                        ) {
                            // TODO rotation icon
                            Image(
                                painter = painterResource(Res.drawable.compose_multiplatform),
                                contentDescription = "Rotate board icon",
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
                Chessboard(
                    gameState = gameState,
                    makeMove = viewModel::makeMove,
                    boardRotation = boardRotation,
                )
            }
        }
    }

    @Composable
    private fun Chessboard(
        gameState: GameScreenModelApi.GameState,
        makeMove: (String) -> Unit,
        boardRotation: PlayerColor,
    ) {
        var pendingPromotionMove by remember { mutableStateOf<String?>(null) }

        pendingPromotionMove?.let { it ->
            PromotionDialog(
                baseMoveUci = it,
                onMoveSelected = {
                    makeMove(it)
                    pendingPromotionMove = null
                },
                onDismiss = { pendingPromotionMove = null },
            )
        }

        var showGameOverDialog by remember { mutableStateOf(false) }
        LaunchedEffect(gameState.gameResult) {
            if (gameState.gameResult.isOver) {
                showGameOverDialog = true
            }
        }
        if (showGameOverDialog) {
            GameOverDialog(
                gameResult = gameState.gameResult,
                onDismiss = { showGameOverDialog = false },
            )
        }

        Box(
            modifier =
                Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth(),
        ) {
            BoxWithConstraints(Modifier.fillMaxSize(0.8f).align(Alignment.Center)) {
                ChessBoardBackground(
                    boardRotation,
                    Modifier.fillMaxSize(),
                )
                val squareSize = minWidth / 8

                // Needed to show all target squares for selected piece
                var selectedPiece by remember { mutableStateOf<Square?>(null) }

                gameState.piecePositions.forEach { (piece, square) ->
                    val legalMoves = gameState.legalMoves.filter { it.from == square }

                    DraggablePiece(
                        modifier = Modifier.align(AbsoluteAlignment.TopLeft).size(squareSize),
                        piece = piece,
                        square = square,
                        squareSize = squareSize,
                        legalMoves = legalMoves,
                        playerColor = boardRotation,
                        startMovePreview = { selectedPiece = it },
                        endMovePreview = { selectedPiece = null },
                        onMove = makeMove,
                        onPromotionRequested = { pendingPromotionMove = it },
                    )
                }

                if (selectedPiece != null) {
                    gameState.legalMoves.filter { it.from == selectedPiece }.map { it.to }.forEach { square ->
                        Image(
                            modifier =
                                Modifier
                                    .align(AbsoluteAlignment.TopLeft)
                                    .size(squareSize)
                                    .offset {
                                        val pxSize = squareSize.toPx()
                                        IntOffset(
                                            (pxSize * square.displayCol(boardRotation)).roundToInt(),
                                            (pxSize * square.displayRow(boardRotation)).roundToInt(),
                                        )
                                    },
                            painter = painterResource(Res.drawable.compose_multiplatform),
                            contentDescription = "test",
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ChessBoardBackground(
        boardRotation: PlayerColor,
        modifier: Modifier = Modifier,
    ) {
        val darkSquareColor = Color(0xFF769656)
        val lightSquareColor = Color(0xFFEEEED2)
        val textMeasurer = rememberTextMeasurer()

        Canvas(modifier) {
            val squareSize = size.width / 8
            repeat(8) { row ->
                repeat(8) { col ->
                    val isDark = (row + col) % 2 != 0
                    drawRect(
                        color = if (isDark) darkSquareColor else lightSquareColor,
                        topLeft = Offset(col * squareSize, row * squareSize),
                        size = Size(squareSize, squareSize),
                    )
                    val letterPadding = 3.dp.toPx()
                    if (row == 7) {
                        val letter = if (boardRotation == PlayerColor.WHITE) 'a' + col else 'h' - col
                        val textLayoutResult =
                            textMeasurer.measure(
                                text = letter.toString(),
                                style = TextStyle(color = if (isDark) lightSquareColor else darkSquareColor),
                            )
                        val position =
                            Offset(
                                x = col * squareSize + letterPadding,
                                y = size.height - textLayoutResult.size.height - letterPadding,
                            )
                        drawText(textLayoutResult, topLeft = position)
                    }
                    if (col == 7) {
                        val letter = if (boardRotation == PlayerColor.WHITE) '8' - row else '1' + row
                        val textLayoutResult =
                            textMeasurer.measure(
                                text = letter.toString(),
                                style = TextStyle(color = if (isDark) lightSquareColor else darkSquareColor),
                            )
                        val position =
                            Offset(
                                x = size.width - textLayoutResult.size.width - letterPadding,
                                y = row * squareSize + letterPadding,
                            )
                        drawText(textLayoutResult, topLeft = position)
                    }
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
        playerColor: PlayerColor,
        startMovePreview: (Square) -> Unit,
        endMovePreview: () -> Unit,
        onMove: (String) -> Unit,
        onPromotionRequested: (String) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        var offset by remember { mutableStateOf(Offset.Zero) }

        Box(
            modifier =
                modifier
                    .offset {
                        val pxSize = squareSize.toPx()
                        IntOffset(
                            (pxSize * square.displayCol(playerColor) + offset.x).roundToInt(),
                            (pxSize * square.displayRow(playerColor) + offset.y).roundToInt(),
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

                                val targetSquare =
                                    getSquareFromDisplay(
                                        square.displayCol(playerColor) + colMove,
                                        square.displayRow(playerColor) + rowMove,
                                        playerColor,
                                    )

                                // TODO find a better way of detecting promotion, so I don't need to check all moves
                                val possibleMoves = legalMoves.filter { it.to == targetSquare }

                                if (possibleMoves.size > 1) {
                                    onPromotionRequested(square.toString() + targetSquare.toString())
                                } else if (possibleMoves.isNotEmpty()) {
                                    onMove(possibleMoves.first().toUci())
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

private fun PieceType.getIcon(): DrawableResource =
    when (this) {
        PieceType.PAWN -> Res.drawable.chess_pawn
        PieceType.KNIGHT -> Res.drawable.chess_knight
        PieceType.BISHOP -> Res.drawable.chess_bishop_2
        PieceType.ROOK -> Res.drawable.chess_rook
        PieceType.QUEEN -> Res.drawable.chess_queen
        PieceType.KING -> Res.drawable.chess_king_2
    }

private fun Piece.getIcon(): DrawableResource = pieceType.getIcon()

private fun PieceType.toPromotionString(): String =
    when (this) {
        PieceType.PAWN -> ""
        PieceType.KNIGHT -> "k"
        PieceType.BISHOP -> "b"
        PieceType.ROOK -> "r"
        PieceType.QUEEN -> "q"
        PieceType.KING -> ""
    }

private fun Square.displayCol(boardRotation: PlayerColor): Int = if (boardRotation == PlayerColor.WHITE) colIndex() else 7 - colIndex()

private fun Square.displayRow(boardRotation: PlayerColor): Int = if (boardRotation == PlayerColor.WHITE) 7 - rowIndex() else rowIndex()

private fun getSquareFromDisplay(
    displayCol: Int,
    displayRow: Int,
    playerColor: PlayerColor,
): Square {
    val col = if (playerColor == PlayerColor.WHITE) displayCol else 7 - displayCol
    val row = if (playerColor == PlayerColor.WHITE) 7 - displayRow else displayRow
    val file = 'a' + col.coerceIn(0, 7)
    val rank = row.coerceIn(0, 7) + 1
    return Square("$file$rank")
}

private fun Square.colIndex(): Int = file.value - 'a'

private fun Square.rowIndex(): Int = rank.value - 1

@Composable
private fun PromotionDialog(
    baseMoveUci: String,
    onMoveSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Promotion Piece") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)
                    .forEach { pieceType ->
                        IconButton(onClick = { onMoveSelected(baseMoveUci + pieceType.toPromotionString()) }) {
                            Image(
                                painter = painterResource(pieceType.getIcon()),
                                contentDescription = pieceType.toString(),
                                modifier = Modifier.size(48.dp),
                                colorFilter = ColorFilter.tint(Color.Black),
                            )
                        }
                    }
            }
        },
        confirmButton = {},
    )
}

@Composable
private fun GameOverDialog(
    gameResult: GameResult,
    onDismiss: () -> Unit,
) {
    val title =
        when (gameResult) {
            is GameResult.Win -> {
                val winner =
                    gameResult.winner.name
                        .lowercase()
                        .replaceFirstChar { it.uppercase() }
                "$winner wins!"
            }
            is GameResult.Draw -> "Draw"
            else -> "Game Over"
        }

    val message =
        when (gameResult) {
            is GameResult.Win -> "Checkmate"
            GameResult.Draw.Stalemate -> "Stalemate"
            GameResult.Draw.ThreefoldRepetition -> "Threefold Repetition"
            GameResult.Draw.FiftyMoveRule -> "Fifty-move Rule"
            GameResult.Draw.InsufficientMaterial -> "Insufficient Material"
            else -> ""
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { },
    )
}
