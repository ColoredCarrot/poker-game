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
