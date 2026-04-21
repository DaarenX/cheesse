package xyz.daaren.cheesse.ui.settings

import kotlinx.coroutines.flow.StateFlow
import xyz.daaren.cheesse.data.ChessServerService

class SettingsViewModel(
    private val chessServerService: ChessServerService,
) : SettingsViewModelApi {
    override val url: StateFlow<String> = chessServerService.serverUrl

    override fun saveUrl(url: String) {
        chessServerService.updateBaseUrl(url)
    }
}
