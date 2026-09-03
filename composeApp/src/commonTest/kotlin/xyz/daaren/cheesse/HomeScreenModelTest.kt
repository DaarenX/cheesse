package xyz.daaren.cheesse

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import xyz.daaren.cheesse.api.CreateGameResponse
import xyz.daaren.cheesse.api.GameColorPreference
import xyz.daaren.cheesse.api.JoinGameResponse
import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.api.ServerMessage
import xyz.daaren.cheesse.data.ChessServerService
import xyz.daaren.cheesse.data.GameSessionConnection
import xyz.daaren.cheesse.ui.menu.HomeScreenModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenModelTest {
    private class FakeChessServerService(
        private val connection: GameSessionConnection,
    ) : ChessServerService {
        override val serverUrl: StateFlow<String> = MutableStateFlow("http://localhost")

        override fun updateBaseUrl(url: String) = Unit

        override suspend fun connectToGameSession(
            id: Long,
            playerToken: String,
        ) = connection

        override suspend fun createGame(color: GameColorPreference) =
            CreateGameResponse(gameId = 1, joinToken = "join", playerId = 1, playerToken = "player", color = PlayerColor.WHITE)

        override suspend fun joinGame(token: String) =
            JoinGameResponse(gameId = 1, playerId = 2, playerToken = "player", color = PlayerColor.BLACK)
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `connection failure while waiting for opponent shows error and leaves lobby`() =
        runTest {
            val failingConnection =
                GameSessionConnection(
                    updates = flow<ServerMessage> { throw IllegalStateException("Connection lost") },
                    sendMove = {},
                )
            val screenModel = HomeScreenModel(FakeChessServerService(failingConnection))

            screenModel.createGame(GameColorPreference.RANDOM)

            // screenModelScope runs on Voyager's platform dispatcher, which setMain does not replace on Wasm
            val state = screenModel.uiState.first { it.errorMessage != null }
            assertEquals("Connection lost", state.errorMessage)
            assertNull(state.waitingLobby)
            assertFalse(state.isLoading)
        }
}
