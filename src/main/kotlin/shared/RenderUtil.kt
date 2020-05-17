package shared

import kotlinx.html.TagConsumer
import kotlinx.html.dom.append
import kotlinx.html.js.link
import kotlinx.html.js.script
import org.w3c.dom.HTMLElement
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.clear

typealias RenderContext = TagConsumer<HTMLElement>

object RenderUtil {

    fun renderToBody(block: RenderContext.() -> Unit) {
        document.body!!.clear()
        document.body!!.append { block() }
        UIkit.update()
    }

    fun hide(el: HTMLElement) {
        if (el.style.display != "none") {
            el.setAttribute("data-old-display", el.style.display)
            el.style.display = "none"
        }
    }

    fun show(el: HTMLElement) {
        if (el.style.display == "none") {
            el.style.display = el.getAttribute("data-old-display") ?: "block"
        }
    }

    fun fadeIn(el: HTMLElement, duration: String = "0.2s") {
        if (el.style.display == "none") {
            el.style.transition = "display"
            el.style.transitionDuration = duration
            el.style.display = el.getAttribute("data-old-display")
                ?.takeUnless { it.isBlank() }
                ?: "block"
        }
    }

    fun fadeOut(el: HTMLElement, duration: String = "0.2s") {
        if (el.style.display != "none") {
            el.style.transition = "display"
            el.style.transitionDuration = duration
            el.setAttribute("data-old-display", el.style.display)
            el.style.display = "none"
        }
    }

}
