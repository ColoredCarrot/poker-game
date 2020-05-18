package shared

import kotlinx.serialization.Serializable

/**
 * Encapsulates the entire personal game state known to
 * one individual player.
 */
// TODO make class hierarchy immutable
@Serializable
data class PrivateGameState(
    val playerInfo: PlayerInfo
) {

    inline fun alterPlayerInfo(block: (PlayerInfo) -> Unit): PrivateGameState {
        return copy(playerInfo = playerInfo.copy().also(block))
    }

}
