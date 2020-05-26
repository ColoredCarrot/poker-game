package participant

import comm.Participant
import comm.msg.Messages
import comm.msg.handledBy
import kotlinext.js.jsObject
import react.RBuilder
import react.RProps
import react.child
import react.dom.h1
import react.dom.p
import react.useEffectWithCleanup
import reactutils.functionalComponentEx
import shared.SessionId
import shared.Table
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

        lobbyContainer {
            h1 { +"Join Game" }



            p { +"Waiting for game to start..." }
        }
    }
