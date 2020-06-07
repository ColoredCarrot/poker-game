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

import kotlinext.js.jsObject
import kotlinx.html.InputType
import kotlinx.html.classes
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.RProps
import react.child
import react.dom.div
import react.dom.h4
import react.dom.input
import react.dom.li
import react.dom.ul
import reactutils.functionalComponentEx
import shared.attrsApplyStyle

fun RBuilder.lobbyPlayerList(otherPlayers: List<String>, ownName: String, ownNameValidated: Boolean, setOwnNameFn: (String) -> Unit) =
    child(LobbyPlayerList, jsObject {
        this.ownName = ownName
        this.ownNameValidated = ownNameValidated
        this.setOwnNameFn = setOwnNameFn
        this.otherPlayers = otherPlayers
    })

private external interface LobbyPlayerListProps : RProps {
    var ownName: String
    var ownNameValidated: Boolean
    var setOwnNameFn: (String) -> Unit
    var otherPlayers: List<String>
}

private val LobbyPlayerList = functionalComponentEx<LobbyPlayerListProps>("LobbyPlayerList") { props ->
    div {
        h4 {
            attrsApplyStyle { marginTop = 34; marginBottom = 0 }
            +"Players"
        }
        ul("uk-list uk-list-hyphen") {
            attrsApplyStyle { marginTop = 4 }

            li {
                input(type = InputType.text, classes = "uk-input uk-form-blank uk-form-width-medium") {
                    if (props.ownNameValidated) {
                        attrs.classes += "uk-form-success"
                    }
                    attrs.placeholder = "Enter your name..."
                    attrs.value = props.ownName
                    attrs.onChangeFunction = { evt ->
                        val value = (evt.currentTarget as HTMLInputElement).value
                        props.setOwnNameFn(value)
                    }
                }
                attrsApplyStyle { marginBottom = 8 }
            }

            for (otherPlayer in props.otherPlayers) {
                li {
                    attrsApplyStyle { paddingLeft = 10; height = 40; marginTop = 0 }
                    +otherPlayer
                }
            }
        }
    }
}
