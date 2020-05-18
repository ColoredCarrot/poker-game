@file:JsModule("react")
@file:JsNonModule

package vendor

import react.RMutableRef
import react.RReadableRef

external fun <T> useRef(initialValue: T): RMutableRef<T>

// Refs (16.3+)
external fun <T> createRef(): RReadableRef<T>
