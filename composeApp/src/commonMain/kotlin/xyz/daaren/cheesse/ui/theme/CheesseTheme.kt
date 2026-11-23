package xyz.daaren.cheesse.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun CheesseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalBoardColors provides if (darkTheme) DarkBoardColors else LightBoardColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkCheeseColors else LightCheeseColors,
            typography = cheesseTypography(),
            shapes = CheesseShapes,
            content = content,
        )
    }
}

object CheesseTheme {
    val boardColors: BoardColors
        @Composable get() = LocalBoardColors.current
}
