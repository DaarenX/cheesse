package xyz.daaren.cheesse

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cheesse.composeapp.generated.resources.Res
import cheesse.composeapp.generated.resources.cheesse_icon_green
import org.jetbrains.compose.resources.painterResource
import xyz.daaren.cheesse.ui.App

fun main() =
    application {
        Window(
            onCloseRequest = ::exitApplication,
            icon = painterResource(Res.drawable.cheesse_icon_green),
            title = "Cheesse",
        ) {
            App()
        }
    }
