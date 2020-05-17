package participant

import comm.Participant
import comm.msg.Messages
import comm.msg.handledBy
import comm.msg.send
import kotlinx.html.js.div
import kotlinx.html.js.p
import render.AnteUpRenderer
import render.GameRenderer
import shared.RenderUtil
import shared.RoundAction
import shared.SessionId
import shared.Table

class GameParticipant(connectToGameId: String) {

    private val connection = Participant()
    private var activeStage: Stage = WaitingForGameStart()

    init {
        connection.connect(connectToGameId)
        connection.hookMessageTypes(Messages)
        activeStage.render()
    }

    private interface Stage {
        fun render()
    }

    inner class WaitingForGameStart : Stage {

        init {
            connection.receive(
                Messages.TotalGameReset.Type handledBy { (m) ->
                    println("handle initial TotalGameReset")
                    activeStage = Playing(m.yourTable, m.ante)
                }
            )
        }

        override fun render() {
            RenderUtil.renderToBody {
                div("uk-container primary-container") {
                    attributes["uk-height-viewport"] = ""

                    p { +"Waiting for game to start..." }
                }
            }
        }
    }

    inner class Playing(private val table: Table, firstAnte: Int) : Stage {

        private var activePlayer: SessionId? = null
        private var amountToCall: Int = 0

        private val gameRenderer = GameRenderer(
            table,
            getAmountToCallFn = { amountToCall },
            getActivePlayerFn = { activePlayer },
            callbacks = GameRenderer.Callbacks(::fold, ::call, ::raise)
        )

        init {
            connection.receive(
                Messages.SetHands.Type handledBy { (m) ->
                    table.allPlayers.forEach { player ->
                        m.newHands[player.sessionId]?.also { player.hand = it }
                    }
                    rerender()
                },

                Messages.UpdateRound.Type handledBy { (m) ->
                    amountToCall = m.amountToCall
                    activePlayer = m.activePlayer
                    table.allPlayers[m.updatedMoneyPlayer].money.value = m.updatedMoneyValue
                    table.pot.value = m.pot

                    val (actor, action) = m.reason
                    if (actor != table.mySessionId) {
                        when (action) {
                            RoundAction.Fold -> otherFold(actor)
                            RoundAction.Call -> otherCall(actor)
                            is RoundAction.Raise -> otherRaise(actor, action.raiseAmount)
                        }
                    }

                    if (m.isNextRound) {
                        gameRenderer.renderNextRound()
                    }

                    rerender()
                }
            )

            render()
            AnteUpRenderer(firstAnte).render()
        }

        override fun render() {
            gameRenderer.render()
        }

        private fun rerender() {
            gameRenderer.rerender()
        }


        private fun fold() {
            table.myself.playerInfo.hasFolded = true
            rerender()
            connection.send(Messages.PerformRoundAction(table.mySessionId, RoundAction.Fold))
        }

        private fun call() {
            table.myself.playerInfo.money.value -= amountToCall
            table.pot.value += amountToCall // note: we just do this for the most fluid UX; it would automatically be done because we receive the update from the host as well
            rerender()
            connection.send(Messages.PerformRoundAction(table.mySessionId, RoundAction.Call))
        }

        private fun raise(raiseAmount: Int) {
            table.myself.playerInfo.money.value -= amountToCall + raiseAmount
            table.pot.value += amountToCall + raiseAmount
            rerender()
            connection.send(Messages.PerformRoundAction(table.mySessionId, RoundAction.Raise(raiseAmount)))
        }


        //<editor-fold desc="otherFold to gameRenderer.otherFold delegates (and family)">
        private fun otherFold(player: SessionId) {
            gameRenderer.renderOtherFold(player)
        }

        private fun otherCall(player: SessionId) {
            gameRenderer.renderOtherCall(player)
        }

        private fun otherRaise(player: SessionId, raiseAmount: Int) {
            gameRenderer.renderOtherRaise(player, raiseAmount)
        }
        //</editor-fold>

    }

}
