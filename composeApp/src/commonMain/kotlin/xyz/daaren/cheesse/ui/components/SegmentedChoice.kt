package xyz.daaren.cheesse.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import xyz.daaren.cheesse.ui.theme.Motion
import xyz.daaren.cheesse.ui.theme.PillShape

/** Pill-shaped single choice control; the selected segment fills with the primary color. */
@Composable
fun <T> SegmentedChoice(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(PillShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(4.dp),
    ) {
        options.forEach { (value, label) ->
            Segment(label, value == selected) { onSelect(value) }
        }
    }
}

@Composable
private fun RowScope.Segment(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val background by animateColorAsState(if (isSelected) colors.primary else Color.Transparent, Motion.colorChange)
    val foreground by animateColorAsState(if (isSelected) colors.onPrimary else colors.onSurfaceVariant, Motion.colorChange)
    Box(
        Modifier
            .weight(1f)
            .clip(PillShape)
            .background(background)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = foreground)
    }
}
