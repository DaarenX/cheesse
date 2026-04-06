package xyz.daaren.cheesse.di

import org.kodein.di.DI
import org.kodein.di.bindFactory
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import xyz.daaren.cheesse.data.GameRepository
import xyz.daaren.cheesse.data.GameRepositoryImpl
import xyz.daaren.cheesse.data.GameSession
import xyz.daaren.cheesse.ui.game.GameScreenModel
import xyz.daaren.cheesse.ui.game.GameScreenModelApi
import xyz.daaren.cheesse.ui.menu.HomeScreenModel
import xyz.daaren.cheesse.ui.menu.HomeScreenModelApi

val dependencyInjection =
    DI {
        bindSingleton<GameRepository> { GameRepositoryImpl() }
        bindFactory<GameSession, GameScreenModelApi> { session -> GameScreenModel(session) }
        bindProvider<HomeScreenModelApi> { HomeScreenModel(gameRepository = instance()) }
    }
