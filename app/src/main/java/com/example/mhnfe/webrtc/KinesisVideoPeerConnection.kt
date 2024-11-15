package com.example.mhnfe.webrtc

import android.util.Log
import org.webrtc.CandidatePairChangeEvent
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RtpReceiver

/**
 * Listener for Peer connection events. Prints event info to the logs at debug level.
 */
open class KinesisVideoPeerConnection : PeerConnection.Observer {

    companion object {
        private const val TAG = "KVSPeerConnection"
    }

    /**
     * Triggered when the SignalingState changes.
     */
    override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {
        Log.d(TAG, "onSignalingChange(): signalingState = [$signalingState]")
    }

    /**
     * Triggered when the IceConnectionState changes.
     */
    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
        Log.d(TAG, "onIceConnectionChange(): iceConnectionState = [$iceConnectionState]")
    }

    /**
     * Triggered when the ICE connection receiving status changes.
     */
    override fun onIceConnectionReceivingChange(connectionChange: Boolean) {
        Log.d(TAG, "onIceConnectionReceivingChange(): connectionChange = [$connectionChange]")
    }

    /**
     * Triggered when the IceGatheringState changes.
     */
    override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {
        Log.d(TAG, "onIceGatheringChange(): iceGatheringState = [$iceGatheringState]")
    }

    /**
     * Triggered when a new ICE candidate has been found.
     */
    override fun onIceCandidate(iceCandidate: IceCandidate) {
        Log.d(TAG, "onIceCandidate(): iceCandidate = [$iceCandidate]")
    }

    /**
     * Triggered when some ICE candidates have been removed.
     */
    override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {
        Log.d(TAG, "onIceCandidatesRemoved(): iceCandidates Length = [${iceCandidates.size}]")
    }

    /**
     * Triggered when the ICE candidate pair is changed.
     */
    override fun onSelectedCandidatePairChanged(event: CandidatePairChangeEvent) {
        val eventString = buildString {
            append("{")
            append(listOf(
                "reason: ${event.reason}",
                "remote: ${event.remote}",
                "local: ${event.local}",
                "lastReceivedMs: ${event.lastDataReceivedMs}"
            ).joinToString(", "))
            append("}")
        }
        Log.d(TAG, "onSelectedCandidatePairChanged(): event = $eventString")
    }

    /**
     * Triggered when media is received on a new stream from remote peer.
     */
    override fun onAddStream(mediaStream: MediaStream) {
        Log.d(TAG, "onAddStream(): mediaStream = [$mediaStream]")
    }

    /**
     * Triggered when a remote peer close a stream.
     */
    override fun onRemoveStream(mediaStream: MediaStream) {
        Log.d(TAG, "onRemoveStream(): mediaStream = [$mediaStream]")
    }

    /**
     * Triggered when a remote peer opens a DataChannel.
     */
    override fun onDataChannel(dataChannel: DataChannel) {
        Log.d(TAG, "onDataChannel(): dataChannel = [$dataChannel]")
    }

    /**
     * Triggered when renegotiation is necessary.
     */
    override fun onRenegotiationNeeded() {
        Log.d(TAG, "onRenegotiationNeeded():")
    }

    /**
     * Triggered when a new track is signaled by the remote peer, as a result of setRemoteDescription.
     */
    override fun onAddTrack(rtpReceiver: RtpReceiver, mediaStreams: Array<MediaStream>) {
        Log.d(TAG, "onAddTrack(): rtpReceiver = [$rtpReceiver], mediaStreams Length = [${mediaStreams.size}]")
    }
}