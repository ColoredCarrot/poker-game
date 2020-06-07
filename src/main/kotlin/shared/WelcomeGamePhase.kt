/*
 * Copyright 2020 Julian Koch and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package shared

import kotlinext.js.jsObject
import kotlinx.html.InputType
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.url.URLSearchParams
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
import kotlin.browser.window

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

    val autoJoinGame = URLSearchParams(window.location.search).get("game")
    if (autoJoinGame != null) {
        props.enterGameFn(autoJoinGame)
        return@functionalComponentEx
    }

    val joinGameInputRef = useRef<HTMLInputElement?>(null)

    lobbyContainer {
        h1 { +"Poker" }

        div("uk-container uk-grid-divider") {
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
                div("uk-text-center") {
                    +"Alternatively, you can also host your own game:"
                }
                button(classes = "uk-button uk-button-large uk-button-secondary uk-margin-top uk-align-center") {
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
