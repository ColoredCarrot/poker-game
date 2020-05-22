package usingreact

import react.RBuilder
import react.RProps
import react.dom.div
import reactutils.functionalComponentEx
import shared.SessionId
import shared.Table
import shared.attrsApplyStyle
import shared.htmlAttrs

data class GameProps(
    val table: Table,
    val activePlayer: SessionId?,
    val amountToCall: Int,
    val actionCenterCallbacks: ActionCenterCallbacks,
    val onHandReorder: (newOrder: List<Int>) -> Unit
) : RProps

val GameProps.myself get() = table.myself.playerInfo

fun RBuilder.game(props: GameProps) = child(Game, props) {}

private val Game = functionalComponentEx<GameProps>("Game") { props ->

    div("uk-background-cover uk-background-center-center") {
        attrsApplyStyle { backgroundImage = "url(table.svg)" }
        htmlAttrs["uk-height-viewport"] = ""

        actionCenter(
            props.myself.money.value,
            props.amountToCall,
            enableActivePlayerControls = props.activePlayer == props.myself.sessionId,
            youWin = false,
            callbacks = props.actionCenterCallbacks
        )

        div("uk-position-center poker-pot-container") {
            chipsDisplay(props.table.pot, 1.0)
        }

        myselfComponent(props.table.myself, props.onHandReorder)

        //TODO otherPlayers.addToDOM(this@renderToBody)

    }

}
