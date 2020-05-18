package usingreact

import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.button
import react.dom.div
import react.dom.h3
import react.dom.h4
import react.setState
import shared.Chips

data class ActionCenterCallbacks(
    val foldFn: () -> Unit,
    val callFn: () -> Unit,
    val raiseFn: (raiseAmount: Int) -> Unit
)

fun RBuilder.actionCenter(
    funds: Int,
    amountToCall: Int,
    enableActivePlayerControls: Boolean,
    youWin: Boolean,
    callbacks: ActionCenterCallbacks
) = child(ActionCenter::class) {
    attrs {
        this.funds = funds
        this.amountToCall = amountToCall
        this.enableActivePlayerControls = enableActivePlayerControls
        this.youWin = youWin

        this.foldFn = callbacks.foldFn
        this.callFn = callbacks.callFn
        this.raiseFn = callbacks.raiseFn
    }
}

private external interface ActionCenterProps : RProps {
    var funds: Int
    var amountToCall: Int

    var foldFn: () -> Unit
    var callFn: () -> Unit
    var raiseFn: (raiseAmount: Int) -> Unit

    var enableActivePlayerControls: Boolean

    var youWin: Boolean
}

private external interface ActionCenterState : RState {
    var raiseModalShown: Boolean
}

private class ActionCenter : RComponent<ActionCenterProps, ActionCenterState>() {
    override fun ActionCenterState.init() {
        raiseModalShown = false
    }

    override fun RBuilder.render() {
        div("uk-position-top-center uk-width-1-6 uk-padding-small") {
            if (props.youWin) {
                attrs.classes += "poker-action-center-win"
            }

            h3 {
                if (props.youWin) +"You Win!"
                else +"Action Center"
            }

            div("uk-button-group") {
                button(classes = "uk-button uk-button-danger uk-button-large") {
                    +"Fold"
                    attrs.onClickFunction = { it.preventDefault(); props.foldFn() }
                    if (!props.enableActivePlayerControls) attrs.disabled = true
                }
                button(classes = "uk-button uk-button-secondary uk-button-large") {
                    +"Call"
                    attrs.onClickFunction = { it.preventDefault(); props.callFn() }
                    if (!props.enableActivePlayerControls) attrs.disabled = true
                }
                button(classes = "uk-button uk-button-primary uk-button-large") {
                    +"Raise"
                    attrs.onClickFunction = { evt ->
                        evt.preventDefault()
                        setState { raiseModalShown = true }
                    }
                    if (!props.enableActivePlayerControls) attrs.disabled = true
                }
            }

            // Chip stack of amount to call
            div {
                div("uk-margin-top") {
                    h4 { +"Amount to call" }
                }
                div("uk-width-1-1") {
                    chipsDisplay(Chips(props.amountToCall), AMT_TO_CALL_CHIPS_SIZE_MOD)
                }
            }

            raiseModal(props.funds, props.amountToCall, state.raiseModalShown) { raiseAmount ->
                setState { raiseModalShown = false }
                props.raiseFn(raiseAmount)
            }
        }
    }

    override fun componentDidUpdate(prevProps: ActionCenterProps, prevState: ActionCenterState, snapshot: Any) {
        //TODO renderWinCelebration
    }
}

private const val AMT_TO_CALL_CHIPS_SIZE_MOD = 0.6