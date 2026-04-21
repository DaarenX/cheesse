package xyz.daaren.cheesse.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cheesse.composeapp.generated.resources.Res
import cheesse.composeapp.generated.resources.arrow_back
import org.jetbrains.compose.resources.painterResource

class SettingsScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel<SettingsViewModelApi>()
        val currentUrl by viewModel.url.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Settings") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                painter = painterResource(Res.drawable.arrow_back),
                                contentDescription = "Back Arrow Icon",
                            )
                        }
                    },
                )
            },
        ) { paddingValues ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                SettingsScreenContent(initialUrl = currentUrl) {
                    viewModel.saveUrl(it)
                    navigator.pop()
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TextField(
                value = url,
                onValueChange = { url = it.trim() },
                label = { Text("Server URL") },
            )

            Button(
                onClick = { onSaveUrl(url) },
            ) {
                Text("Save")
            }
        }
    }

    @Composable
    @Preview
    private fun SettingsScreenContentPreview() {
        SettingsScreenContent(initialUrl = "http://127.0.0.1:8080") {}
    }
}
