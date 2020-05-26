package comm

import kotlinext.js.jsObject

object PeerJS {

    private val LOCALHOST_INIT = jsObject<dynamic> {
        debug = 2
        host = "localhost"
        port = 9000
        path = "/peer"
    }

    private val PEERJS_CLOUD_INIT = jsObject<dynamic> {
        debug = 2
    }

    private var init = PEERJS_CLOUD_INIT

    fun createPeer(): dynamic {
        @Suppress("UNUSED_VARIABLE")
        val init = init
        return js("new Peer(null, init)")
    }

    /////////////////////////////////////////////////////////
    ////       These methods may be invoked as so:       ////
    ////   window['poker-game'].comm.PeerJS.setCustom*   ////
    /////////////////////////////////////////////////////////
    @JsName("setCustomHost")
    fun setCustomHost(host: String) {
        init.host = host
    }

    @JsName("setCustomPort")
    fun setCustomPort(port: Int) {
        init.port = port
    }

    @JsName("setCustomPath")
    fun setCustomPath(path: String) {
        init.path = path
    }

}
