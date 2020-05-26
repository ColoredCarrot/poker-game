package comm

import kotlinext.js.jsObject

object PeerJS {

    fun createPeer(): dynamic {
        @Suppress("UNUSED_VARIABLE")
        val init = PEERJS_CLOUD_INIT
        return js("new Peer(null, init)")
    }

    private val LOCALHOST_INIT = jsObject<dynamic> {
        debug = 2
        host = "localhost"
        port = 9000
        path = "/peer"
    }

    private val PEERJS_CLOUD_INIT = jsObject<dynamic> {
        debug = 2
    }
}
