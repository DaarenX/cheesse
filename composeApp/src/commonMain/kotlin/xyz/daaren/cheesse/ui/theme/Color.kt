package xyz.daaren.cheesse.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/** Brand colors taken from the cheese-crown logo. */
object CheeseColors {
    val Gold = Color(0xFFFCBF29)
    val Butter = Color(0xFFFBD307)
    val Rind = Color(0xFFB5762E)
    val HoleOrange = Color(0xFFF09235)
    val Ivory = Color(0xFFFFFDF5)
    val Cocoa = Color(0xFF2B1D12)
}

internal val LightCheeseColors =
    lightColorScheme(
        primary = Color(0xFFE8A317),
        onPrimary = Color(0xFF2B1D0E),
        primaryContainer = Color(0xFFFFE08A),
        onPrimaryContainer = Color(0xFF3A2800),
        secondary = CheeseColors.HoleOrange,
        onSecondary = Color(0xFF2B1608),
        secondaryContainer = Color(0xFFFFD9B5),
        onSecondaryContainer = Color(0xFF3B1E00),
        tertiary = Color(0xFF7C8F5A),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFDCE8C8),
        onTertiaryContainer = Color(0xFF1E2A0E),
        background = Color(0xFFFFF8E7),
        onBackground = Color(0xFF2B2117),
        surface = Color(0xFFFFFBF1),
        onSurface = Color(0xFF2B2117),
        surfaceVariant = Color(0xFFF5E6C4),
        onSurfaceVariant = Color(0xFF6B5A45),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFFFF4DC),
        surfaceContainer = Color(0xFFF7EBCD),
        surfaceContainerHigh = Color(0xFFF2E2BC),
        surfaceContainerHighest = Color(0xFFEDDAAE),
        outline = Color(0xFFC9A97A),
        outlineVariant = Color(0xFFE6D3AA),
        error = Color(0xFFB3261E),
        onError = Color(0xFFFFFFFF),
        inverseSurface = Color(0xFF3A2E22),
        inverseOnSurface = Color(0xFFFFF3DD),
        inversePrimary = CheeseColors.Gold,
    )

internal val DarkCheeseColors =
    darkColorScheme(
        primary = CheeseColors.Gold,
        onPrimary = Color(0xFF3A2800),
        primaryContainer = Color(0xFF5C4300),
        onPrimaryContainer = Color(0xFFFFE08A),
        secondary = Color(0xFFFFB870),
        onSecondary = Color(0xFF4A2800),
        secondaryContainer = Color(0xFF6A3B0A),
        onSecondaryContainer = Color(0xFFFFDCC0),
        tertiary = Color(0xFFB5C99A),
        onTertiary = Color(0xFF243312),
        tertiaryContainer = Color(0xFF3A4A26),
        onTertiaryContainer = Color(0xFFDCE8C8),
        background = Color(0xFF17120D),
        onBackground = Color(0xFFF3E6CF),
        surface = Color(0xFF211A13),
        onSurface = Color(0xFFF3E6CF),
        surfaceVariant = Color(0xFF3A2F23),
        onSurfaceVariant = Color(0xFFCDB99A),
        surfaceContainerLowest = Color(0xFF120E0A),
        surfaceContainerLow = Color(0xFF1D1711),
        surfaceContainer = Color(0xFF2B2219),
        surfaceContainerHigh = Color(0xFF352A1F),
        surfaceContainerHighest = Color(0xFF403326),
        outline = Color(0xFF7A6550),
        outlineVariant = Color(0xFF4A3C2E),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        inverseSurface = Color(0xFFF3E6CF),
        inverseOnSurface = Color(0xFF2B2117),
        inversePrimary = Color(0xFF8A6100),
    )
