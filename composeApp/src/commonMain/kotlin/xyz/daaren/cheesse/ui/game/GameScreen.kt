package xyz.daaren.cheesse.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cheesse.composeapp.generated.resources.Res
import cheesse.composeapp.generated.resources.screen_rotation_alt
import org.jetbrains.compose.resources.painterResource
import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.data.GameSession
import xyz.daaren.cheesse.data.OfflineGameSession
import xyz.daaren.cheesse.domain.chess.ChessGameResult
import xyz.daaren.cheesse.ui.components.CheeseHolesBackground
import xyz.daaren.cheesse.ui.components.CheesseTopBar

class GameScreen(
    val gameSession: GameSession,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel<GameSession, GameScreenModelApi>(arg = gameSession)
        val gameState by viewModel.gameState.collectAsState()
        var boardRotation by remember { mutableStateOf(gameSession.playerColor) }
        val isOffline = gameSession is OfflineGameSession

        GameOverPrompt(gameState.gameResult, onBackToMenu = { navigator.pop() })
        Box {
            CheeseHolesBackground()
            Scaffold(
                topBar = {
                    CheesseTopBar(if (isOffline) "Local game" else "Online game", onBack = { navigator.pop() }) {
                        RotateBoardButton(boardRotation) { boardRotation = boardRotation.opposite() }
                    }
                },
                containerColor = Color.Transparent,
            ) { padding ->
                val youColor = gameSession.playerColor.takeUnless { isOffline }
                GameBody(GameBodyState(gameState, boardRotation, youColor), viewModel::makeMove, Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun GameOverPrompt(
    gameResult: ChessGameResult,
    onBackToMenu: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(gameResult) { visible = gameResult.isOver }
    if (visible) {
        GameOverDialog(gameResult, onBackToMenu, onDismiss = { visible = false })
    }
}

@Composable
private fun RotateBoardButton(
    rotation: PlayerColor,
    onClick: () -> Unit,
) {
    val angle by animateFloatAsState(if (rotation == PlayerColor.WHITE) 0f else 180f)
    IconButton(onClick = onClick) {
        Icon(painterResource(Res.drawable.screen_rotation_alt), contentDescription = "Rotate board", Modifier.rotate(angle))
    }
}

private fun PlayerColor.opposite(): PlayerColor = if (this == PlayerColor.WHITE) PlayerColor.BLACK else PlayerColor.WHITE
