package xyz.daaren.cheesse.di

import org.kodein.di.DI
import org.kodein.di.bindFactory
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import xyz.daaren.cheesse.data.ChessServerService
import xyz.daaren.cheesse.data.ChessServerServiceImpl
import xyz.daaren.cheesse.data.GameSession
import xyz.daaren.cheesse.ui.game.GameScreenModel
import xyz.daaren.cheesse.ui.game.GameScreenModelApi
import xyz.daaren.cheesse.ui.menu.HomeScreenModel
import xyz.daaren.cheesse.ui.menu.HomeScreenModelApi
import xyz.daaren.cheesse.ui.settings.SettingsViewModel
import xyz.daaren.cheesse.ui.settings.SettingsViewModelApi

val dependencyInjection =
    DI {
        bindSingleton<ChessServerService> { ChessServerServiceImpl() }
        bindFactory<GameSession, GameScreenModelApi> { session -> GameScreenModel(session) }
        bindProvider<HomeScreenModelApi> { HomeScreenModel(chessServerService = instance()) }
        bindProvider<SettingsViewModelApi> { SettingsViewModel(chessServerService = instance()) }
    }
