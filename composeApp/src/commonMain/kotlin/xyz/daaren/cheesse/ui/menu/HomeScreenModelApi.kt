package xyz.daaren.cheesse.ui.menu

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import xyz.daaren.cheesse.api.GameColorPreference
import xyz.daaren.cheesse.data.GameSession

interface HomeScreenModelApi : ScreenModel {
    val uiState: StateFlow<HomeUiState>
    val gameStartEvents: Flow<GameSession>

    fun createGame(color: GameColorPreference)

    fun joinGame(token: String)

    fun dismissError()

    fun cancelWaiting()

    data class HomeUiState(
        val isLoading: Boolean = false,
        val waitingLobby: WaitingLobby? = null,
        val errorMessage: String? = null,
    )

    data class WaitingLobby(
        val gameId: Long,
        val playerToken: String,
        val joinToken: String,
        val isOpponentConnected: Boolean = false,
    )
}
