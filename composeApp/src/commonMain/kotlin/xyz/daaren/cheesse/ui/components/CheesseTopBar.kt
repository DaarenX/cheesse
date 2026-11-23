package xyz.daaren.cheesse.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import cheesse.composeapp.generated.resources.Res
import cheesse.composeapp.generated.resources.arrow_back
import org.jetbrains.compose.resources.painterResource

/** Transparent top bar with a back arrow, so the screen background shows through. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheesseTopBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(painterResource(Res.drawable.arrow_back), contentDescription = "Back")
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
    )
}
