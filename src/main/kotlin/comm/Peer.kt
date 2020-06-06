package comm

import comm.msg.Message
import comm.msg.MessageHandler
import comm.msg.MessageTypeRegistry
import comm.msg.Messenger
import kotlinext.js.jsObject
import shared.Notify
import shared.PNotify
import shared.SessionId
import toImplicitBoolean

/**
 * Abstracts the usage of PeerJS
 */
open class Peer : Messenger<SessionId> {

    private val peer = PeerJS.createPeer()
    private val remotes = LinkedHashMap<String, dynamic>(1)

    val peerIdOrNull get() = peer.id as? String
    val peerId get() = peerIdOrNull ?: throw ConnectionClosedException()
    val remotesCount get() = remotes.size
    fun peers() = (remotes as Map<String, dynamic>).keys

    var connectionAcceptor: (theirId: String) -> Boolean = { false }


    init {
        // Connect to server callback
        peer.on("open") { id: String ->
            log("Connected to server; assigned ID: $id")
            hook.open.notify(id)
        }

        peer.on("disconnected") {
            // Disconnected from server
            // Could reconnect using myself.reconnect()
            log("Disconnected from server")
            hook.close.notify { peer.reconnect(); Unit }
        }

        peer.on("connection") { c: DataConnection ->
            if (connectionAcceptor(c.peer)) {
                log("accepted connection from ${c.peer}")
                remotes[c.peer] = c
                ready(c)
            } else {
                log("rejected connection from ${c.peer}")
                c.send("Connection rejected")
                c.close()
            }
        }

        peer.on("close") {
            // Peer destroyed
            remotes.clear()
            log("peer destroyed")
        }

        peer.on("error") { err: dynamic ->
            log("peer error: ", err)
            hook.error.notify(err)
        }
    }

    fun connect(theirId: String) {
        @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "RemoveExplicitTypeArguments")
        val them = peer.connect(theirId, jsObject<dynamic> { reliable = true }) as DataConnection
        remotes[theirId] = them

        them.on("open") {
            log("connected to ${them.peer}")
            hook.connected.notify()
        }
        them.on("error") { err: dynamic ->
            log("error connecting to $theirId: ", err)
            hook.errConnecting.notify(err)
        }

        ready(them)
    }

    /**
     * Triggered once a connection has been achieved.
     * Defines callbacks to handle incoming data and connection events.
     */
    private fun ready(remote: DataConnection) {
        val remoteId = remote.peer
        remote.on("data", fun(data: dynamic) {
            log("recv($remoteId): ", data)
            messageReceive(data as String)
        })
        remote.on("close", fun() {
            // Connection to remote closed
            log("disconnected from $remoteId")
            hook.disconnected.notify(remoteId)
            remotes.remove(remoteId)
            //js("start(true)") // TODO: what's this
        })
    }


    //<editor-fold desc="Messaging">
    fun send(data: String) = send { data }

    fun send(dataCreator: (peer: String) -> String?) {
        for ((peer, remote) in remotes) {
            if (toImplicitBoolean(remote) && toImplicitBoolean(remote.open)) {
                val data = dataCreator(peer) ?: continue
                remote.send(data)
                log("send($peer): ", data)
            } else {
                // TODO only warn if the remote object is still in remotes after a second or so
                console.warn("Skipping sending message to $peer because the connection is closed.")
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

    @Suppress("UNCHECKED_CAST")
    private fun <M, X> handleMessage(msg: M, handler: (X) -> Unit) {
        handler(msg as X)
    }
    //</editor-fold>

    private fun log(msg: String, vararg more: dynamic) {
        console.log("[Peer] $msg", *more)
    }


    val hook = Hook()

    class Hook {
        internal val open = PNotify<String>()
        internal val close = PNotify<() -> Unit>()
        internal val error = PNotify<dynamic>()
        internal val connected = Notify()
        internal val disconnected = PNotify<SessionId>()
        internal val errConnecting = PNotify<dynamic>()

        /**
         * Called when the connection to the peer server is opened.
         *
         * The argument is our peer ID.
         */
        fun open(handler: (peerId: String) -> Unit) = open.handle(handler)

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
        fun close(handler: (reconnectFn: () -> Unit) -> Unit) = close.handle(handler)

        fun error(handler: (err: dynamic) -> Unit) = error.handle(handler)

        fun connectedToPeer(handler: () -> Unit) = connected.handle(handler)

        fun disconnectedFromPeer(handler: (peer: SessionId) -> Unit) = disconnected.handle(handler)

        fun errorConnectingToPeer(handler: (err: dynamic) -> Unit) = errConnecting.handle(handler)

        fun clear() {
            open {}
            close {}
            error {}
            connectedToPeer {}
            disconnectedFromPeer {}
            errorConnectingToPeer {}
        }
    }
}

private external interface DataConnection {
    val peer: String
    fun send(data: dynamic)
    fun on(event: String, callback: dynamic)

    /** Closes the data connection gracefully, cleaning up underlying DataChannels and PeerConnections. */
    fun close()
}
