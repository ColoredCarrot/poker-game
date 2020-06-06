package usingreact

import kotlinx.html.InputType
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onFocusFunction
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.RProps
import react.RPureComponent
import react.RState
import react.dom.a
import react.dom.div
import react.dom.input
import react.setState
import shared.htmlAttrs
import vendor.createRef
import kotlin.browser.document

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

    private val inputRef = createRef<HTMLInputElement>()

    override fun RBuilder.render() {
        div("uk-inline uk-width-1-1") {
            a(classes = "uk-form-icon uk-form-icon-flip") {
                htmlAttrs["uk-icon"] = "icon: copy"
                attrs.onClickFunction = { evt ->
                    evt.preventDefault()
                    val inputEl = inputRef.current
                    if (inputEl != null) {
                        inputEl.focus()
                        inputEl.select()
                        document.execCommand("copy")
                        setState { copied = true }
                    }
                }
                if (state.copied) {
                    attrs.classes += "uk-text-success"
                }
            }
            input(classes = "uk-input", type = InputType.text) {
                ref = inputRef
                attrs {
                    value = props.value
                    autoFocus = props.autoFocus
                    readonly = true
                    onFocusFunction = { evt ->
                        val el = evt.currentTarget as? HTMLInputElement
                        el?.select()
                    }
                }
            }
        }
    }

}
