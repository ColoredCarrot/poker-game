package comm

import comm.msg.Message
import comm.msg.MessageHandler
import comm.msg.MessageTypeRegistry
import comm.msg.Messenger
import kotlinx.html.dom.append
import kotlinx.html.js.br
import kotlinx.html.js.p
import org.w3c.dom.url.URLSearchParams
import shared.SessionId
import shared.modifyURLSearchParams
import toImplicitBoolean
import kotlin.browser.document
import kotlin.browser.window

class Host : Messenger<SessionId> {

    private val myself = js("new Peer(null, { debug: 2 })")

    private var remotes = LinkedHashMap<String, dynamic>(4)

    var connectionRejector: (() -> Boolean)? = null

    val remotesCount: Int get() = remotes.size

    var onRecvMsg: ((String) -> Unit)? = null

    lateinit var myPeerId: String
        private set

    fun peers() = (remotes as Map<String, dynamic>).keys

    fun iteratePeers() = (remotes as Map<String, dynamic>).keys.iterator()

    init {
        // Connect to server callback
        myself.on("open") { id: dynamic ->
            println("ID: $id. Awaiting connections...")
            myPeerId = id as String
            document.body!!.append {
                br()
                p { +"Your game ID: $id" }
                br()
                // Create direct link
                val directLink = modifyURLSearchParams(window.location.href) {
                    it.append("game", id)
                }
                p { +"Direct link: $directLink" }
            }
        }

        myself.on("connection") { c: dynamic ->
            if (connectionRejector?.invoke() == false) {
                c.send("Connection rejected")
                println("Rejected a new connection")
            } else {
                //remotes[c.id as String] = c
                remotes[c.peer as String] = c
                println("Connected to ${c.peer}")
                ready(c)
            }
        }

        myself.on("disconnected") {
            println("Connection lost")
        }

        myself.on("close") {
            remotes.clear()
            println("Connection destroyed")
        }

        myself.on("error") { err: dynamic ->
            println("error: $err")
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
                throw ConnectionClosedException("Cannot send because connection is closed")
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

}
