import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import react.dom.render
import shared.poker
import kotlin.browser.document

fun main() {
    println("main")

    document.addEventListener("DOMContentLoaded", {

        render(document.body!!.append.div()) { poker() }

        /*val autoJoinGame = URLSearchParams(window.location.search).get("game")
        if (autoJoinGame != null) {
            joinGame(autoJoinGame)
        } else {

            document.getElementById("host-a-game")!!.asDynamic().onclick = { evt: Event ->
                evt.preventDefault()
                hostGame()
            }

            document.getElementById("game-id-input")!!.addEventListener("keyup", { evt ->
                if ((evt as KeyboardEvent).key == "Enter") {
                    joinGame(js("this.value") as String)
                }
            })

        }*/
    })

}

/*fun joinGame(id: String) {
    println("Join game: $id")
    GameParticipant(id)
}

fun hostGame() {
    println("Host game")
    GameHost()
}*/

//should be noinline somehow
fun toImplicitBoolean(@Suppress("UNUSED_PARAMETER") x: dynamic) = js("!!x") as Boolean

val COMM_JSON = Json(JsonConfiguration.Stable)
