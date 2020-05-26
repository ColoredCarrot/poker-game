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
    })

}

//should be noinline somehow
fun toImplicitBoolean(@Suppress("UNUSED_PARAMETER") x: dynamic) = js("!!x") as Boolean

val COMM_JSON = Json(JsonConfiguration.Stable)
