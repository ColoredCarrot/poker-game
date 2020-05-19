package participant

import comm.msg.Messages
import comm.msg.Messenger
import comm.msg.handledBy
import comm.msg.send
import kotlinext.js.jsObject
import react.RBuilder
import react.RProps
import react.child
import react.functionalComponent
import react.getValue
import react.setValue
import react.useEffectWithCleanup
import react.useState
import shared.RoundAction
import shared.SessionId
import shared.Table
import shared.mapAllPlayers
import shared.mapPlayer
import shared.minus
import shared.plus
import shared.reorderMyHand
import shared.setHand
import shared.setMoney
import shared.setMyselfPlayerInfo
import shared.setPot
import usingreact.ActionCenterCallbacks
import usingreact.GameProps
import usingreact.game
import kotlin.browser.window

fun RBuilder.participantPlayingGamePhase(initialTable: Table, connection: Messenger<SessionId>) =
    child(ParticipantPlayingGamePhase, jsObject {
        this.initialTable = initialTable
        this.connection = connection
    })

private external interface ParticipantPlayingGamePhaseProps : RProps {
    var initialTable: Table
    var connection: Messenger<SessionId>
}

private val ParticipantPlayingGamePhase = functionalComponent<ParticipantPlayingGamePhaseProps> { props ->

    var table by useState(props.initialTable)
    var activePlayer by useState(props.initialTable.mySessionId)
    var amountToCall by useState(0)

    var recentAction by useState<Pair<RoundAction, SessionId>?>(null)
    //TODO actually render recentAction by giving it as prop to OtherPlayers

    useEffectWithCleanup(dependencies = emptyList()) {
        // We have mounted!
        // Let's register our listeners
        // TODO: since the effect hook is only run after painting, this might be too late and we might miss some messages.
        //  Possible solution: send confirmation to host here

        var clearRecentActionTimeout: Int? = null

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
                table = table.mapPlayer(m.updatedMoneyPlayer) { it.setMoney { it.setValue(m.updatedMoneyValue) } }
                table = table.setPot { it.setValue(m.pot) }

                val (actor, action) = m.reason
                if (actor != table.mySessionId) {
                    recentAction = action to actor
                    clearRecentActionTimeout = window.setTimeout({
                        recentAction = null
                        clearRecentActionTimeout = null
                    }, 2000)
                }

                if (m.isNextRound) {
                    //TODO gameRenderer.renderNextRound()
                }
            }
        )

        return@useEffectWithCleanup {
            // Cleanup! Remove our listeners
            props.connection.receive()

            // Also clear timeout
            clearRecentActionTimeout?.also { window.clearTimeout(it) }
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
                    table = table.setMyselfPlayerInfo { it.copy(hasFolded = true) }
                    props.connection.send(Messages.PerformRoundAction(table.mySessionId, RoundAction.Fold))
                },
                callFn = {
                    table = table.setMyselfPlayerInfo { it.setMoney { money -> money - amountToCall } }
                    table =
                        table.setPot { it + amountToCall } // note: we just do this for the most fluid UX; it would automatically be done because we receive the update from the host as well
                    props.connection.send(Messages.PerformRoundAction(table.mySessionId, RoundAction.Call))
                },
                raiseFn = { raiseAmount ->
                    table = table.setMyselfPlayerInfo { it.setMoney { money -> money - (amountToCall + raiseAmount) } }
                    table.setPot { pot -> pot + (amountToCall + raiseAmount) }
                    props.connection.send(
                        Messages.PerformRoundAction(
                            table.mySessionId,
                            RoundAction.Raise(raiseAmount)
                        )
                    )
                }
            ),
            onHandReorder = { table = table.reorderMyHand(it) }
        )
    )
}
