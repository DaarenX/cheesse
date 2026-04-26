package xyz.daaren.cheesse.ui.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.flow.collectLatest
import xyz.daaren.cheesse.api.GameColorPreference
import xyz.daaren.cheesse.ui.game.GameScreen
import xyz.daaren.cheesse.ui.settings.SettingsScreen

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel<HomeScreenModelApi>()
        val uiState by screenModel.uiState.collectAsState()
        var showJoinGameDialog by remember { mutableStateOf(false) }
        var showCreateGameDialog by remember { mutableStateOf(false) }

        LaunchedEffect(screenModel) {
            screenModel.gameStartEvents.collectLatest { session ->
                showJoinGameDialog = false
                showCreateGameDialog = false
                navigator.push(GameScreen(session))
            }
        }

        AnimatedVisibility(showJoinGameDialog) {
            JoinGameDialog(
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                onDismissRequest = { showJoinGameDialog = false },
                onJoinGameButton = { screenModel.joinGame(it) },
                onDismissError = screenModel::dismissError,
            )
        }
        AnimatedVisibility(showCreateGameDialog) {
            CreateGameDialog(
                isLoading = uiState.isLoading,
                onDismissRequest = { showCreateGameDialog = false },
                onCreateGameButton = {
                    showCreateGameDialog = false
                    screenModel.createGame(it)
                },
            )
        }
        Box(Modifier.fillMaxSize()) {
            val waitingLobby = uiState.waitingLobby
            if (waitingLobby == null) {
                Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = { screenModel.createLocalGame() },
                        enabled = !uiState.isLoading,
                    ) {
                        Text("New local Game")
                    }
                    Button(
                        onClick = { showCreateGameDialog = true },
                        enabled = !uiState.isLoading,
                    ) {
                        Text("New Game")
                    }
                    Button(
                        onClick = { showJoinGameDialog = true },
                        enabled = !uiState.isLoading,
                    ) {
                        Text("Join Game")
                    }
                    Button(
                        onClick = { navigator.push(SettingsScreen()) },
                        enabled = !uiState.isLoading,
                    ) {
                        Text("Settings")
                    }
                }
            } else {
                WaitingForOpponent(
                    modifier = Modifier.align(Alignment.Center),
                    waitingLobby = waitingLobby,
                    onCancel = screenModel::cancelWaiting,
                )
            }

            AnimatedVisibility(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
                visible = uiState.errorMessage != null && uiState.waitingLobby == null,
            ) {
                Card {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = uiState.errorMessage.orEmpty(),
                            color = Color.Red,
                        )
                        Button(onClick = screenModel::dismissError) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun CreateGameDialog(
        isLoading: Boolean,
        onDismissRequest: () -> Unit,
        onCreateGameButton: (GameColorPreference) -> Unit,
    ) {
        BasicAlertDialog(onDismissRequest = onDismissRequest) {
            Card {
                Column(
                    modifier = Modifier.padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                ) {
                    var selectedColorPreference by remember { mutableStateOf(GameColorPreference.RANDOM) }
                    Text("Color")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Button(
                            onClick = { selectedColorPreference = GameColorPreference.WHITE },
                            border = if (selectedColorPreference == GameColorPreference.WHITE) BorderStroke(2.dp, Color.Black) else null,
                        ) {
                            Text("WHITE")
                        }
                        Button(
                            onClick = { selectedColorPreference = GameColorPreference.RANDOM },
                            border = if (selectedColorPreference == GameColorPreference.RANDOM) BorderStroke(2.dp, Color.Black) else null,
                        ) {
                            Text("RANDOM")
                        }

                        Button(
                            onClick = { selectedColorPreference = GameColorPreference.BLACK },
                            border = if (selectedColorPreference == GameColorPreference.BLACK) BorderStroke(2.dp, Color.Black) else null,
                        ) {
                            Text("BLACK")
                        }
                    }
                    Button(
                        onClick = { onCreateGameButton(selectedColorPreference) },
                        enabled = !isLoading,
                    ) {
                        Text(if (isLoading) "CREATING..." else "CREATE GAME")
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun JoinGameDialog(
        isLoading: Boolean,
        errorMessage: String?,
        onDismissRequest: () -> Unit,
        onJoinGameButton: (String) -> Unit,
        onDismissError: () -> Unit,
    ) {
        BasicAlertDialog(onDismissRequest = onDismissRequest) {
            Card {
                Column(
                    modifier = Modifier.padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                ) {
                    var input by remember { mutableStateOf("") }
                    TextField(
                        input,
                        onValueChange = { input = it },
                        label = { Text("Token") },
                    )
                    if (errorMessage != null) {
                        Text(errorMessage, color = Color.Red)
                        Button(onClick = onDismissError) {
                            Text("Dismiss")
                        }
                    }
                    Button(
                        onClick = { onJoinGameButton(input) },
                        enabled = !isLoading,
                    ) {
                        Text(if (isLoading) "JOINING..." else "JOIN")
                    }
                }
            }
        }
    }

    @Composable
    private fun WaitingForOpponent(
        modifier: Modifier = Modifier,
        waitingLobby: HomeScreenModelApi.WaitingLobby,
        onCancel: () -> Unit,
    ) {
        Card(modifier = modifier) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Lobby created")
                Text("Share this join token:")
                Text(waitingLobby.joinToken)
                if (waitingLobby.isOpponentConnected) {
                    Text("Opponent joined. Starting game...")
                } else {
                    Text("Waiting for opponent...")
                }
                Button(onClick = onCancel) {
                    Text("Cancel")
                }
            }
        }
    }
}
