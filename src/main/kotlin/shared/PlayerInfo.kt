/*
 * Copyright 2020 Julian Koch and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

inline fun PlayerInfo.setHand(hand: (Hand?) -> Hand?) = copy(hand = hand(this.hand))

inline fun PlayerInfo.setMoney(money: (Chips) -> Chips) = copy(money = money(this.money))

@Serializable
data class PlayerInfoList(val all: LinkedHashSet<PlayerInfo>) : Iterable<PlayerInfo> by all {

    operator fun get(sessionId: String) = all.first { it.sessionId == sessionId }

}
