package com.example.remotecontrolserver

import android.content.Context
import kotlinx.coroutines.*
import timber.log.Timber
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

object NetworkDiscovery {
    private const val DISCOVERY_PORT = 5001
    private const val BROADCAST_INTERVAL = 5_000L
    private var job: Job? = null

    fun start(context: Context) {
        if (job?.isActive == true) return
        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                val socket = DatagramSocket()
                socket.broadcast = true
                val ip = NetworkUtils.getLocalIpAddress(context) ?: "0.0.0.0"
                val message = "RemoteControlServer:$ip:5000"
                val buf = message.toByteArray(Charsets.UTF_8)
                val packet = DatagramPacket(buf, buf.size, InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT)
                while (isActive) {
                    try {
                        socket.send(packet)
                    } catch (e: Exception) {
                        Timber.w(e, "broadcast failed")
                    }
                    delay(BROADCAST_INTERVAL)
                }
                socket.close()
            } catch (e: Exception) {
                Timber.e(e, "Discovery start failed")
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}