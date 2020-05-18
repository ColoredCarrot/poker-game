package usingreact

import react.RBuilder
import react.RProps
import react.child
import react.dom.div
import react.functionalComponent
import shared.PrivateGameState

fun RBuilder.myselfComponent(
    myself: PrivateGameState,
    onHandReorder: (newOrder: List<Int>) -> Unit
) = child(MyselfComponent) {
    attrs {
        this.myself = myself
        this.onHandReorder = onHandReorder
    }
}

private external interface MyselfComponentProps : RProps {
    var myself: PrivateGameState
    var onHandReorder: (newOrder: List<Int>) -> Unit
}

private val MyselfComponent = functionalComponent<MyselfComponentProps> { props ->

    div("uk-position-bottom-center poker-my-hand-container") {
        chipsDisplay(props.myself.playerInfo.money, MY_MONEY_RENDER_SIZE_MOD)
        handDisplay(props.myself.playerInfo.hand!!, true, props.onHandReorder)
        //TODO onDblClick, explain hand in modal
    }

}

private const val MY_MONEY_RENDER_SIZE_MOD = 0.7
