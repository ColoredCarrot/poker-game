package shared

import COMM_JSON
import kotlinx.serialization.Serializable

/**
 * Encapsulates the entire personal game state known to
 * one individual player.
 */
@Serializable
class PrivateGameState(
    val playerInfo: PlayerInfo
) {

    companion object {
        fun serialize(privateGameState: PrivateGameState) = COMM_JSON.stringify(serializer(), privateGameState)

        fun deserialize(string: String) = COMM_JSON.parse(serializer(), string)
    }

}
