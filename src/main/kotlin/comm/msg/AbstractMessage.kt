package comm.msg

abstract class AbstractMessage<M : AbstractMessage<M>>(override val type: MessageType<M>) : Message<M>
