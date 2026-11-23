package xyz.daaren.cheesse.ui.game.board

import androidx.compose.runtime.Composable
import cheesse.composeapp.generated.resources.Res
import cheesse.composeapp.generated.resources.arrow_back
import cheesse.composeapp.generated.resources.ic_join
import cheesse.composeapp.generated.resources.ic_online
import cheesse.composeapp.generated.resources.ic_person
import cheesse.composeapp.generated.resources.ic_settings
import cheesse.composeapp.generated.resources.screen_rotation_alt
import org.jetbrains.compose.resources.painterResource
import xyz.daaren.cheesse.domain.chess.ChessPieceType
import xyz.daaren.cheesse.ui.game.getIcon

private val preloadedIcons =
    listOf(
        Res.drawable.arrow_back,
        Res.drawable.screen_rotation_alt,
        Res.drawable.ic_person,
        Res.drawable.ic_online,
        Res.drawable.ic_join,
        Res.drawable.ic_settings,
    )

/** Loads every icon once up front, so the game screen never shows an empty first frame. */
@Composable
fun PreloadIcons() {
    ChessPieceType.entries.forEach { painterResource(it.getIcon()) }
    preloadedIcons.forEach { painterResource(it) }
}
