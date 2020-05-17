package shared

import kotlinx.serialization.Serializable

@Serializable
sealed class RoundAction {
    @Serializable
    object Fold : RoundAction()

    @Serializable
    object Call : RoundAction()

    @Serializable
    data class Raise(val raiseAmount: Int) : RoundAction()
}
