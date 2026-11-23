package xyz.daaren.cheesse.ui.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cheesse.composeapp.generated.resources.Res
import cheesse.composeapp.generated.resources.github
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import xyz.daaren.cheesse.ui.components.CheeseHolesBackground
import xyz.daaren.cheesse.ui.game.GameScreen
import xyz.daaren.cheesse.ui.game.board.PreloadIcons
import xyz.daaren.cheesse.ui.settings.SettingsScreen

internal enum class HomeDialog { None, Create, Join }

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        PreloadIcons()
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel<HomeScreenModelApi>()
        val uiState by screenModel.uiState.collectAsState()
        var dialog by remember { mutableStateOf(HomeDialog.None) }
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(screenModel) {
            screenModel.gameStartEvents.collectLatest { session ->
                dialog = HomeDialog.None
                navigator.push(GameScreen(session))
            }
        }
        ErrorSnackbar(uiState, dialog, snackbarHostState, screenModel::dismissError)
        HomeDialogs(dialog, uiState, screenModel) { dialog = it }

        val actions =
            HomeActions(
                onPlayLocal = screenModel::createLocalGame,
                onCreateOnline = { dialog = HomeDialog.Create },
                onJoin = { dialog = HomeDialog.Join },
                onSettings = { navigator.push(SettingsScreen()) },
                onCancelLobby = screenModel::cancelWaiting,
            )
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent,
        ) { padding ->
            HomeLayout(uiState, actions, padding)
        }
    }
}

@Composable
private fun HomeLayout(
    uiState: HomeScreenModelApi.HomeUiState,
    actions: HomeActions,
    padding: PaddingValues,
) {
    Box(Modifier.fillMaxSize()) {
        CheeseHolesBackground()
        HomeContent(uiState, actions, Modifier.padding(padding))
        GitHubLink(Modifier.align(Alignment.BottomEnd).padding(padding).padding(16.dp))
    }
}

/** Errors outside the join dialog appear as a snackbar; the join dialog shows its own inline. */
@Composable
private fun ErrorSnackbar(
    uiState: HomeScreenModelApi.HomeUiState,
    dialog: HomeDialog,
    hostState: SnackbarHostState,
    onDismiss: () -> Unit,
) {
    val message = uiState.errorMessage.takeIf { dialog != HomeDialog.Join && uiState.waitingLobby == null }
    LaunchedEffect(message) {
        if (message != null) {
            hostState.showSnackbar(message, actionLabel = "Dismiss", withDismissAction = false)
            onDismiss()
        }
    }
}

@Composable
private fun GitHubLink(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    OutlinedIconButton(
        onClick = { uriHandler.openUri("https://github.com/DaarenX/cheesse") },
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(Res.drawable.github),
            contentDescription = "GitHub repository",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
