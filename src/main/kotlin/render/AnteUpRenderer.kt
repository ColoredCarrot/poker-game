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

package render

import org.w3c.dom.HTMLElement
import usingreact.chipsDisplay
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
