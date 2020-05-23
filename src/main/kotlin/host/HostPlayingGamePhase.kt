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
import react.useEffect
import react.useState
import reactutils.functionalComponentEx
import shared.Card
import shared.ConcreteCard
import shared.Hand
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
        val roundTable = RoundTable(listOf(props.connections.myPeerId) + props.connections.peers())
        Round(
            roundTable,
            RoundLabel.PREFLOP,
            props.gameSettings.ante,
            roundTable.index(0)
        )
    }

    var table by useState(props.initialTable)


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

    val revealCommunityCards = { from: Int, until: Int ->
        table = table.mapAllPlayers { player ->
            player.setHand { hand ->
                hand!! + props.communityCards.subList(from, until).map { ConcreteCard(it, true) }
            }
        }
        props.connections.send(Messages.SetHands(table.allPlayers.associate { it.sessionId to it.hand!! }))
    }

    fun someoneRoundAction(actor: SessionId, action: RoundAction, newTable: Table, newRound: Round) {
        val nextRound = newRound.isFinished()
        if (nextRound) {
            when (newRound.label) {
                RoundLabel.PREFLOP -> {
                    // Reveal first three community cards (the flop)
                    revealCommunityCards(0, 3)
                }
                RoundLabel.FLOP -> {
                    // Reveal the fourth community card (the turn)
                    revealCommunityCards(3, 4)
                }
                RoundLabel.TURN -> {
                    // Reveal the fifth and final community card (the river)
                    revealCommunityCards(4, 5)
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
                isNextRound = nextRound
            )
        )

        //TODO if (nextRound) gameRenderer.renderNextRound()
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
    useEffect {
        props.connections.receive(
            Messages.PerformRoundAction.Type handledBy { (msg) ->
                when (val action = msg.action) {
                    RoundAction.Fold -> someoneFold(msg.actor)
                    RoundAction.Call -> someoneCall(msg.actor)
                    is RoundAction.Raise -> someoneRaise(msg.actor, action.raiseAmount)
                }
            }
        )
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
            lastAction = null /*TODO recentAction for host. prolly best to add a showRecentAction state to Game instead of duplicating that logic*/
        )
    )
}

private data class EvaluatedHand(val hand: Hand, val value: Int)
