package comm

@Deprecated(message = "no")
class WebRTCClient {

    private var localConn: dynamic = undefined
    private var remoteConn: dynamic = undefined
    private var sendCh: dynamic = undefined
    private var receiveCh: dynamic = undefined

    /*fun connect() {

        val servers: dynamic = null
        localConn = js("new RTCPeerConnection(servers)")
        remoteConn = js("new RTCPeerConnection(servers)")
        val sendCh = localConn.createDataChannel("sendDataChannel")

        remoteConn.ondatachannel = { evt: dynamic ->

        }

    }*/

    fun createConnection() {

        // Used in js("...") statements
        @Suppress("UNUSED_VARIABLE") val servers = null

        localConn = js("new RTCPeerConnection(servers)")
        println("Created local peer connection object localConnection")

        sendCh = localConn.createDataChannel("sendDataChannel")
        println("Created send data channel")

        localConn.onicecandidate = { e: dynamic ->
            onIceCandidate(localConn, e)
        }
        sendCh.onopen = ::onSendChannelStateChange
        sendCh.onclose = ::onSendChannelStateChange

        remoteConn = js("new RTCPeerConnection(servers)")
        println("Created remote peer connection object remoteConnection")

        remoteConn.onicecandidate = { e: dynamic ->
            onIceCandidate(remoteConn, e)
        }
        remoteConn.ondatachannel = ::receiveChannelCallback

        localConn.createOffer().then(
                ::gotDescription1,
                ::onCreateSessionDescriptionError
        )
    }

    private fun onCreateSessionDescriptionError(error: dynamic) {
        println("Failed to create session description: $error")
    }

    private fun sendData() {
        val data = "This is my test data! XXX"
        sendCh.send(data)
        println("Sent Data: $data")
    }

    fun closeDataChannels() {
        println("Closing data channels")
        sendCh.close()
        println("Closed data channel with label: " + sendCh.label.toString())
        receiveCh.close()
        println("Closed data channel with label: " + receiveCh.label.toString())
        localConn.close()
        remoteConn.close()
        localConn = null
        remoteConn = null
        println("Closed peer connections")
    }

    private fun gotDescription1(desc: dynamic) {
        localConn.setLocalDescription(desc)
        println("Offer from localConn\n${desc.sdp}")
        remoteConn.setRemoteDescription(desc)
        remoteConn.createAnswer().then(
                ::gotDescription2,
                ::onCreateSessionDescriptionError
        )
    }

    private fun gotDescription2(desc: dynamic) {
        remoteConn.setLocalDescription(desc)
        println("Answer from remoteConn\n${desc.sdp}")
        localConn.setRemoteDescription(desc)
    }

    private fun getOtherPc(pc: dynamic) = if (pc === localConn) remoteConn else localConn

    private fun getName(pc: dynamic) = if (pc === localConn) "localConn" else "remoteConn"

    private fun onIceCandidate(pc: dynamic, evt: dynamic) {
        getOtherPc(pc)
                .addIceCandidate(evt.candidate)
                .then({ onIceAddCandidateSuccess(pc) }, { err: dynamic -> onAddIceCandidateError(pc, err) })
        println("${getName(pc)} ICE candidate: ${evt.candidate?.candidate ?: "(null)"}")
    }

    private fun onIceAddCandidateSuccess(pc: dynamic) {
        println("AddIceCandidate success")
    }

    private fun onAddIceCandidateError(pc: dynamic, err: dynamic) {
        println("Failed to add Ice Candidate: $err")
    }

    private fun receiveChannelCallback(evt: dynamic) {
        println("Receive Channel Callback")
        receiveCh = evt.channel
        receiveCh.onmessage = ::onReceiveMessageCallback
        receiveCh.onopen = ::onReceiveChannelStateChange
        receiveCh.onclose = ::onReceiveChannelStateChange
    }

    private fun onReceiveMessageCallback(evt: dynamic) {
        println("Receive message: ${evt.data}")
    }

    private fun onSendChannelStateChange() {
        val readyState = sendCh.readyState
        println("Send channel state is: $readyState")
        if (readyState === "open") {

        } else {

        }
    }

    private fun onReceiveChannelStateChange() {
        val readyState = receiveCh.readyState
        println("Receive channel state is: $readyState")
    }

}
