package shared

import kotlinext.js.jsObject
import kotlinx.html.Tag
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.url.URL
import org.w3c.dom.url.URLSearchParams
import react.Component
import react.RBuilder
import react.RProps
import react.dom.RDOMBuilder
import react.rClass
import kotlin.browser.document
import kotlin.browser.window
import kotlin.properties.ReadOnlyProperty
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@Suppress("SpellCheckingInspection")
val UIkit
    inline get() = window.asDynamic().UIkit

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

fun Offsets.applyStyle(style: dynamic) = with(style) {
    if (rightAligned) right = "${x}vw" else left = "${x}vw"
    if (bottomAligned) bottom = "${y}vh" else top = "${y}vh"
}

@Suppress("unused")
inline fun <reified T : Element?> Document.elementById(id: String) = object : ReadOnlyProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return document.getElementById(id) as T
    }
}

class OutParam<T : Any> {
    lateinit var out: T
}

class InOutParam<T>(value: T, private val setter: (T) -> Unit) {
    var value: T = value
        set(value) {
            setter(value)
            field = value
        }
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

class PNotify<D : Any> {

    private var notified: D? = null
    private var handler: (D) -> Unit = {}

    fun handle(handler: (D) -> Unit) {
        val notified = notified
        if (notified != null) handler(notified)
        else this.handler = handler
    }

    fun notify(d: D) {
        notified = d
        handler(d)
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

val <T : Tag> RDOMBuilder<T>.htmlAttrs get() = attrs.attributes

//TODO test
inline fun RDOMBuilder<*>.attrsApplyStyle(styler: dynamic.() -> Unit) {
    @Suppress("UnsafeCastFromDynamic")
    attrs["style"] = jsObject(styler)
}

fun <A, B> Pair<A, B>.swap() = second to first

fun String.pluralize(n: Int) = if (n == 1) this else this + 's'

fun String.counted(count: Int) = "$count ${pluralize(count)}"

inline fun <P : RProps, reified C : Component<P, *>> RBuilder.childEx(props: P) =
    child(C::class.rClass, props, {})

@Suppress("RemoveExplicitTypeArguments") // actually needed or compilation error
inline fun <P : RProps, reified C : Component<P, *>> RBuilder.childEx(
    @Suppress("UNUSED_PARAMETER") klass: KClass<C>,
    propsInit: P.() -> Unit
) =
    childEx<P, C>(jsObject<P>(propsInit))
