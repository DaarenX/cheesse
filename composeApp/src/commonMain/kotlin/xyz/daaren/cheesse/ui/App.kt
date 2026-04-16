package xyz.daaren.cheesse.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.CrossfadeTransition
import org.kodein.di.compose.withDI
import xyz.daaren.cheesse.di.dependencyInjection
import xyz.daaren.cheesse.ui.menu.HomeScreen

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
