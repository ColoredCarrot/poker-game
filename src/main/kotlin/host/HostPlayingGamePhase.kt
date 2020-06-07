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

package host

import comm.Host
import comm.msg.Messages
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
import shared.Card
import shared.ConcreteCard
import shared.Hand
import shared.InOutParam
import shared.Round
import shared.RoundAction
import shared.RoundLabel
import shared.RoundTable
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
import shared.setPot
import shared.setWinners
import usingreact.ActionCenterCallbacks
import usingreact.GameProps
import usingreact.game

fun RBuilder.hostPlayingGamePhase(
    connections: Host,
    gameSettings: GameSettings,
    communityCards: List<Card>,
    initialTable: Table
) = child(HostPlayingGamePhase, jsObject {
    this.connections = connections
    this.gameSettings = gameSettings
    this.communityCards = communityCards
    this.initialTable = initialTable
})

private external interface HostPlayingGamePhaseProps : RProps {
    var connections: Host
    var gameSettings: GameSettings
    var communityCards: List<Card>
    var initialTable: Table
}

private val HostPlayingGamePhase = functionalComponentEx<HostPlayingGamePhaseProps>("HostPlayingGamePhase") { props ->

    var round by useState {
        val roundTable = RoundTable(listOf(props.connections.peerId) + props.connections.peers())
        Round(
            roundTable,
            RoundLabel.PREFLOP,
            props.gameSettings.ante,
            roundTable.index(0)
        )
    }

    var table by useState(props.initialTable)

    var lastAction by useState<Pair<RoundAction, SessionId>?>(null)


    val allRoundsHaveFinished = {
        println("allRoundsHaveFinished()")

        val nonFoldedHands = table.allPlayers.asSequence()
            .filter { !it.hasFolded }
            .map { it.sessionId to it.hand!! }
            .map { (sid, hand) -> sid to EvaluatedHand(hand, hand.evaluate()) }
            .sortedByDescending { it.second.value }
            .toMap()
        // Might have multiple winners if it's a draw
        val winningValue = nonFoldedHands.entries.first().value.value
        val winners =
            nonFoldedHands.entries.takeWhile { (_, hand) -> hand.value == winningValue }
                .map { (peer, _) -> peer }

        table = table.setWinners { winners }
        props.connections.send(Messages.GameFinish(table.winners))

        //TODO clear/reset pot, distribute pot money to winners (though prolly from btn in action center)
    }

    val revealCommunityCards = { from: Int, until: Int, table: InOutParam<Table> ->
        table.value = table.value.mapAllPlayers { player ->
            player.setHand { hand ->
                hand!! + props.communityCards.subList(from, until).map { ConcreteCard(it, true) }
            }
        }
        props.connections.send(Messages.SetHands(table.value.allPlayers.associate { it.sessionId to it.hand!! }))
    }

    fun someoneRoundAction(actor: SessionId, action: RoundAction, newTable: Table, newRound: Round) {
        var newTable = newTable

        lastAction = if (actor != newTable.mySessionId) action to actor else null

        val nextRound = newRound.isFinished()
        if (nextRound) {
            when (newRound.label) {
                RoundLabel.PREFLOP -> {
                    // Reveal first three community cards (the flop)
                    revealCommunityCards(0, 3, InOutParam(newTable, { newTable = it }))
                }
                RoundLabel.FLOP -> {
                    // Reveal the fourth community card (the turn)
                    revealCommunityCards(3, 4, InOutParam(newTable, { newTable = it }))
                }
                RoundLabel.TURN -> {
                    // Reveal the fifth and final community card (the river)
                    revealCommunityCards(4, 5, InOutParam(newTable, { newTable = it }))
                }
                RoundLabel.RIVER -> {
                    // This is a special case because we just finished the last round.
                    // Let's calculate the winner etc. etc.
                    allRoundsHaveFinished()
                }
            }
            newRound.nextRound()
            println("round was finished and is now = $newRound")
        }

        // These are deferred operations, thus we need to operate on newTable/Round lest we use stale data
        table = newTable
        round = newRound

        props.connections.send(
            Messages.UpdateRound(
                amountToCall = newRound.amountToCall,
                activePlayer = newRound.activePlayer.get(),
                updatedMoneyPlayer = actor,
                updatedMoneyValue = newTable.allPlayers[actor].money.value,
                pot = newTable.pot.value,
                reason = actor to action,
                isNextRound = if (nextRound) Messages.UpdateRound.NextRoundInfo(
                    label = newRound.label,
                    underTheGun = newRound.activePlayer.get()
                ) else null
            )
        )

        if (nextRound) {
            renderNextRoundPopup(newRound.label, newTable.getName(newRound.activePlayer.get()))
        }
    }

    val someoneFold = { actor: SessionId ->
        someoneRoundAction(
            actor,
            RoundAction.Fold,
            newTable = table.mapPlayer(actor) { it.copy(hasFolded = true) },
            newRound = round.copy().also { it.advanceByFolding() }
        )
    }
    val someoneCall = { actor: SessionId ->
        someoneRoundAction(
            actor,
            RoundAction.Call,
            newTable = table
                .setPot { it + round.amountToCall }
                .mapPlayer(actor) { it.setMoney { it - round.amountToCall } },
            newRound = round
                .copy().also { it.advanceByCalling() }
        )
    }
    val someoneRaise = { actor: SessionId, raiseAmount: Int ->
        val newTable = table
            .setPot { it + (round.amountToCall + raiseAmount) }
            .mapPlayer(actor) { it.setMoney { it - (round.amountToCall + raiseAmount) } }
        val newRound = round
            .copy().also { it.advanceByRaising(raiseAmount) }
        someoneRoundAction(actor, RoundAction.Raise(raiseAmount), newTable, newRound)
    }


    // Unfortunately, we cannot give an empty dependency list:
    // The effect must run on every update because otherwise we'd use stale state.
    useEffectWithCleanup {
        props.connections.receive(
            Messages.PerformRoundAction.Type handledBy { (msg) ->
                when (val action = msg.action) {
                    RoundAction.Fold -> someoneFold(msg.actor)
                    RoundAction.Call -> someoneCall(msg.actor)
                    is RoundAction.Raise -> someoneRaise(msg.actor, action.raiseAmount)
                }
            }
        )

        return@useEffectWithCleanup {
            props.connections.receive()
        }
    }


    game(
        GameProps(
            table = table,
            activePlayer = round.activePlayer.get(),
            amountToCall = round.amountToCall,
            actionCenterCallbacks = ActionCenterCallbacks(
                foldFn = { someoneFold(table.mySessionId) },
                callFn = { someoneCall(table.mySessionId) },
                raiseFn = { someoneRaise(table.mySessionId, it) }
            ),
            onHandReorder = { newOrder -> table = table.reorderMyHand(newOrder) },
            lastAction = lastAction
        )
    )
}

private data class EvaluatedHand(val hand: Hand, val value: Int)
