package xyz.daaren.cheesse.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import cheesse.composeapp.generated.resources.Res
import cheesse.composeapp.generated.resources.nunito_bold
import cheesse.composeapp.generated.resources.nunito_extrabold
import cheesse.composeapp.generated.resources.nunito_regular
import cheesse.composeapp.generated.resources.nunito_semibold
import org.jetbrains.compose.resources.Font

@Composable
fun nunitoFontFamily(): FontFamily =
    FontFamily(
        Font(Res.font.nunito_regular, FontWeight.Normal),
        Font(Res.font.nunito_semibold, FontWeight.SemiBold),
        Font(Res.font.nunito_bold, FontWeight.Bold),
        Font(Res.font.nunito_extrabold, FontWeight.ExtraBold),
    )

@Composable
internal fun cheesseTypography(): Typography {
    val nunito = nunitoFontFamily()
    val base = Typography()

    fun TextStyle.with(weight: FontWeight) = copy(fontFamily = nunito, fontWeight = weight)
    return Typography(
        displayLarge = base.displayLarge.with(FontWeight.ExtraBold).copy(letterSpacing = (-1).sp),
        displayMedium = base.displayMedium.with(FontWeight.ExtraBold).copy(letterSpacing = (-0.5).sp),
        displaySmall = base.displaySmall.with(FontWeight.ExtraBold),
        headlineLarge = base.headlineLarge.with(FontWeight.ExtraBold),
        headlineMedium = base.headlineMedium.with(FontWeight.ExtraBold),
        headlineSmall = base.headlineSmall.with(FontWeight.Bold),
        titleLarge = base.titleLarge.with(FontWeight.Bold),
        titleMedium = base.titleMedium.with(FontWeight.Bold),
        titleSmall = base.titleSmall.with(FontWeight.Bold),
        bodyLarge = base.bodyLarge.with(FontWeight.Normal),
        bodyMedium = base.bodyMedium.with(FontWeight.Normal),
        bodySmall = base.bodySmall.with(FontWeight.Normal),
        labelLarge = base.labelLarge.with(FontWeight.Bold),
        labelMedium = base.labelMedium.with(FontWeight.SemiBold),
        labelSmall = base.labelSmall.with(FontWeight.SemiBold),
    )
}
