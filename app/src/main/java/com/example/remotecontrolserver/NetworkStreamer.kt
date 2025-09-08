package com.example.remotecontrolserver

import android.content.Context
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicReference

class NetworkStreamer(private val context: Context?, private val port: Int) {
    private var serverJob: Job? = null
    private val clientSocketRef = AtomicReference<Socket?>(null)
    private var aeadPrimitive: com.google.crypto.tink.Aead? = null

    fun startListeningForClient() {
        if (serverJob?.isActive == true) return
        serverJob = CoroutineScope(Dispatchers.IO).launch {
            val server = ServerSocket(port)
            Timber.i("NetworkStreamer listening on $port")
            try {
                while (isActive) {
                    val client = server.accept()
                    Timber.i("Streamer client connected: ${client.inetAddress.hostAddress}")
                    clientSocketRef.getAndSet(client)?.close()
                }
            } finally {
                server.close()
            }
        }

        // load AEAD if pairing done
        val keyBytes = context?.let { PairingManager.getAeadKey(it) }
        if (keyBytes != null) {
            aeadPrimitive = PairingManager.aeadFromKeysetBytes(keyBytes)
        }
    }

    fun stop() {
        serverJob?.cancel()
        clientSocketRef.getAndSet(null)?.close()
    }

    fun sendVideoFrame(frame: ByteArray) {
        val socket = clientSocketRef.get() ?: return
        try {
            val out = DataOutputStream(socket.getOutputStream())
            val payload = if (aeadPrimitive != null) {
                aeadPrimitive!!.encrypt(frame, null)
            } else {
                frame
            }
            // prefix with 4-byte length
            out.writeInt(payload.size)
            out.write(payload)
            out.flush()
        } catch (e: Exception) {
            Timber.w(e, "sendVideoFrame failed")
            clientSocketRef.getAndSet(null)?.close()
        }
    }
}