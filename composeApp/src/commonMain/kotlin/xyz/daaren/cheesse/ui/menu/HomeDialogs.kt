package xyz.daaren.cheesse.ui.menu

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import xyz.daaren.cheesse.api.GameColorPreference
import xyz.daaren.cheesse.ui.components.ButtonOptions
import xyz.daaren.cheesse.ui.components.CheesseButton
import xyz.daaren.cheesse.ui.components.CheesseDialog
import xyz.daaren.cheesse.ui.components.SegmentedChoice

private val colorChoices =
    listOf(
        GameColorPreference.WHITE to "White",
        GameColorPreference.RANDOM to "Random",
        GameColorPreference.BLACK to "Black",
    )

@Composable
internal fun HomeDialogs(
    dialog: HomeDialog,
    uiState: HomeScreenModelApi.HomeUiState,
    screenModel: HomeScreenModelApi,
    onDialogChange: (HomeDialog) -> Unit,
) {
    val close = { onDialogChange(HomeDialog.None) }
    when (dialog) {
        HomeDialog.Create ->
            CreateGameDialog(close) { preference ->
                close()
                screenModel.createGame(preference)
            }
        HomeDialog.Join -> JoinGameDialog(uiState, screenModel, close)
        HomeDialog.None -> Unit
    }
}

@Composable
private fun CreateGameDialog(
    onDismissRequest: () -> Unit,
    onCreate: (GameColorPreference) -> Unit,
) {
    CheesseDialog(title = "New online game", onDismissRequest = onDismissRequest) {
        var preference by remember { mutableStateOf(GameColorPreference.RANDOM) }
        Text(
            "Play as",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SegmentedChoice(colorChoices, preference) { preference = it }
        CheesseButton("Create game", { onCreate(preference) }, Modifier.fillMaxWidth())
    }
}

@Composable
private fun JoinGameDialog(
    uiState: HomeScreenModelApi.HomeUiState,
    screenModel: HomeScreenModelApi,
    onDismissRequest: () -> Unit,
) {
    CheesseDialog(title = "Join a game", onDismissRequest = onDismissRequest) {
        var input by remember { mutableStateOf("") }
        val error = uiState.errorMessage
        OutlinedTextField(
            value = input,
            onValueChange = {
                input = it
                if (error != null) screenModel.dismissError()
            },
            label = { Text("Join token") },
            singleLine = true,
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
        )
        CheesseButton(
            label = if (uiState.isLoading) "Joining…" else "Join",
            onClick = { screenModel.joinGame(input.trim()) },
            modifier = Modifier.fillMaxWidth(),
            options = ButtonOptions(enabled = input.isNotBlank(), loading = uiState.isLoading),
        )
    }
}
