package shared

import kotlinx.serialization.Serializable

@Serializable
data class PlayerInfo(
    val sessionId: SessionId,
    val profile: Profile,
    var hand: Hand?,
    val money: Chips,
    var hasFolded: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlayerInfo) return false
        return sessionId == other.sessionId
    }

    override fun hashCode(): Int {
        return sessionId.hashCode()
    }
}

@Serializable
data class PlayerInfoList(val all: LinkedHashSet<PlayerInfo>) : Iterable<PlayerInfo> by all {

    operator fun get(sessionId: String) = all.first { it.sessionId == sessionId }

}
