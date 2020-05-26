package participant

import comm.msg.Messages
import comm.msg.Messenger
import comm.msg.handledBy
import kotlinext.js.jsObject
import react.RBuilder
import react.RProps
import react.child
import react.dom.div
import react.dom.p
import react.useEffectWithCleanup
import reactutils.functionalComponentEx
import shared.SessionId
import shared.Table
import shared.htmlAttrs

fun RBuilder.participantLobbyGamePhase(
    connection: Messenger<SessionId>,
    switchToPlayingPhaseFn: (initialTable: Table, firstAnte: Int, activePlayer: SessionId?) -> Unit
) =
    child(ParticipantLobbyGamePhase, jsObject {
        this.connection = connection
        this.switchToPlayingPhaseFn = switchToPlayingPhaseFn
    })

private external interface ParticipantLobbyGamePhaseProps : RProps {
    var connection: Messenger<SessionId>
    var switchToPlayingPhaseFn: (initialTable: Table, firstAnte: Int, activePlayer: SessionId?) -> Unit
}

private val ParticipantLobbyGamePhase =
    functionalComponentEx<ParticipantLobbyGamePhaseProps>("ParticipantLobbyGamePhase") { props ->

        useEffectWithCleanup(listOf()) {
            props.connection.receive(
                Messages.TotalGameReset.Type handledBy { (m) ->
                    props.switchToPlayingPhaseFn(m.yourTable, m.ante, m.activePlayer)
                }
            )

            return@useEffectWithCleanup {
                props.connection.receive()
            }
        }

        div("uk-container primary-container") {
            htmlAttrs["uk-height-viewport"] = ""

            p { +"Waiting for game to start..." }
        }
    }
