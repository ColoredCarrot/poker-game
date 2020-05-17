package host

import comm.Host
import comm.msg.Messages
import comm.msg.handledBy
import comm.msg.jsonMessage
import comm.msg.send
import kotlinx.html.dom.append
import kotlinx.html.id
import kotlinx.html.js.a
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.p
import kotlinx.html.js.span
import render.AnteUpRenderer
import render.GameRenderer
import shared.Card
import shared.Chips
import shared.ConcreteCard
import shared.Hand
import shared.PlayerInfo
import shared.PlayerInfoList
import shared.PrivateGameState
import shared.Profile
import shared.Round
import shared.RoundAction
import shared.RoundLabel
import shared.RoundTable
import shared.SessionId
import shared.Table
import shared.nextName
import shared.takeHand
import kotlin.browser.document
import kotlin.dom.clear
import kotlin.random.Random

// Exactly one player in a game instance is the host.
// The host holds the game's state and communicates
// with all other clients.
class GameHost {

    private val connections = Host()

    private var activeStage: GameStage = WaitingForPlayers()

    init {
        connections.connectionRejector = { activeStage.allowsNewConnections }
        activeStage.render()

        connections.hookMessageTypes(Messages)
    }


    interface GameStage {

        val allowsNewConnections: Boolean

        fun render()

    }

    inner class WaitingForPlayers : GameStage {
        override val allowsNewConnections: Boolean
            get() = true

        override fun render() {
            document.body!!.clear()
            document.body!!.append {
                p {
                    +"Waiting for players... "
                    a("#") {
                        +"Start game with "
                        span {
                            id = "players-count-span"
                            +"0"
                        }
                        +" players"
                        onClickFunction = {
                            println("clicked to start game")
                            it.preventDefault()
                            onStartGame()
                        }
                    }
                }
            }
        }

        private val gameSettings = GameSettings()

        /** Called when host presses button to start game */
        private fun onStartGame() {
            val drawCards = Card.drawCards()

            // We already draw all community cards now,
            // but we won't reveal them all until the later rounds
            val communityCards = drawCards.take(COMMUNITY_CARDS_COUNT)

            // We, the host, get the first private game state,
            // and the rest of the hands are sent to the other connected players
            val pgsBySid: Map<String, PrivateGameState> = {
                val allSids = mutableListOf(connections.myPeerId)
                allSids += connections.peers()
                allSids.associateWith { sid ->
                    PrivateGameState(
                        PlayerInfo(
                            sid,
                            Profile(Random.nextName()),
                            drawCards.takeHand(2),
                            Chips(200)
                        )
                    )
                }
            }()

            val playerInfoByPeerId = pgsBySid.mapValues { (_, pgs) -> pgs.playerInfo }

            fun Map<String, PlayerInfo>.toSetDroppingKey(key: String): LinkedHashSet<PlayerInfo> {
                return values.filterTo(LinkedHashSet()) { it.sessionId != key }
            }

            val pot = Chips(gameSettings.ante * (connections.remotesCount + 1))

            val personalizedTables = pgsBySid.mapValues { (sid, pgs) ->
                Table(
                    pot,
                    PlayerInfoList(playerInfoByPeerId.toSetDroppingKey(sid)),
                    pgs
                )
            }

            val roundTable = RoundTable(listOf(connections.myPeerId) + connections.peers())
            val initialRound = Round(
                roundTable,
                RoundLabel.PREFLOP,
                gameSettings.ante,
                roundTable.index(0)
            )

            activeStage = Playing(
                personalizedTables[connections.myPeerId]!!,
                communityCards,
                initialRound,
                gameSettings
            )

            connections.sendDynamic { sid ->
                Messages.TotalGameReset(personalizedTables[sid]!!, initialRound.ante).jsonMessage()
            }

            activeStage.render()
        }
    }

    inner class Playing(
        private val table: Table,
        private val communityCards: List<Card>,
        private val round: Round,
        private val settings: GameSettings
    ) : GameStage {
        override val allowsNewConnections: Boolean
            get() = false

        // TODO the first player should not have the normal action center controls
        //  but rather a modal:  You are under the gun! [Check] [Bet x]
        // (right now, the action center controls work fine; check==call and bet==raise)

        private val gameRenderer = GameRenderer(
            table,
            round,
            GameRenderer.Callbacks(
                { someoneFold(mySessionId) },
                { someoneCall(mySessionId) },
                { someoneRaise(mySessionId, it) }
            )
        )

        private val mySessionId get() = table.mySessionId

        init {
            connections.receive(
                Messages.PerformRoundAction.Type handledBy { (msg) ->
                    when (val action = msg.action) {
                        RoundAction.Fold -> someoneFold(msg.actor)
                        RoundAction.Call -> someoneCall(msg.actor)
                        is RoundAction.Raise -> someoneRaise(msg.actor, action.raiseAmount)
                    }
                }
            )
        }


        //<editor-fold desc="All about round actions">
        private fun someoneFold(actor: SessionId) {
            table.allPlayers[actor].hasFolded = true

            round.advanceByFolding()
            if (actor != mySessionId) gameRenderer.renderOtherFold(actor)
            afterRoundAction(actor, RoundAction.Fold)
        }

        private fun someoneCall(actor: SessionId) {
            table.pot.value += round.amountToCall
            table.allPlayers[actor].money.value -= round.amountToCall

            round.advanceByCalling()
            if (actor != mySessionId) gameRenderer.renderOtherCall(actor)
            afterRoundAction(actor, RoundAction.Call)
        }

        private fun someoneRaise(actor: SessionId, raiseAmount: Int) {
            table.pot.value += round.amountToCall + raiseAmount
            table.allPlayers[actor].money.value -= round.amountToCall + raiseAmount

            round.advanceByRaising(raiseAmount)
            if (actor != mySessionId) gameRenderer.renderOtherRaise(actor, raiseAmount)
            afterRoundAction(actor, RoundAction.Raise(raiseAmount))
        }

        private fun afterRoundAction(actor: SessionId, action: RoundAction) {
            println("afterRoundAction actor=$actor. round after advancing = $round")

            val nextRound = round.isFinished()
            if (nextRound) {
                when (round.label) {
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
                round.nextRound()
                println("round was finished and is now = $round")
            }

            connections.send(
                Messages.UpdateRound(
                    amountToCall = round.amountToCall,
                    activePlayer = round.activePlayer.get(),
                    updatedMoneyPlayer = actor,
                    updatedMoneyValue = table.allPlayers[actor].money.value,
                    pot = table.pot.value,
                    reason = actor to action,
                    isNextRound = nextRound
                )
            )

            if (nextRound) gameRenderer.renderNextRound()

            rerender()
        }
        //</editor-fold>

        private fun revealCommunityCards(from: Int, until: Int) {
            for (player in table.allPlayers) {
                player.hand!!.cards += communityCards.subList(from, until).map { ConcreteCard(it, true) }
            }
            connections.send(Messages.SetHands(table.allPlayers.associate { it.sessionId to it.hand!! }))
        }

        private fun allRoundsHaveFinished() {
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

            table.winners.addAll(winners)
            rerender()
            connections.send(Messages.GameFinish(table.winners))

            //TODO clear/reset pot, distribute pot money to winners (though prolly from btn in action center)
            return
        }


        override fun render() {
            gameRenderer.render()
            AnteUpRenderer(settings.ante).render()
        }

        private fun rerender() {
            gameRenderer.rerender()
        }

    }

    private data class EvaluatedHand(val hand: Hand, val value: Int)


    companion object {
        private const val COMMUNITY_CARDS_COUNT = 5
    }
}
