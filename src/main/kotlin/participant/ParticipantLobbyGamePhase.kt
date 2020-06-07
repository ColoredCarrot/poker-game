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

package participant

import comm.Participant
import comm.msg.Messages
import comm.msg.handledBy
import comm.msg.send
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.dom.h1
import react.dom.span
import react.dom.strong
import react.setState
import shared.SessionId
import shared.Table
import shared.childEx
import shared.htmlAttrs
import usingreact.lobbyContainer
import usingreact.lobbyPlayerList

fun RBuilder.participantLobbyGamePhase(
    connection: Participant,
    switchToPlayingPhaseFn: (initialTable: Table, firstAnte: Int, activePlayer: SessionId?) -> Unit
) =
    childEx(ParticipantLobbyGamePhase::class) {
        this.connection = connection
        this.switchToPlayingPhaseFn = switchToPlayingPhaseFn
    }

private external interface ParticipantLobbyGamePhaseProps : RProps {
    var connection: Participant
    var switchToPlayingPhaseFn: (initialTable: Table, firstAnte: Int, activePlayer: SessionId?) -> Unit
}

private class ParticipantLobbyGamePhaseState : RState {
    var connectedToHost = false
    var error: dynamic = null
    var myPlayerName = ""
    var myPlayerNameValidated = false
    var otherPlayerNames = listOf<String>()
}

private class ParticipantLobbyGamePhase : RComponent<ParticipantLobbyGamePhaseProps, ParticipantLobbyGamePhaseState>() {

    init {
        state = ParticipantLobbyGamePhaseState()
    }

    override fun componentDidMount() {
        props.connection.receive(
            Messages.Lobby_UpdatePlayerList.Type handledBy { (m) ->
                setState {
                    otherPlayerNames = m.allNames.filter { it != myPlayerName }
                    myPlayerNameValidated = myPlayerName in m.allNames
                }
            },
            Messages.TotalGameReset.Type handledBy { (m) ->
                props.switchToPlayingPhaseFn(m.yourTable, m.ante, m.activePlayer)
            }
        )

        props.connection.hook.connectedToPeer {
            setState { connectedToHost = true }
        }
        props.connection.hook.errorConnectingToPeer { err -> setState { error = err } }
        props.connection.hook.error { err -> setState { error = err } }
    }

    override fun componentWillUnmount() {
        props.connection.receive()
        props.connection.hook.clear()
    }

    override fun componentDidUpdate(
        prevProps: ParticipantLobbyGamePhaseProps,
        prevState: ParticipantLobbyGamePhaseState,
        snapshot: Any
    ) {
        // Send update if we changed our name, and invalidate until we receive confirmation from server
        if (prevState.myPlayerName != state.myPlayerName) {
            setState { myPlayerNameValidated = false }
            props.connection.send(Messages.Lobby_SetName(state.myPlayerName, props.connection.peerId))
        }
    }

    override fun RBuilder.render() {
        lobbyContainer {
            h1 { +"Join Game" }

            if (state.error != null) div("poker-lobby-error") {
                strong { +"${state.error}" }
            } else div {
                +"Status: "

                if (state.connectedToHost) {
                    span("uk-text-success") { +"Connected. " }
                    +"Waiting for host to start game..."
                    lobbyPlayerList(
                        state.otherPlayerNames,
                        state.myPlayerName,
                        state.myPlayerNameValidated
                    ) { newName ->
                        setState { myPlayerName = newName }
                    }
                } else {
                    +"Connecting..."
                    div("poker-lobby-spinner") {
                        htmlAttrs["uk-spinner"] = ""
                    }
                }
            }
        }
    }
}
