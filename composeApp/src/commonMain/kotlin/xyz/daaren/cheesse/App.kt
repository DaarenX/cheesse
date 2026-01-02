package xyz.daaren.cheesse

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.CrossfadeTransition
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.compose.withDI
import xyz.daaren.cheesse.game.GameScreenModel
import xyz.daaren.cheesse.game.GameScreenModelApi

@Composable
@Preview
fun App() {
    withDI(dependencyInjection) {
        MaterialTheme {
            Navigator(HomeScreen()) {
                CrossfadeTransition(it)
            }
        }
    }
}

val dependencyInjection =
    DI {
        bindProvider<GameScreenModelApi> { GameScreenModel() }
    }
