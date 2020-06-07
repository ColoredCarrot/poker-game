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

package usingreact

import react.RBuilder
import react.RElementBuilder
import react.RProps
import react.child
import react.dom.a
import react.dom.div
import react.dom.span
import reactutils.functionalComponentEx
import shared.attrsApplyStyle
import shared.htmlAttrs
import kotlin.js.Date

fun RBuilder.lobbyContainer(handler: RElementBuilder<RProps>.() -> Unit) =
    child(LobbyContainer, handler = handler)

private val LobbyContainer = functionalComponentEx<RProps>("LobbyContainer") { props ->
    div("poker-lobby-bg") {
        div("poker-lobby-main uk-container") {
            htmlAttrs["uk-height-viewport"] = "expand: true"

            props.children()

            // Footer
            div("uk-position-bottom uk-section uk-section-small") {
                div("uk-container") {
                    div("uk-panel uk-text-muted uk-text-small") {
                        span("uk-align-left") {
                            attrsApplyStyle { cursor = "default" }
                            +"Poker Game © Julian Koch ${Date().getFullYear()}"
                        }
                        span("uk-align-right") {
                            a(classes = "uk-link-reset uk-button-text", href = "https://github.com/ColoredCarrot/poker-game") {
                                +"https://github.com/ColoredCarrot/poker-game"
                            }
                        }
                    }
                }
            }
        }
    }
}
