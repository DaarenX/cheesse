package xyz.daaren.cheesse.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import xyz.daaren.cheesse.ui.theme.Motion
import xyz.daaren.cheesse.ui.theme.PillShape

enum class ButtonVariant { Primary, Tonal, Outlined, Text }

@Immutable
data class ButtonOptions(
    val variant: ButtonVariant = ButtonVariant.Primary,
    val icon: DrawableResource? = null,
    val enabled: Boolean = true,
    val loading: Boolean = false,
)

/** Pill-shaped button that gently shrinks while pressed. */
@Composable
fun CheesseButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    options: ButtonOptions = ButtonOptions(),
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, Motion.press)
    val sized =
        modifier.heightIn(min = 52.dp).graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    val enabled = options.enabled && !options.loading
    val content: @Composable RowScope.() -> Unit = { ButtonContent(label, options) }
    when (options.variant) {
        ButtonVariant.Primary -> Button(onClick, sized, enabled, PillShape, interactionSource = interaction, content = content)
        ButtonVariant.Tonal ->
            FilledTonalButton(
                onClick,
                sized,
                enabled,
                PillShape,
                tonalColors(),
                interactionSource = interaction,
                content = content,
            )
        ButtonVariant.Outlined -> OutlinedButton(onClick, sized, enabled, PillShape, interactionSource = interaction, content = content)
        ButtonVariant.Text ->
            TextButton(
                onClick,
                sized,
                enabled,
                PillShape,
                inkColors(),
                interactionSource = interaction,
                content = content,
            )
    }
}

// Pale gold instead of the default secondary container, so tonal buttons stay in the cheese family.
@Composable
private fun tonalColors() =
    ButtonDefaults.filledTonalButtonColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )

// Gold text is too faint on cream, so text buttons use the darker on-container ink.
@Composable
private fun inkColors() = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer)

@Composable
private fun ButtonContent(
    label: String,
    options: ButtonOptions,
) {
    if (options.loading) {
        CircularProgressIndicator(Modifier.size(18.dp), color = LocalContentColor.current, strokeWidth = 2.dp)
        Spacer(Modifier.width(10.dp))
    } else if (options.icon != null) {
        Icon(painterResource(options.icon), contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
    }
    Text(label)
}
