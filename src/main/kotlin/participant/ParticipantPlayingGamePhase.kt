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

package participant

import comm.msg.Messages
import comm.msg.Messenger
import comm.msg.handledBy
import comm.msg.send
import kotlinext.js.jsObject
import react.RBuilder
import react.RProps
import react.child
import react.getValue
import react.setValue
import react.useEffectWithCleanup
import react.useState
import reactutils.functionalComponentEx
import shared.RoundAction
import shared.SessionId
import shared.Table
import shared.mapAllPlayers
import shared.mapPlayer
import shared.minus
import shared.plus
import shared.renderNextRoundPopup
import shared.reorderMyHand
import shared.setHand
import shared.setMoney
import shared.setMyselfPlayerInfo
import shared.setPot
import usingreact.ActionCenterCallbacks
import usingreact.GameProps
import usingreact.game

fun RBuilder.participantPlayingGamePhase(
    initialTable: Table,
    initialActivePlayer: SessionId?,
    connection: Messenger<SessionId>
) =
    child(ParticipantPlayingGamePhase, jsObject {
        this.initialTable = initialTable
        this.initialActivePlayer = initialActivePlayer
        this.connection = connection
    })

private external interface ParticipantPlayingGamePhaseProps : RProps {
    var initialTable: Table
    var initialActivePlayer: SessionId?
    var connection: Messenger<SessionId>
}

private val ParticipantPlayingGamePhase =
    functionalComponentEx<ParticipantPlayingGamePhaseProps>("ParticipantPlayingGamePhase") { props ->

        var table by useState(props.initialTable)
        var activePlayer by useState(props.initialActivePlayer)
        var amountToCall by useState(0)

        var lastAction by useState<Pair<RoundAction, SessionId>?>(null)

        useEffectWithCleanup {
            // We have mounted!
            // Let's register our listeners
            // TODO: since the effect hook is only run after painting, this might be too late and we might miss some messages.
            //  Possible solution: send confirmation to host here

            props.connection.receive(
                Messages.SetHands.Type handledBy { (m) ->
                    table = table.mapAllPlayers { player ->
                        player.setHand { oldHand ->
                            m.newHands[player.sessionId] ?: oldHand
                        }
                    }
                },

                Messages.UpdateRound.Type handledBy { (m) ->
                    amountToCall = m.amountToCall
                    activePlayer = m.activePlayer
                    table = table
                        .mapPlayer(m.updatedMoneyPlayer) { it.setMoney { it.setValue(m.updatedMoneyValue) } }
                        .setPot { it.setValue(m.pot) }

                    val (actor, action) = m.reason
                    if (actor != table.mySessionId) {
                        lastAction = action to actor
                    }

                    if (m.isNextRound != null) {
                        renderNextRoundPopup(m.isNextRound.label, table.getName(m.isNextRound.underTheGun))
                    }
                }
            )

            return@useEffectWithCleanup {
                // Cleanup! Remove our listeners
                props.connection.receive()
            }
        }

        //TODO render ante up (though prolly in parent)

        game(
            GameProps(
                table = table,
                activePlayer = activePlayer,
                amountToCall = amountToCall,
                actionCenterCallbacks = ActionCenterCallbacks(
                    foldFn = {
                        val newTable = table.setMyselfPlayerInfo { it.copy(hasFolded = true) }
                        table = newTable
                        props.connection.send(Messages.PerformRoundAction(newTable.mySessionId, RoundAction.Fold))
                    },
                    callFn = {
                        val newTable = table
                            .setMyselfPlayerInfo { it.setMoney { money -> money - amountToCall } }
                            .setPot { it + amountToCall } // note: we just do this for the most fluid UX; it would automatically be done because we receive the update from the host as well
                        table = newTable
                        props.connection.send(Messages.PerformRoundAction(newTable.mySessionId, RoundAction.Call))
                    },
                    raiseFn = { raiseAmount ->
                        val newTable = table
                            .setMyselfPlayerInfo { it.setMoney { money -> money - (amountToCall + raiseAmount) } }
                            .setPot { pot -> pot + (amountToCall + raiseAmount) }
                        table = newTable
                        props.connection
                            .send(Messages.PerformRoundAction(newTable.mySessionId, RoundAction.Raise(raiseAmount)))
                    }
                ),
                onHandReorder = { table = table.reorderMyHand(it) },
                lastAction = lastAction
            )
        )
    }
