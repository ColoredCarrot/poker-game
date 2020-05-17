package comm.msg

interface MessageType<M : Message<M>> {

    fun parse(raw: String): M

    fun stringify(m: M): String

}
