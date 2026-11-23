package xyz.daaren.cheesse.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.ScreenTransition
import org.kodein.di.compose.withDI
import xyz.daaren.cheesse.di.dependencyInjection
import xyz.daaren.cheesse.ui.menu.HomeScreen
import xyz.daaren.cheesse.ui.theme.CheesseTheme
import xyz.daaren.cheesse.ui.theme.Motion

@Composable
@Preview
fun App() {
    withDI(dependencyInjection) {
        CheesseTheme {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Navigator(HomeScreen()) { navigator ->
                    ScreenTransition(navigator, transition = { slideAndFade(navigator.lastEvent == StackEvent.Pop) })
                }
            }
        }
    }
}

/** Screens glide a tenth of the width while cross-fading; popping reverses the direction. */
private fun AnimatedContentTransitionScope<Screen>.slideAndFade(isPop: Boolean): ContentTransform {
    val direction = if (isPop) -1 else 1
    val slide = tween<IntOffset>(Motion.LONG_MS)
    val fade = tween<Float>(Motion.LONG_MS)
    return (slideInHorizontally(slide) { direction * it / 10 } + fadeIn(fade)) togetherWith
        (slideOutHorizontally(slide) { -direction * it / 10 } + fadeOut(fade))
}
