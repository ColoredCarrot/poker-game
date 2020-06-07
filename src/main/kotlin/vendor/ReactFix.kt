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

@file:JsModule("react-dom")
@file:JsNonModule

package vendor

import org.w3c.dom.Element
import react.RMutableRef
import react.ReactElement

// See https://reactjs.org/docs/react-dom.html

external fun render(element: dynamic, container: Element?, callback: () -> Unit = definedExternally)

external fun hydrate(element: dynamic, container: Element?, callback: () -> Unit = definedExternally)

external fun unmountComponentAtNode(domContainerNode: Element?)

external fun findDOMNode(component: dynamic): Element

external fun createPortal(element: dynamic, container: Element?): ReactElement
