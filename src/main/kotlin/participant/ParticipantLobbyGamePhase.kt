package participant

import comm.Participant
import comm.msg.Messages
import comm.msg.handledBy
import kotlinext.js.jsObject
import react.RBuilder
import react.RProps
import react.child
import react.dom.div
import react.dom.h1
import react.dom.span
import react.dom.strong
import react.getValue
import react.setValue
import react.useEffectWithCleanup
import react.useState
import reactutils.functionalComponentEx
import shared.SessionId
import shared.Table
import shared.htmlAttrs
import usingreact.lobbyContainer

fun RBuilder.participantLobbyGamePhase(
    connection: Participant,
    switchToPlayingPhaseFn: (initialTable: Table, firstAnte: Int, activePlayer: SessionId?) -> Unit
) =
    child(ParticipantLobbyGamePhase, jsObject {
        this.connection = connection
        this.switchToPlayingPhaseFn = switchToPlayingPhaseFn
    })

private external interface ParticipantLobbyGamePhaseProps : RProps {
    var connection: Participant
    var switchToPlayingPhaseFn: (initialTable: Table, firstAnte: Int, activePlayer: SessionId?) -> Unit
}

private val ParticipantLobbyGamePhase =
    functionalComponentEx<ParticipantLobbyGamePhaseProps>("ParticipantLobbyGamePhase") { props ->

        var connectedToHost by useState(false)
        var error by useState<dynamic>(null)

        useEffectWithCleanup(listOf()) {
            props.connection.receive(
                Messages.TotalGameReset.Type handledBy { (m) ->
                    props.switchToPlayingPhaseFn(m.yourTable, m.ante, m.activePlayer)
                }
            )

            props.connection.hook.connectedToPeer {
                connectedToHost = true
            }
            props.connection.hook.errorConnectingToPeer { error = it }
            props.connection.hook.error { error = it }

            return@useEffectWithCleanup {
                props.connection.receive()
                props.connection.hook.clear()
            }
        }

        lobbyContainer {
            h1 { +"Join Game" }

            if (error != null) div("poker-lobby-error") {
                strong { +"$error" }
            } else div {
                +"Status: "

                if (connectedToHost) {
                    span("uk-text-success") { +"Connected. " }
                    +"Waiting for host to start game..."
                } else {
                    +"Connecting..."
                    div("poker-lobby-spinner") {
                        htmlAttrs["uk-spinner"] = ""
                    }
                }
            }
        }
    }
