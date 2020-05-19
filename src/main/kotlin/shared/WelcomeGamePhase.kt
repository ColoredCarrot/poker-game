package shared

import kotlinext.js.jsObject
import kotlinx.html.InputType
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onKeyDownFunction
import kotlinx.html.js.onKeyPressFunction
import kotlinx.html.js.onKeyUpFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import react.RBuilder
import react.RProps
import react.child
import react.dom.a
import react.dom.div
import react.dom.input
import react.dom.label
import react.dom.p
import react.functionalComponent

fun RBuilder.welcomeGamePhase(enterGameFn: (String) -> Unit, hostGameFn: () -> Unit) =
    child(WelcomeGamePhase, jsObject {
        this.enterGameFn = enterGameFn
        this.hostGameFn = hostGameFn
    })

private external interface WelcomeGamePhaseProps : RProps {
    var enterGameFn: (String) -> Unit
    var hostGameFn: () -> Unit
}

private val WelcomeGamePhase = functionalComponent<WelcomeGamePhaseProps> { props ->
    div {
        p {
            +"Enter a game ID and press Enter to join a game."
            +"Alternatively, you can also "
            a(href = "#") {
                +"host your own game"
                attrs.onClickFunction = { evt ->
                    evt.preventDefault()
                    props.hostGameFn()
                }
            }
            +"."
        }

        label {
            +"Game ID:"
            input(type = InputType.text) {
                attrs.autoComplete = false
                attrs.onKeyUpFunction = { evt ->
                    if (evt.asDynamic().key as String == "Enter") {
                        evt.preventDefault()
                        props.enterGameFn((evt.currentTarget as HTMLInputElement).value)
                    }
                }
            }
        }
    }
}
