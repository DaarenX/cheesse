package xyz.daaren.cheesse.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import xyz.daaren.cheesse.ui.components.CheeseHolesBackground
import xyz.daaren.cheesse.ui.components.CheesseButton
import xyz.daaren.cheesse.ui.components.CheesseCard
import xyz.daaren.cheesse.ui.components.CheesseTopBar
import xyz.daaren.cheesse.ui.theme.CheesseTheme

class SettingsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel<SettingsViewModelApi>()
        val currentUrl by viewModel.url.collectAsState()

        Box {
            CheeseHolesBackground()
            Scaffold(
                topBar = { CheesseTopBar("Settings", onBack = { navigator.pop() }) },
                containerColor = Color.Transparent,
            ) { paddingValues ->
                Box(
                    Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    SettingsScreenContent(initialUrl = currentUrl) {
                        viewModel.saveUrl(it)
                        navigator.pop()
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreenContent(
    initialUrl: String,
    onSaveUrl: (String) -> Unit,
) {
    var url by remember(initialUrl) { mutableStateOf(initialUrl) }
    CheesseCard(Modifier.widthIn(max = 420.dp).fillMaxWidth()) {
        Text("Server", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth())
        Text(
            "The Cheesse server used for online games.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = url,
            onValueChange = { url = it.trim() },
            label = { Text("Server URL") },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
        )
        CheesseButton("Save", { onSaveUrl(url) }, Modifier.fillMaxWidth())
    }
}

@Composable
@Preview
private fun SettingsScreenContentPreview() {
    CheesseTheme {
        SettingsScreenContent(initialUrl = "http://127.0.0.1:8080") {}
    }
}
