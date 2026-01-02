package xyz.daaren.cheesse

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.CrossfadeTransition
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import cheesse.composeapp.generated.resources.Res
import cheesse.composeapp.generated.resources.compose_multiplatform
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

val dependencyInjection = DI {
    bindProvider<GameScreenModelApi> { GameScreenModel() }
}