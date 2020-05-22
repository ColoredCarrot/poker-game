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
