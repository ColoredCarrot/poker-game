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
import kotlinx.serialization.Transient

@Serializable
class Table(
    val pot: Chips,
    val otherPlayers: PlayerInfoList,
    val myself: PrivateGameState
) {

    @Transient
    val winners = ArrayList<SessionId>()

    constructor(
        pot: Chips, otherPlayers: PlayerInfoList, myself: PrivateGameState,
        winners: List<SessionId>
    ) : this(pot, otherPlayers, myself) {
        this.winners += winners
    }

    val allPlayers get() = PlayerInfoList(LinkedHashSet(otherPlayers.all).also { it += myself.playerInfo })

    val mySessionId get() = myself.playerInfo.sessionId

    val allSessionIds get() = listOf(mySessionId) + otherPlayers.all.map { it.sessionId }

    fun getProfile(sid: SessionId) =
        if (sid == mySessionId) myself.playerInfo.profile
        else otherPlayers[sid].profile

    fun getName(sid: SessionId) = getProfile(sid).name

}

inline fun Table.setPot(pot: (Chips) -> Chips) = Table(
    pot(this.pot),
    otherPlayers, myself, winners
)

inline fun Table.setMyself(myself: (PrivateGameState) -> PrivateGameState) = Table(
    pot, otherPlayers, myself(this.myself), winners
)

inline fun Table.setWinners(winners: (List<SessionId>) -> List<SessionId>) = Table(
    pot, otherPlayers, myself, winners(this.winners)
)

inline fun Table.setMyselfPlayerInfo(myselfPlayerInfo: (PlayerInfo) -> PlayerInfo) =
    setMyself { it.setPlayerInfo(myselfPlayerInfo) }

fun Table.reorderMyHand(newOrder: List<Int>) = Table(
    pot, otherPlayers,
    myself.setPlayerInfo { it.setHand { it!!.reorderManually(newOrder) } },
    winners
)

inline fun Table.mapAllPlayers(map: (PlayerInfo) -> PlayerInfo) =
    Table(
        pot,
        PlayerInfoList(otherPlayers.mapTo(LinkedHashSet(), map)),
        myself.setPlayerInfo(map),
        winners
    )

inline fun Table.mapPlayer(player: SessionId, map: (PlayerInfo) -> PlayerInfo) =
    mapAllPlayers { if (it.sessionId == player) map(it) else it }
