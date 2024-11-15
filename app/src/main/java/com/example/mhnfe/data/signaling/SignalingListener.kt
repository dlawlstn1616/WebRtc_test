package com.example.mhnfe.data.signaling

import android.util.Base64
import android.util.Log
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import com.google.gson.Gson
import com.example.mhnfe.data.signaling.model.Event

abstract class SignalingListener : Signaling {

    companion object {
        private const val TAG = "CustomMessageHandler"
    }

    private val gson = Gson()

    // WebSocketListener를 생성하여 메시지를 처리하는 로직을 정의합니다.
    private val websocketListener = object : WebSocketListener() {
        override fun onMessage(webSocket: WebSocket, message: String) {
            if (message.isEmpty()) {
                return
            }

            Log.d(TAG, "Received message: $message")

            // 메시지에 "messagePayload"가 포함되어 있지 않으면 무시합니다.
            if (!message.contains("messagePayload")) {
                return
            }

            // 메시지를 Event 객체로 변환합니다.
            val evt = gson.fromJson(message, Event::class.java)

            // evt가 null이거나 messageType이 null이거나 messagePayload가 비어있으면 무시합니다.
            if (evt == null || evt.messageType == null || evt.messagePayload.isEmpty()) {
                return
            }

            // evt의 messageType에 따라 적절한 처리 로직을 수행합니다.
            when (evt.messageType.toUpperCase()) {
                "SDP_OFFER" -> {
                    Log.d(TAG, "Offer received: SenderClientId=${evt.senderClientId}")
                    Log.d(TAG, String(Base64.decode(evt.messagePayload, Base64.DEFAULT)))
                    onSdpOffer(evt)
                }
                "SDP_ANSWER" -> {
                    Log.d(TAG, "Answer received: SenderClientId=${evt.senderClientId}")
                    onSdpAnswer(evt)
                }
                "ICE_CANDIDATE" -> {
                    Log.d(TAG, "Ice Candidate received: SenderClientId=${evt.senderClientId}")
                    Log.d(TAG, String(Base64.decode(evt.messagePayload, Base64.DEFAULT)))
                    onIceCandidate(evt)
                }
            }
        }
    }

    // WebSocketListener를 반환하는 메서드
    fun getWebsocketListener(): WebSocketListener {
        return websocketListener
    }
}