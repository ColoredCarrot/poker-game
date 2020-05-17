package comm

import comm.msg.Message
import comm.msg.MessageHandler
import comm.msg.MessageTypeRegistry
import comm.msg.Messenger
import shared.SessionId
import toImplicitBoolean

// Opposite of Host
class Participant : Messenger<SessionId> {

    private var myself = js("new Peer(null, { debug: 2 })")
    private var host: dynamic = null
    private var hostId: SessionId? = null

    var onRecvMsg: ((String) -> Unit)? = null

    var myPeerId: String? = null
        private set

    init {

        myself.on("open") { _ ->
            // We also get our own ID, but we don't care about it since we're not the host
            // to whom all players connect

            println("ID: " + myself.id)
            myPeerId = myself.id as String
            Unit
        }

        myself.on("disconnected") {
            println("Connection lost. Please reconnect")
        }

        myself.on("close") {
            host = null
            hostId = null
            myPeerId = null
            println("Connection destroyed");
        }

        myself.on("error", fun(err: dynamic) {
            println("error: $err")
        })

    }

    fun connect(hostId: String) {
        // Close current connection
        if (host) {
            host.close()
        }

        host = myself.connect(hostId, js("{ reliable: true }"))
        this.hostId = hostId

        host.on("open") {
            println("Connected to: ${host.peer}")
        }

        host.on("data") { data: dynamic ->
            println("Incoming message: $data")
            onRecvMsg?.invoke("$data")
            messageReceive("$data")
        }

        host.on("close") {
            println("Connection closed")
        }
    }

    fun send(data: String) {
        if (host && toImplicitBoolean(host.open)) {
            host.send(data)
            println("Sent: $data")
        } else {
            throw ConnectionClosedException("Cannot send because connection is closed: $data")
        }
    }

    private val messageTypeRegistry = MessageTypeRegistry()
    private var messageReceive: (String) -> Unit = {}

    override fun hookMessageTypes(mtRegistry: MessageTypeRegistry) {
        messageTypeRegistry.merge(mtRegistry)
    }

    override fun <M : Message<M>> send(message: M) {
        send(messageTypeRegistry.encode(message))
    }

    override fun <M : Message<M>> sendDynamic(message: (SessionId) -> M?) {
        val m = message(hostId!!) ?: return
        send(m)
    }

    override fun receive(vararg handlers: MessageHandler<*>) {
        val map = handlers.associateBy { (mt, _) -> mt }
        messageReceive = { raw ->
            val msg = messageTypeRegistry.decode(raw)
            map[msg.type]?.also { (_, handler) ->
                handleMessage(msg, handler)
            } ?: console.error("Unhandled message: $raw")
        }
    }

    private fun <M, X> handleMessage(msg: M, handler: (X) -> Unit) {
        handler(msg as X)
    }
}
