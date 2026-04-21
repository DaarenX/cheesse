package xyz.daaren.cheesse.ui.settings

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.StateFlow

interface SettingsViewModelApi : ScreenModel {
    val url: StateFlow<String>

    fun saveUrl(url: String)
}
