package usingreact

import react.RBuilder
import react.RProps
import react.dom.div
import reactutils.functionalComponentEx
import shared.RoundAction
import shared.SessionId
import shared.Table
import shared.attrsApplyStyle
import shared.htmlAttrs
import shared.swap

data class GameProps(
    val table: Table,
    val activePlayer: SessionId?,
    val amountToCall: Int,
    val actionCenterCallbacks: ActionCenterCallbacks,
    val onHandReorder: (newOrder: List<Int>) -> Unit,
    val recentAction: Pair<RoundAction, SessionId>?
) : RProps

val GameProps.myself get() = table.myself.playerInfo
val GameProps.mySessionId get() = myself.sessionId

fun RBuilder.game(props: GameProps) = child(Game, props) {}

private val Game = functionalComponentEx<GameProps>("Game") { props ->

    div("uk-background-cover uk-background-center-center") {
        attrsApplyStyle { backgroundImage = "url(table.svg)" }
        htmlAttrs["uk-height-viewport"] = ""

        actionCenter(
            props.myself.money.value,
            props.amountToCall,
            enableActivePlayerControls = props.mySessionId == props.activePlayer,
            youWin = props.mySessionId in props.table.winners,
            callbacks = props.actionCenterCallbacks
        )

        div("uk-position-center poker-pot-container") {
            chipsDisplay(props.table.pot, 1.0)
        }

        myselfComponent(props.table.myself, props.onHandReorder)

        otherPlayers(props.table.otherPlayers, props.table.winners, props.activePlayer, props.recentAction?.swap())

    }

}
