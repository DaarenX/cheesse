package xyz.daaren.cheesse.ui.menu

import cafe.adriel.voyager.core.model.screenModelScope
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xyz.daaren.cheesse.api.GameColorPreference
import xyz.daaren.cheesse.api.ServerMessage
import xyz.daaren.cheesse.data.ChessServerService
import xyz.daaren.cheesse.data.GameSession

class HomeScreenModel(
    private val chessServerService: ChessServerService,
) : HomeScreenModelApi {
    private val internalUiState = MutableStateFlow(HomeScreenModelApi.HomeUiState())
    override val uiState = internalUiState.asStateFlow()

    private val internalGameStartEvents = MutableSharedFlow<GameSession>(extraBufferCapacity = 1)
    override val gameStartEvents = internalGameStartEvents.asSharedFlow()

    private var waitForOpponentJob: Job? = null

    override fun createGame(color: GameColorPreference) {
        screenModelScope.launch {
            internalUiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val createResponse = chessServerService.createGame(color)
                val connection = chessServerService.connectToGameSession(createResponse.gameId, createResponse.playerToken)
                internalUiState.update {
                    it.copy(
                        isLoading = false,
                        waitingLobby =
                            HomeScreenModelApi.WaitingLobby(
                                gameId = createResponse.gameId,
                                playerToken = createResponse.playerToken,
                                joinToken = createResponse.joinToken,
                            ),
                        errorMessage = null,
                    )
                }
                GameSession(
                    id = createResponse.gameId,
                    playerToken = createResponse.playerToken,
                    playerColor = createResponse.color,
                    gameSessionConnection = connection,
                )
            }.onSuccess { session ->
                startWaitingForOpponent(session)
            }.onFailure { exception ->
                internalUiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.asReadableMessage("Could not create game"),
                    )
                }
            }
        }
    }

    override fun joinGame(token: String) {
        val sanitizedToken = token.trim()
        if (sanitizedToken.isBlank()) {
            internalUiState.update {
                it.copy(errorMessage = "Please enter a join token")
            }
            return
        }

        screenModelScope.launch {
            internalUiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val joinResponse = chessServerService.joinGame(sanitizedToken)
                val connection = chessServerService.connectToGameSession(joinResponse.gameId, joinResponse.playerToken)
                GameSession(
                    id = joinResponse.gameId,
                    playerToken = joinResponse.playerToken,
                    playerColor = joinResponse.color,
                    gameSessionConnection = connection,
                )
            }.onSuccess { session ->
                startWaitingForOpponent(session)
            }.onFailure { exception ->
                internalUiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.asReadableMessage("Could not join game"),
                    )
                }
            }
        }
    }

    override fun dismissError() {
        internalUiState.update { it.copy(errorMessage = null) }
    }

    override fun cancelWaiting() {
        waitForOpponentJob?.cancel()
        internalUiState.update { it.copy(waitingLobby = null, isLoading = false) }
    }

    override fun onDispose() {
        waitForOpponentJob?.cancel()
        super.onDispose()
    }

    private fun startWaitingForOpponent(session: GameSession) {
        waitForOpponentJob =
            screenModelScope.launch {
                println("1")
                session.gameSessionConnection.updates
                    .filterIsInstance<ServerMessage.GameStart>()
                    .first()
                println("2")
                internalUiState.update { it.copy(isLoading = false, errorMessage = null, waitingLobby = null) }
                internalGameStartEvents.emit(session)
                println("3")
            }
    }
}

private fun Throwable.asReadableMessage(defaultMessage: String): String =
    when (this) {
        is ResponseException -> "$defaultMessage (${response.status.value})"
        else -> message ?: defaultMessage
    }
