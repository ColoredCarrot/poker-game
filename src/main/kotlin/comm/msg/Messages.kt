package comm.msg

import kotlinx.serialization.Serializable
import shared.Hand
import shared.RoundAction
import shared.SessionId
import shared.Table

object Messages : MessageTypeRegistry(
    "PerformRoundAction" to PerformRoundAction.Type,
    "TotalGameReset" to TotalGameReset.Type,
    "GameStartWithAnte" to GameStartWithAnte.Type,
    "SetHands" to SetHands.Type,
    "UpdateRound" to UpdateRound.Type,
    "GameFinish" to GameFinish.Type
) {

    @Serializable
    data class PerformRoundAction(val actor: SessionId, val action: RoundAction) : JsonMessageToken<PerformRoundAction> {
        override val jsonType get() = Type

        object Type : JsonMessage.Type<PerformRoundAction>(serializer())
    }

    //  Participant to Host
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  Host to Participant

    @Serializable
    data class TotalGameReset(val yourTable: Table, val ante: Int, val activePlayer: SessionId?) : JsonMessageToken<TotalGameReset> {
        override val jsonType get() = Type

        object Type : JsonMessage.Type<TotalGameReset>(serializer())
    }

    @Serializable
    data class GameStartWithAnte(val ante: Int) : JsonMessageToken<GameStartWithAnte> {
        override val jsonType get() = Type

        object Type : JsonMessage.Type<GameStartWithAnte>(serializer())
    }

    @Serializable
    data class SetHands(val newHands: Map<SessionId, Hand>) : JsonMessageToken<SetHands> {
        override val jsonType get() = Type

        object Type : JsonMessage.Type<SetHands>(serializer())
    }

    @Serializable
    data class UpdateRound(
        val amountToCall: Int,
        val activePlayer: SessionId,
        val updatedMoneyPlayer: SessionId,
        val updatedMoneyValue: Int,
        val pot: Int,
        val reason: Pair<SessionId, RoundAction>,
        val isNextRound: Boolean
    ) : JsonMessageToken<UpdateRound> {
        override val jsonType get() = Type

        object Type : JsonMessage.Type<UpdateRound>(serializer())
    }

    @Serializable
    data class GameFinish(val winners: List<SessionId>) : JsonMessageToken<GameFinish> {
        override val jsonType get() = Type

        object Type : JsonMessage.Type<GameFinish>(serializer())
    }

}
