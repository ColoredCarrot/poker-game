package comm

import comm.msg.Message
import comm.msg.MessageHandler
import comm.msg.MessageTypeRegistry
import comm.msg.Messenger
import shared.PNotify
import shared.SessionId
import toImplicitBoolean

class Host : Messenger<SessionId> {

    private val myself = PeerJS.createPeer()

    private var remotes = LinkedHashMap<String, dynamic>(4)

    var connectionRejector: (() -> Boolean)? = null

    val remotesCount: Int get() = remotes.size

    var onRecvMsg: ((String) -> Unit)? = null

    lateinit var myPeerId: String
        private set

    fun peers() = (remotes as Map<String, dynamic>).keys

    fun iteratePeers() = (remotes as Map<String, dynamic>).keys.iterator()

    val hook = Hook()

    init {
        // Connect to server callback
        myself.on("open") { id: String ->
            println("ID: $id. Awaiting connections...")
            myPeerId = id
            hook.open.notify(id)
        }

        myself.on("connection") { c: dynamic ->
            if (connectionRejector?.invoke() == false) {
                c.send("Connection rejected")
                println("Rejected a new connection")
            } else {
                remotes[c.peer as String] = c
                println("Connected to ${c.peer}")
                ready(c)
            }
        }

        myself.on("disconnected") {
            // Disconnected from server
            // Could reconnect using myself.reconnect()
            println("Disconnected from server")
            hook.close.notify { myself.reconnect(); Unit }
        }

        myself.on("close") {
            remotes.clear()
            println("Connection destroyed")
        }

        myself.on("error") { err: dynamic ->
            console.log("Peer error: $err ", err)
            hook.error.notify(err)
        }
    }

    /**
     * Triggered once a connection has been achieved.
     * Defines callbacks to handle incoming data and connection events.
     */
    private fun ready(remote: dynamic) {
        remote.on("data", fun(data: dynamic) {
            console.log("Data received: $data")
            onRecvMsg?.invoke(data as String)
            messageReceive(data as String)
        })
        remote.on("close", fun() {
            println("Connection reset<br>Awaiting connection...")
            //remotes.remove(remote)
            js("start(true)") // TODO: what's this
        })
    }

    fun send(data: String) {
        for (remote in remotes.values) {
            if (toImplicitBoolean(remote) && toImplicitBoolean(remote.open)) {
                remote.send(data)
                println("Sent: $data")
            } else {
                throw ConnectionClosedException("Cannot send because connection is closed: $data")
            }
        }
    }

    fun send(dataCreator: (peer: String) -> String?) {
        for ((peer, remote) in remotes) {
            if (toImplicitBoolean(remote) && toImplicitBoolean(remote.open)) {
                val data = dataCreator(peer) ?: continue
                remote.send(data)
                println("Sent to $peer: $data")
            } else {
                throw ConnectionClosedException("Cannot send because connection is closed. peer=$peer remote=$remote")
            }
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
        send { sid ->
            val m = message(sid)
            m?.let { messageTypeRegistry.encode(it) }
        }
    }

    override fun receive(vararg handlers: MessageHandler<*>) {
        val map = handlers.associateBy { (mt, _) -> mt }
        messageReceive = { raw ->
            val msg = messageTypeRegistry.decode(raw)
            map[msg.type]?.also { (_, handler) ->
                handleMessage(msg, handler)
            }
        }
    }

    private fun <M, X> handleMessage(msg: M, handler: (X) -> Unit) {
        handler(msg as X)
    }

    class Hook {
        /**
         * Called when the connection to the peer server is opened.
         *
         * The argument is our peer ID.
         */
        val open = PNotify<String>()

        /**
         * Called when the connection to the peer server is closed,
         * either manually or because it was lost.
         *
         * Already established connections are maintained,
         * but new ones cannot be brokered.
         *
         * The argument is a function that, when called,
         * attempts to reconnect to the server.
         */
        val close = PNotify<() -> Unit>()

        val error = PNotify<dynamic>()

        fun clearHandlers() {
            open.handle {}
            close.handle {}
            error.handle {}
        }
    }
}
