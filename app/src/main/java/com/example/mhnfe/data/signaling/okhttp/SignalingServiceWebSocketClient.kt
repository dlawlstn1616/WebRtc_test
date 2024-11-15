package com.example.mhnfe.data.signaling.okhttp

import android.util.Base64
import android.util.Log
import com.example.mhnfe.data.signaling.SignalingListener
import com.example.mhnfe.data.signaling.model.Message
import com.google.gson.Gson
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

/**
 * Signaling service client based on websocket.
 */
class SignalingServiceWebSocketClient(
    uri: String,
    signalingListener: SignalingListener,
    private val executorService: ExecutorService
) {
    private val TAG = "SignalingServiceWebSocketClient"
    private val websocketClient = WebSocketClient(uri, signalingListener)
    private val gson = Gson()

    init {
        Log.d(TAG, "Connecting to URI $uri as master")
    }

    fun isOpen(): Boolean {
        return websocketClient.isOpen()
    }

    fun sendSdpOffer(offer: Message) {
        executorService.submit {
            if (offer.action.equals("SDP_OFFER", ignoreCase = true)) {
                Log.d(TAG, "Sending Offer")
                send(offer)
            }
        }
    }

    fun sendSdpAnswer(answer: Message) {
        executorService.submit {
            if (answer.action.equals("SDP_ANSWER", ignoreCase = true)) {
                val decodedPayload = answer.messagePayload?.let {
                    String(Base64.decode(it.toByteArray(), Base64.NO_WRAP or Base64.URL_SAFE))
                } ?: "Payload is null" // 또는 적절한 기본값
                Log.d(TAG, "Answer sent $decodedPayload")
                send(answer)
            }
        }
    }

    fun sendIceCandidate(candidate: Message) {
        executorService.submit {
            if (candidate.action.equals("ICE_CANDIDATE", ignoreCase = true)) {
                send(candidate)
            }
            Log.d(TAG, "Sent Ice candidate message")
        }
    }

    fun disconnect() {
        executorService.submit { websocketClient.disconnect() }
        try {
            executorService.shutdown()
            if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                executorService.shutdownNow()
            }
        } catch (e: InterruptedException) {
            Log.e(TAG, "Error in disconnect", e)
        }
    }

    private fun send(message: Message) {
        val jsonMessage = gson.toJson(message)
        Log.d(TAG, "Sending JSON Message= $jsonMessage")
        websocketClient.send(jsonMessage)
        Log.d(TAG, "Sent JSON Message= $jsonMessage")
    }
}