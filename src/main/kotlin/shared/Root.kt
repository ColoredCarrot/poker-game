package shared

import comm.Host
import comm.Participant
import comm.msg.Messages
import comm.msg.Messenger
import comm.msg.jsonMessage
import host.GameSettings
import host.hostLobbyGamePhase
import host.hostPlayingGamePhase
import participant.participantLobbyGamePhase
import participant.participantPlayingGamePhase
import react.RBuilder
import react.RProps
import react.child
import react.functionalComponent
import react.getValue
import react.setValue
import react.useState
import kotlin.random.Random

fun RBuilder.poker() = child(Root)

private val Root = functionalComponent<RProps> {
    var phase by useState<GamePhase> { GamePhase.Welcome }

    val switchPhaseFn = { newPhase: GamePhase -> phase = newPhase }

    with(phase) { render(switchPhaseFn) }
}

private sealed class GamePhase {

    abstract fun RBuilder.render(switchPhaseFn: (GamePhase) -> Unit)

    object Welcome : GamePhase() {
        override fun RBuilder.render(switchPhaseFn: (GamePhase) -> Unit) {
            welcomeGamePhase(
                enterGameFn = { gameId ->
                    val conn = Participant()
                    conn.hookMessageTypes(Messages)
                    conn.connect(gameId)

                    switchPhaseFn(ParticipantLobby(conn))
                },
                hostGameFn = {
                    val conn = Host()
                    conn.hookMessageTypes(Messages)

                    switchPhaseFn(HostLobby(conn))
                }
            )
        }
    }

    class ParticipantLobby(private val connection: Messenger<SessionId>) : GamePhase() {
        override fun RBuilder.render(switchPhaseFn: (GamePhase) -> Unit) {
            participantLobbyGamePhase(connection, switchToPlayingPhaseFn = { initialTable, firstAnte ->
                switchPhaseFn(ParticipantPlaying(connection, initialTable, firstAnte))
            })
        }
    }

    class ParticipantPlaying(
        private val connection: Messenger<SessionId>,
        private val initialTable: Table,
        private val firstAnte: Int
    ) : GamePhase() {
        override fun RBuilder.render(switchPhaseFn: (GamePhase) -> Unit) {
            //TODO firstAnte
            participantPlayingGamePhase(initialTable, connection)
        }
    }

    class HostLobby(private val connections: Host) : GamePhase() {
        override fun RBuilder.render(switchPhaseFn: (GamePhase) -> Unit) {
            hostLobbyGamePhase(connections, switchToPlayingPhaseFn = {

                val gameSettings = GameSettings()

                val drawCards = Card.drawCards()

                // We already draw all community cards now,
                // but we won't reveal them all until the later rounds
                val communityCards = drawCards.take(COMMUNITY_CARDS_COUNT)

                // We, the host, get the first private game state,
                // and the rest of the hands are sent to the other connected players
                val pgsBySid: Map<String, PrivateGameState> = {
                    val allSids = mutableListOf(connections.myPeerId)
                    allSids += connections.peers()
                    allSids.associateWith { sid ->
                        PrivateGameState(
                            PlayerInfo(
                                sid,
                                Profile(Random.nextName()),
                                drawCards.takeHand(2),
                                Chips(200)
                            )
                        )
                    }
                }()

                val playerInfoByPeerId = pgsBySid.mapValues { (_, pgs) -> pgs.playerInfo }

                fun Map<String, PlayerInfo>.toSetDroppingKey(key: String): LinkedHashSet<PlayerInfo> {
                    return values.filterTo(LinkedHashSet()) { it.sessionId != key }
                }

                val pot = Chips(gameSettings.ante * (connections.remotesCount + 1))

                val personalizedTables = pgsBySid.mapValues { (sid, pgs) ->
                    Table(
                        pot,
                        PlayerInfoList(playerInfoByPeerId.toSetDroppingKey(sid)),
                        pgs
                    )
                }

                switchPhaseFn(HostPlaying(
                    connections,
                    gameSettings,
                    communityCards,
                    personalizedTables[connections.myPeerId]!!
                ))

                connections.sendDynamic { sid ->
                    Messages.TotalGameReset(personalizedTables[sid]!!, gameSettings.ante).jsonMessage()
                }
            })
        }
    }

    class HostPlaying(
        private val connection: Host,
        private val settings: GameSettings,
        private val communityCards: List<Card>,
        private val initialTable: Table
    ) : GamePhase() {
        override fun RBuilder.render(switchPhaseFn: (GamePhase) -> Unit) {
            hostPlayingGamePhase(connection, settings, communityCards, initialTable)
        }
    }

}

private const val COMMUNITY_CARDS_COUNT = 5
