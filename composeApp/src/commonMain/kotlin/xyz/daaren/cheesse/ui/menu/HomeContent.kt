package xyz.daaren.cheesse.ui.menu

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import cheesse.composeapp.generated.resources.Res
import cheesse.composeapp.generated.resources.cheesse_icon
import cheesse.composeapp.generated.resources.ic_join
import cheesse.composeapp.generated.resources.ic_online
import cheesse.composeapp.generated.resources.ic_person
import cheesse.composeapp.generated.resources.ic_settings
import org.jetbrains.compose.resources.painterResource
import xyz.daaren.cheesse.ui.components.ButtonOptions
import xyz.daaren.cheesse.ui.components.ButtonVariant
import xyz.daaren.cheesse.ui.components.CheesseButton
import xyz.daaren.cheesse.ui.components.CheesseCard
import xyz.daaren.cheesse.ui.theme.Motion

internal data class HomeActions(
    val onPlayLocal: () -> Unit,
    val onCreateOnline: () -> Unit,
    val onJoin: () -> Unit,
    val onSettings: () -> Unit,
    val onCancelLobby: () -> Unit,
)

@Composable
internal fun HomeContent(
    uiState: HomeScreenModelApi.HomeUiState,
    actions: HomeActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
    ) {
        HomeHero()
        AnimatedContent(
            targetState = uiState.waitingLobby,
            contentKey = { it != null },
            transitionSpec = { (fadeIn(tween(Motion.LONG_MS)) + scaleIn(initialScale = 0.94f)) togetherWith fadeOut() },
            modifier = Modifier.widthIn(max = 420.dp),
        ) { lobby ->
            if (lobby == null) {
                MainActions(uiState.isLoading, actions)
            } else {
                LobbyCard(lobby, actions.onCancelLobby)
            }
        }
    }
}

@Composable
private fun HomeHero() {
    val breath by rememberInfiniteTransition(label = "logo").animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Reverse),
        label = "logoScale",
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            Modifier
                .size(112.dp)
                .graphicsLayer {
                    scaleX = breath
                    scaleY = breath
                }.background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(painterResource(Res.drawable.cheesse_icon), contentDescription = "Cheesse logo", Modifier.size(72.dp))
        }
        Text("Cheesse", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground)
//        Text(
//            "Chess, but make it gouda.",
//            style = MaterialTheme.typography.bodyLarge,
//            color = MaterialTheme.colorScheme.onSurfaceVariant,
//        )
    }
}

@Composable
private fun MainActions(
    isLoading: Boolean,
    actions: HomeActions,
) {
    CheesseCard(Modifier.fillMaxWidth()) {
        val wide = Modifier.fillMaxWidth()
        val tonal = ButtonOptions(ButtonVariant.Tonal, enabled = !isLoading)
        CheesseButton("Play local", actions.onPlayLocal, wide, ButtonOptions(icon = Res.drawable.ic_person, enabled = !isLoading))
        CheesseButton("Create online game", actions.onCreateOnline, wide, tonal.copy(icon = Res.drawable.ic_online))
        CheesseButton("Join with token", actions.onJoin, wide, tonal.copy(icon = Res.drawable.ic_join))
        CheesseButton(
            "Settings",
            actions.onSettings,
            wide,
            ButtonOptions(ButtonVariant.Text, Res.drawable.ic_settings, enabled = !isLoading),
        )
    }
}
