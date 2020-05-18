package render

import org.w3c.dom.HTMLElement
import react.chipsDisplay
import shared.Chips
import vendor.Swal

class AnteUpRenderer(private val ante: Int) : Renderable {

    override fun render() {

        Swal.Options(
            title = "Ante Up!",
            html = """<div class="ante-up-modal-content-root"></div>""",
            onBeforeOpen = { modal: dynamic ->
                val root = (modal as HTMLElement).querySelector("div.ante-up-modal-content-root")
                react.dom.render(root) { chipsDisplay(Chips(ante), 1.0) }
                Unit
            },
            timer = 3000,
            showConfirmButton = false
        ).fire()
    }

}
