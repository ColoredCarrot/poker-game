package render

import kotlinx.html.dom.append
import kotlinx.html.id
import kotlinx.html.js.div
import org.w3c.dom.HTMLElement
import shared.Chips
import shared.RenderContext
import kotlin.browser.document
import kotlin.dom.clear

class PotComponent(private val potChips: Chips) : Component() {

    override fun RenderContext.addStaticToDOM() {
        div("uk-position-center poker-pot-container") {
            id = ID_CONTAINER
            potChips.render(this@addStaticToDOM)
        }
    }

    override fun update() {
        (document.getElementById(ID_CONTAINER) as HTMLElement).also {
            it.clear()
            potChips.render(it.append)
        }
    }

    companion object {
        private const val ID_CONTAINER = "pot-container"
    }
}
