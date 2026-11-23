package xyz.daaren.cheesse.ui.menu

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import xyz.daaren.cheesse.ui.components.ButtonOptions
import xyz.daaren.cheesse.ui.components.ButtonVariant
import xyz.daaren.cheesse.ui.components.CheesseButton
import xyz.daaren.cheesse.ui.components.CheesseCard
import xyz.daaren.cheesse.ui.components.PulsingDot

@Composable
internal fun LobbyCard(
    lobby: HomeScreenModelApi.WaitingLobby,
    onCancel: () -> Unit,
) {
    CheesseCard(Modifier.fillMaxWidth()) {
        Text("Lobby ready", style = MaterialTheme.typography.titleLarge)
        Text(
            "Share this token with your opponent",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        TokenChip(lobby.joinToken)
        LobbyStatus(lobby.isOpponentConnected)
        CheesseButton("Cancel", onCancel, Modifier.fillMaxWidth(), ButtonOptions(ButtonVariant.Outlined))
    }
}

@Composable
private fun TokenChip(token: String) {
    SelectionContainer {
        Text(
            text = token,
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
        )
    }
}

@Composable
private fun LobbyStatus(isOpponentConnected: Boolean) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        PulsingDot(if (isOpponentConnected) colors.tertiary else colors.primary)
        AnimatedContent(isOpponentConnected) { connected ->
            Text(
                if (connected) "Opponent joined. Starting game…" else "Waiting for opponent…",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }
    }
}
