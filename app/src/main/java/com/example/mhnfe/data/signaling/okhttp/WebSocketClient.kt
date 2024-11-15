package com.example.mhnfe.data.signaling.okhttp

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import android.util.Log
import com.example.mhnfe.data.signaling.SignalingListener
import com.example.mhnfe.utils.Constants
import org.awaitility.Awaitility.await
import java.util.concurrent.TimeUnit

/**
 * An OkHttp based WebSocket client.
 */
class WebSocketClient(
    uri: String,
    signalingListener: SignalingListener
) {
    private val TAG = "WebSocketClient"
    private val webSocket: WebSocket
    @Volatile
    private var isOpen = false

    init {
        val client = OkHttpClient.Builder().build()

        val userAgent = "${Constants.APP_NAME}/${Constants.VERSION} ${System.getProperty("http.agent")}".trim()

        Log.d(TAG, "User agent: $userAgent")

        val request = Request.Builder()
            .url(uri)
            .addHeader("User-Agent", userAgent)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connection opened")
                isOpen = true
            }

            override fun onMessage(webSocket: WebSocket, message: String) {
                Log.d(TAG, "Websocket received a message: $message")
                signalingListener.getWebsocketListener().onMessage(webSocket, message)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket connection closed: $reason")
                isOpen = false
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket connection failed", t)
                isOpen = false
                signalingListener.onException(t as Exception)
            }
        })

        // Await WebSocket connection
        await().atMost(10, TimeUnit.SECONDS).until { isOpen }
    }

    fun send(message: String) {
        if (isOpen) {
            if (webSocket.send(message)) {
                Log.d(TAG, "Successfully sent $message")
            } else {
                Log.d(TAG, "Could not send $message as the connection may have closing, closed, or canceled.")
            }
        } else {
            Log.d(TAG, "Cannot send the websocket message as it is not open.")
        }
    }

    fun disconnect() {
        if (isOpen) {
            if (webSocket.close(1000, "Disconnect requested")) {
                Log.d(TAG, "Websocket successfully disconnected.")
            } else {
                Log.d(TAG, "Websocket could not disconnect in a graceful shutdown. Going to cancel it to release resources.")
                webSocket.cancel()
            }
        } else {
            Log.d(TAG, "Cannot close the websocket as it is not open.")
        }
    }

    fun isOpen(): Boolean {
        return isOpen
    }
}