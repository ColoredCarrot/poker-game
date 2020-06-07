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

package vendor

object Swal {

    fun fire(options: dynamic) = js("window.Swal.fire(options)")

    fun fire(options: Options) = js("window.Swal.fire(options)")

    data class Options(
        val title: String = "",
        val text: String = "",
        val html: dynamic = "",
        val timer: Number? = null,
        val onBeforeOpen: dynamic = undefined,

        val allowEscapeKey: Boolean = true,
        /** If set to false, the user can't confirm the modal by pressing the Enter or Space keys, unless they manually focus the confirm button. You can also pass a custom function returning a boolean value. */
        val allowEnterKey: Boolean = true,
        /** If set to false, a "Confirm"-button will not be shown. It can be useful when you're using custom HTML description. */
        val showConfirmButton: Boolean = true,
        /** If set to true, a "Cancel"-button will be shown, which the user can click on to dismiss the modal. */
        val showCancelButton: Boolean = false,

        val confirmButtonText: String = "OK",
        val cancelButtonText: String = "Cancel",
        val confirmButtonColor: String = "#3085d6",
        val cancelButtonColor: String = "#aaa"
    ) {

        fun fire() = fire(this)
    }

}
