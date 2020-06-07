/*
 * Copyright 2020 Julian Koch and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    @JsName("switchToLocalhost")
    fun switchToLocalhost() {
        init = LOCALHOST_INIT
    }

    @JsName("switchToPeerJSCloud")
    fun switchToPeerJSCloud() {
        init = PEERJS_CLOUD_INIT
    }

}
