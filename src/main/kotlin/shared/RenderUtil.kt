/*
 * Copyright 2020 Julian Koch and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
