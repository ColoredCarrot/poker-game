package usingreact

import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLElement
import react.RBuilder
import react.RComponent
import react.RProps
import react.RReadableRef
import react.RState
import react.dom.button
import react.dom.div
import react.dom.form
import react.dom.h2
import react.dom.input
import react.dom.p
import react.setState
import shared.UIkit
import shared.htmlAttrs

fun RBuilder.raiseModal(funds: Int, amountToCall: Int, shown: Boolean, raiseHandler: (raiseAmount: Int) -> Unit) = child(
    RaiseModal::class) {
    attrs {
        this.funds = funds
        this.amountToCall = amountToCall
        this.shown = shown
        this.commitRaiseHandler = raiseHandler
    }
}

private external interface RaiseModalProps : RProps {
    var funds: Int

    var amountToCall: Int

    var commitRaiseHandler: (raiseAmount: Int) -> Unit
    var shown: Boolean
}

private external interface RaiseModalState : RState {
    var amountToRaise: Int
}

private class RaiseModal : RComponent<RaiseModalProps, RaiseModalState>() {

    private lateinit var uiKitModalRef: RReadableRef<HTMLElement>

    override fun RaiseModalState.init() {
        amountToRaise = -1
        uiKitModalRef = vendor.createRef()
    }

    override fun RBuilder.render() {
        div {
            htmlAttrs["uk-modal"] = ""
            ref = uiKitModalRef

            div("uk-modal-dialog uk-modal-body") {
                button(classes = "uk-modal-close-default", type = ButtonType.button) {
                    htmlAttrs["uk-close"] = ""
                }
                h2("uk-modal-title") { +"Raise" }
                p { +"Increase the current betting amount" }

                form {
                    slider(
                        min = props.amountToCall + 1,
                        max = props.funds,
                        value = state.amountToRaise.takeUnless { it < 0 }
                    ) { newAmountToRaise ->
                        setState { amountToRaise = newAmountToRaise }
                    }

                    input(InputType.submit, classes = "uk-button uk-button-primary") {}

                    attrs.onSubmitFunction = { evt ->
                        evt.preventDefault()
                        submitRaise()
                    }
                }
            } // modal body
        }
    }

    override fun componentDidUpdate(prevProps: RaiseModalProps, prevState: RaiseModalState, snapshot: Any) {
        if (prevProps.shown != props.shown) {
            val uiKitModal = UIkit.modal(uiKitModalRef.current!!)
            if (props.shown) {
                uiKitModal.hide()
            } else {
                uiKitModal.show()
            }
        }
    }

    private fun submitRaise() {
        val money = props.funds
        // The raise amount is the total amount minus the amount to call
        val amount = (state.amountToRaise - props.amountToCall).coerceAtMost(money)

        if (amount >= 1) {
            props.commitRaiseHandler(amount)
        }
    }
}
