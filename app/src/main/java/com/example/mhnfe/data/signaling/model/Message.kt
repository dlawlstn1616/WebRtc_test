package com.example.mhnfe.data.signaling.model

import android.util.Base64
import org.webrtc.SessionDescription

data class Message(
    var action: String?,
    var recipientClientId: String?,
    var senderClientId: String?,
    var messagePayload: String?
) {
    constructor() : this(null, null, null, null)

    // 두 번째 생성자는 팩토리 메서드로 변경
    companion object {
        fun createMessage(
            action: String,
            recipientClientId: String,
            senderClientId: String,
            messagePayload: String
        ) = Message(action, recipientClientId, senderClientId, messagePayload)

        fun createAnswerMessage(
            sessionDescription: SessionDescription,
            master: Boolean,
            recipientClientId: String?
        ): Message {
            val description = sessionDescription.description
            val answerPayload = "{\"type\":\"answer\",\"sdp\":\"${description.replace("\r\n", "\\r\\n")}\"}"
            val encodedString = Base64.encodeToString(answerPayload.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)

            // SenderClientId should always be "" for master creating answer case
            return createMessage("SDP_ANSWER", recipientClientId ?: "", "", encodedString)
        }

        fun createOfferMessage(
            sessionDescription: SessionDescription,
            clientId: String
        ): Message {
            val description = sessionDescription.description
            val offerPayload = "{\"type\":\"offer\",\"sdp\":\"${description.replace("\r\n", "\\r\\n")}\"}"
            val encodedString = Base64.encodeToString(offerPayload.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)

            return createMessage("SDP_OFFER", "", clientId, encodedString)
        }
    }
}