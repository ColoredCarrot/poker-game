package comm.msg

interface Messenger<S> {

    fun hookMessageTypes(mtRegistry: MessageTypeRegistry)

    fun <M : Message<M>> send(message: M)

    fun <M : Message<M>> sendDynamic(message: (S) -> M?)

    fun receive(vararg handlers: MessageHandler<*>)

}

data class MessageHandler<M : Message<M>>(
    val type: MessageType<M>,
    val handler: (M) -> Unit
)

infix fun <M : Message<M>> MessageType<M>.handledBy(handler: (M) -> Unit) = MessageHandler(this, handler)
