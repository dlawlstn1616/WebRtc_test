package com.example.mhnfe.data.signaling.model

import android.util.Base64
import android.util.Log
import com.google.gson.JsonParser
import org.webrtc.IceCandidate
import java.nio.charset.StandardCharsets

/**
 * A class representing the Event object. All response messages are asynchronously delivered
 * to the recipient as events (for example, an SDP offer or SDP answer delivery).
 *
 * @see <a href="https://docs.aws.amazon.com/kinesisvideostreams-webrtc-dg/latest/devguide/kvswebrtc-websocket-apis-7.html">Event</a>
 */
class Event(
    val senderClientId: String,
    val messageType: String,
    val messagePayload: String,
    var statusCode: String? = null,
    var body: String? = null
) {
    companion object {
        private const val TAG = "Event"

        /**
         * Attempts to convert an [ICE_CANDIDATE] [Event] into an [IceCandidate].
         *
         * @param event the ICE_CANDIDATE event to convert.
         * @return an [IceCandidate] from the [Event]. `null` if the IceCandidate wasn't
         * able to be constructed.
         */
        fun parseIceCandidate(event: Event?): IceCandidate? {
            if (event == null || event.messageType.equals("ICE_CANDIDATE", ignoreCase = true).not()) {
                Log.e(TAG, "$event is not an ICE_CANDIDATE type!")
                return null
            }

            val decode = Base64.decode(event.messagePayload, Base64.DEFAULT)
            val candidateString = String(decode, StandardCharsets.UTF_8)

            if (candidateString == "null") {
                Log.w(TAG, "Received null IceCandidate!")
                return null
            }

            val jsonObject = JsonParser.parseString(candidateString).asJsonObject

            val sdpMid = jsonObject["sdpMid"]?.asString?.removeSurrounding("\"") ?: ""

            val sdpMLineIndex = jsonObject["sdpMLineIndex"]?.asInt ?: -1

            // Ice Candidate needs one of these two to be present
            if (sdpMid.isEmpty() && sdpMLineIndex == -1) {
                return null
            }

            val candidate = jsonObject["candidate"]?.asString?.removeSurrounding("\"") ?: ""

            return IceCandidate(sdpMid, if (sdpMLineIndex == -1) 0 else sdpMLineIndex, candidate)
        }

        fun parseSdpEvent(answerEvent: Event): String {
            val message = String(Base64.decode(answerEvent.messagePayload.toByteArray(), Base64.DEFAULT))
            val jsonObject = JsonParser.parseString(message).asJsonObject
            val type = jsonObject["type"].asString

            if (!type.equals("answer", ignoreCase = true)) {
                Log.e(TAG, "Error in answer message")
            }

            val sdp = jsonObject["sdp"].asString
            Log.d(TAG, "SDP answer received from master: $sdp")
            return sdp
        }

        fun parseOfferEvent(offerEvent: Event): String {
            val decodedPayload = String(Base64.decode(offerEvent.messagePayload, Base64.DEFAULT))

            return JsonParser.parseString(decodedPayload)
                .takeIf { it.isJsonObject }
                ?.asJsonObject
                ?.get("sdp")
                ?.asString ?: ""
        }
    }

    override fun toString(): String {
        return "Event(senderClientId='$senderClientId', messageType='$messageType', messagePayload='$messagePayload', statusCode='$statusCode', body='$body')"
    }
}
