package comm

import kotlinext.js.jsObject

object PeerJS {

    fun createPeer(): dynamic {
        val init = jsObject<dynamic> {
            debug = 2
            host = "localhost"
            port = 9000
            path = "/peer"
        }
        return js("new Peer(null, init)")
    }

}
