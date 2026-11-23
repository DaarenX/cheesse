package xyz.daaren.cheesse.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import xyz.daaren.cheesse.api.PlayerColor
import xyz.daaren.cheesse.ui.game.board.Chessboard

private val Gap = 12.dp
private val BarHeight = 64.dp
private val SidePanelWidth = 280.dp
private val MinBoardSize = 200.dp

internal data class GameBodyState(
    val gameState: GameScreenModelApi.GameState,
    val rotation: PlayerColor,
    val youColor: PlayerColor?,
)

/** Board between two player bars in portrait; board beside the bars when there is more width than height. */
@Composable
internal fun GameBody(
    state: GameBodyState,
    onMove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        if (maxWidth > maxHeight * 1.2f) {
            val boardSize = minOf(maxHeight, maxWidth - SidePanelWidth - Gap * 2).coerceAtLeast(MinBoardSize)
            Row(horizontalArrangement = Arrangement.spacedBy(Gap * 2), verticalAlignment = Alignment.CenterVertically) {
                Chessboard(state.gameState, state.rotation, onMove, Modifier.size(boardSize))
                PlayerBars(state, Modifier.width(SidePanelWidth).height(boardSize), Arrangement.SpaceBetween) {}
            }
        } else {
            val boardSize = minOf(maxWidth, maxHeight - BarHeight * 2 - Gap * 2).coerceAtLeast(MinBoardSize)
            PlayerBars(state, Modifier.width(boardSize), Arrangement.spacedBy(Gap)) {
                Chessboard(state.gameState, state.rotation, onMove, Modifier.size(boardSize))
            }
        }
    }
}

/** The opponent's bar on top, your bar at the bottom, and [middle] in between. */
@Composable
private fun PlayerBars(
    state: GameBodyState,
    modifier: Modifier,
    arrangement: Arrangement.Vertical,
    middle: @Composable () -> Unit,
) {
    val top = if (state.rotation == PlayerColor.WHITE) PlayerColor.BLACK else PlayerColor.WHITE
    Column(modifier, verticalArrangement = arrangement) {
        PlayerBar(playerBarState(top, state.gameState, state.youColor))
        middle()
        PlayerBar(playerBarState(state.rotation, state.gameState, state.youColor))
    }
}

private fun Dp.coerceAtLeast(min: Dp): Dp = if (this < min) min else this
