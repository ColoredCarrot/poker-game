package usingreact

import kotlinx.html.InputType
import kotlinx.html.js.onFocusFunction
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.RProps
import react.RPureComponent
import react.RState
import react.dom.div
import react.dom.input
import react.dom.span
import shared.htmlAttrs

fun RBuilder.valueDisplay(value: String, autoFocus: Boolean = false) = child(ValueDisplay::class) {
    attrs {
        this.value = value
        this.autoFocus = autoFocus
    }
}

private external interface ValueDisplayProps : RProps {
    var value: String
    var autoFocus: Boolean
}

private class ValueDisplayState : RState {
    var copied = false
}

private class ValueDisplay : RPureComponent<ValueDisplayProps, ValueDisplayState>() {

    override fun RBuilder.render() {
        div("uk-inline uk-width-1-1") {
            span("uk-form-icon uk-form-icon-flip") {
                htmlAttrs["uk-icon"] = "icon: copy"
            }
            input(classes = "uk-input", type = InputType.text) {
                attrs.value = props.value
                attrs.autoFocus = props.autoFocus
                attrs.readonly = true
                attrs.onFocusFunction = { evt ->
                    val el = evt.currentTarget as? HTMLInputElement
                    el?.select()
                }
            }
        }
    }

}
