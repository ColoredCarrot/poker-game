package shared

import kotlinext.js.jsObject
import kotlinx.html.InputType
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onKeyUpFunction
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.RProps
import react.child
import react.dom.a
import react.dom.button
import react.dom.div
import react.dom.h1
import react.dom.input
import react.dom.label
import react.dom.p
import reactutils.functionalComponentEx
import usingreact.lobbyContainer

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
    lobbyContainer {
        h1 { +"Poker" }

        div("uk-container") {
            // Divide container into two columns
            div("uk-width-1-2 uk-display-inline-block") {
                div { +"Enter a game ID and press Enter to join a game." }
                label {
                    +"Game ID:"
                    input(type = InputType.text) {
                        attrs.autoComplete = false
                        attrs.onKeyUpFunction = { evt ->
                            // Note: we cannot actually cast to KeyboardEvent
                            // because React provides its own Event with the
                            // same interface, but whose type is not KeyboardEvent.
                            if (evt.asDynamic().key as String == "Enter") {
                                evt.preventDefault()
                                props.enterGameFn((evt.currentTarget as HTMLInputElement).value)
                            }
                        }
                    }
                }
            } // first column
            div("uk-width-1-2 uk-display-inline-block") {
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
