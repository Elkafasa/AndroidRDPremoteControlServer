package com.example.remotecontrolserver

import android.content.Context
import kotlinx.coroutines.*
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import timber.log.Timber
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.TimeUnit

/**
 * RemoteService manages the TCP/WebSocket control plane for incoming clients.
 * For 0.1.0 we provide a simple WebSocket server-like behavior using OkHttp as client on server side to connect to client ws,
 * but here we implement a simple TCP Server (port 5000) that accepts JSON text messages.
 */
object RemoteService {
    private var serverJob: Job? = null
    private const val PORT = 5000

    fun start(context: Context) {
        if (serverJob?.isActive == true) return
        serverJob = CoroutineScope(Dispatchers.IO).launch {
            val server = ServerSocket(PORT)
            Timber.i("Control server started on $PORT")
            try {
                while (isActive) {
                    val client = server.accept()
                    Timber.i("Client connected: ${client.inetAddress.hostAddress}")
                    handleClient(context, client)
                }
            } finally {
                server.close()
            }
        }
    }

    fun stop() {
        serverJob?.cancel()
        serverJob = null
    }

    private fun handleClient(context: Context, socket: Socket) {
        CoroutineScope(Dispatchers.IO).launch {
            socket.use { s ->
                val reader = s.getInputStream().bufferedReader()
                val writer = s.getOutputStream().bufferedWriter()
                // send welcome and pairing challenge
                writer.write("{\"type\":\"welcome\"}\n")
                writer.flush()
                while (isActive && !s.isClosed) {
                    val line = reader.readLine() ?: break
                    try {
                        val json = JSONObject(line)
                        when (json.optString("type")) {
                            "pair" -> {
                                val code = json.optString("code")
                                val serverCode = PairingManager.getPairingCode(context)
                                if (code == serverCode) {
                                    writer.write("{\"type\":\"paired\",\"status\":\"ok\"}\n")
                                    writer.flush()
                                    // provide keyset to client in real impl - for v0.1.0 we send the base64 keyset
                                    val keyBytes = PairingManager.getAeadKey(context)
                                    if (keyBytes != null) {
                                        val encoded = android.util.Base64.encodeToString(keyBytes, android.util.Base64.NO_WRAP)
                                        writer.write("{\"type\":\"keyset\",\"data\":\"$encoded\"}\n")
                                        writer.flush()
                                    }
                                } else {
                                    writer.write("{\"type\":\"paired\",\"status\":\"fail\"}\n")
                                    writer.flush()
                                }
                            }
                            "start_stream" -> {
                                // Client asked to start receiving video/audio — in 0.1.0 we treat this as acknowledged
                                writer.write("{\"type\":\"start_ack\"}\n")
                                writer.flush()
                            }
                            "telemetry" -> {
                                // client telemetry signals accepted (e.g., upscale/downscale)
                                // you could adapt encoder settings here
                            }
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "client loop")
                    }
                }
            }
        }
    }
}