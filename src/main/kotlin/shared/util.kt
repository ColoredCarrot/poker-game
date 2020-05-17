package shared

import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.url.URL
import org.w3c.dom.url.URLSearchParams
import kotlin.browser.document
import kotlin.browser.window
import kotlin.properties.ReadOnlyProperty
import kotlin.random.Random
import kotlin.reflect.KProperty

@Suppress("SpellCheckingInspection")
val UIkit inline get() = window.asDynamic().UIkit

@Suppress("FunctionName", "SpellCheckingInspection")
fun UIkitUpdate() {
    UIkit.update()
}

inline fun CanvasRenderingContext2D.saveAndRestore(block: CanvasRenderingContext2D.() -> Unit) {
    save()
    try {
        block()
    } finally {
        restore()
    }
}

fun Random.nextHtmlId(prefix: String = "-", rndLen: Int = 8) =
    prefix + String(CharArray(rndLen) { ALPHANUMERICALS.random(this) })

private const val ALPHANUMERICALS = "abcdefghijklmnopqrstuvwxyz0123456789"

fun Random.nextName() = NAMES.random(this)

private val NAMES = listOf("Alice", "Bob", "Charlie", "David", "Ely", "Fabian", "Gertrud", "Hin")

inline fun <T> Iterable<T>.allButOne(predicate: (T) -> Boolean) =
    count { !predicate(it) } == 1

typealias SessionId = String

data class Offsets(
    val x: Int,
    val y: Int,
    val rightAligned: Boolean = false,
    val bottomAligned: Boolean = false
)

fun Offsets.mirrored() = listOf(
    this,
    copy(rightAligned = !rightAligned),
    copy(bottomAligned = !bottomAligned),
    copy(rightAligned = !rightAligned, bottomAligned = !bottomAligned)
)

fun Offsets.toStyleString() = buildString {
    append(if (rightAligned) "right" else "left")
    append(':').append(x).append("vw;")
    append(if (bottomAligned) "bottom" else "top")
    append(':').append(y).append("vh;")
}

@Suppress("unused")
inline fun<reified T : Element?> Document.elementById(id: String) = object : ReadOnlyProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return document.getElementById(id) as T
    }
}

class OutParam<T : Any> {
    lateinit var out: T
}

class Notify {

    private var notified = false
    private var handler: () -> Unit = {}

    fun handle(handler: () -> Unit) {
        if (notified) handler()
        else this.handler = handler
    }

    fun notify() {
        notified = true
        handler()
    }

}

fun Element.appendAttribute(qualifiedName: String, append: String, separator: String = "") {
    var value = getAttribute(qualifiedName)
    if (value.isNullOrBlank()) {
        value = append
    } else if (!value.endsWith(separator)) {
        value += separator + append
    } else {
        value += append
    }
    setAttribute(qualifiedName, value)
}

fun Element.removeElementStyle(cssName: String) {
    TODO()
}

inline fun modifyURLSearchParams(url: String, block: (URLSearchParams) -> Unit): String {
    val urlParser = URL(url)
    val params = URLSearchParams(urlParser.search)
    block(params)
    urlParser.search = params.toString()
    return urlParser.href
}
