package com.screenovate.superdo

import android.util.Log
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer
import java.nio.charset.Charset

private const val TAG = "CustomWebSocketClient"

/**
 * CustomWebSocketClient
 * @author Gabriel Noam
 */
internal open class CustomWebSocketClient(
    address: String,
    private val onMessageReceived: (String?) -> Unit,
    private val onErrorReceived: (Exception?) -> Unit)
        : WebSocketClient(URI(address)) {

    private val CONNECTION_TIME_LOST = 1006

    override fun onOpen(handshakedata: ServerHandshake?) {
        Log.i(TAG, "onOpen")
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        Log.i(TAG, "onClose")
        if(code == CONNECTION_TIME_LOST) {
            connect()
        }
    }

    override fun onMessage(message: String?) {
        Log.i(TAG, "onMessage")
        onMessageReceived(message)
    }

    override fun onMessage(bytes: ByteBuffer?) {
        bytes ?: return
        val buffer = ByteArray(bytes.remaining())
        bytes.get(buffer)
        onMessage(String(buffer, Charset.defaultCharset()))
    }

    override fun onError(ex: Exception?) {
        Log.i(TAG, "onMessage")
        onErrorReceived(ex)
    }
}