package comm.msg

interface Message<Self : Message<Self>> {

    val type: MessageType<Self>

}
