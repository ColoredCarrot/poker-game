package usingreact

import kotlinx.html.InputType
import kotlinx.html.js.onFocusFunction
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.RProps
import react.RPureComponent
import react.RState
import react.dom.input

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
