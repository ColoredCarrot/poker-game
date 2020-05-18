package render

import kotlinx.html.dom.create
import shared.Chips
import shared.Notify
import vendor.Swal
import kotlin.browser.document

class AnteUpRenderer(private val ante: Int) : Renderable {

    override fun render() {

        val addedToDOM = Notify()
        val modalContent = Chips(ante).legacyrender(document.create, addedToDOM = addedToDOM).innerHTML

        Swal.Options(
            title = "Ante Up!",
            html = modalContent,
            onBeforeOpen = { modal: dynamic ->
                addedToDOM.notify()
                Unit
            },
            timer = 3000,
            showConfirmButton = false
        ).fire()
    }

}
