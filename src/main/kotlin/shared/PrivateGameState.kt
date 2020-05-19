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
)

inline fun PrivateGameState.setPlayerInfo(playerInfo: (PlayerInfo) -> PlayerInfo) =
    copy(playerInfo = playerInfo(this.playerInfo))
