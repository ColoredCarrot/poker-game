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

package reactutils

import react.RBuilder
import react.RProps
import react.functionalComponent

external interface FunctionalComponentInterface {

    var displayName: String

}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
inline fun <P : RProps> functionalComponentEx(
    settings: FunctionalComponentInterface.() -> Unit = {},
    noinline func: RBuilder.(props: P) -> Unit
) = functionalComponent(func).also { settings(it.asDynamic() as FunctionalComponentInterface) }

fun <P : RProps> functionalComponentEx(displayName: String, func: RBuilder.(props: P) -> Unit) =
    functionalComponentEx({ this.displayName = displayName }, func)
