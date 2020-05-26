package shared

import kotlinext.js.jsObject
import kotlinx.html.InputType
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.RProps
import react.child
import react.dom.button
import react.dom.div
import react.dom.form
import react.dom.h1
import react.dom.input
import reactutils.functionalComponentEx
import usingreact.lobbyContainer
import vendor.useRef

fun RBuilder.welcomeGamePhase(enterGameFn: (String) -> Unit, hostGameFn: () -> Unit) =
    child(WelcomeGamePhase, jsObject {
        this.enterGameFn = enterGameFn
        this.hostGameFn = hostGameFn
    })

private external interface WelcomeGamePhaseProps : RProps {
    var enterGameFn: (String) -> Unit
    var hostGameFn: () -> Unit
}

private val WelcomeGamePhase = functionalComponentEx<WelcomeGamePhaseProps>("WelcomeGamePhase") { props ->

    val joinGameInputRef = useRef<HTMLInputElement?>(null)

    lobbyContainer {
        h1 { +"Poker" }

        div("uk-container") {
            htmlAttrs["uk-grid"] = ""

            // Divide container into two columns
            div("uk-width-1-2") {
                +"Enter a game ID and press Enter to join a game."

                form(classes = "poker-lobby-form uk-form-stacked") {
                    attrs.onSubmitFunction = { evt ->
                        evt.preventDefault()
                        props.enterGameFn(joinGameInputRef.current!!.value)
                    }

                    div("poker-lobby-info-label") { +"Game ID:" }
                    div("uk-form-controls") {
                        input(classes = "uk-input", type = InputType.text) {
                            attrs.autoComplete = false
                            ref = joinGameInputRef
                        }
                    }
                    //TODO input type=submit
                }
            } // first column
            div("uk-width-1-2") {
                div { +"Alternatively, you can also host your own game:" }
                button(classes = "uk-button uk-button-large uk-button-secondary") {
                    +"Host your own game"
                    attrs.onClickFunction = { evt ->
                        evt.preventDefault()
                        props.hostGameFn()
                    }
                }
            } // second column
        } // two-split container
    }
}
