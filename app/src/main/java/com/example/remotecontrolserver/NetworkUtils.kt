package com.example.remotecontrolserver

import android.content.Context
import android.net.wifi.WifiManager
import timber.log.Timber

object NetworkUtils {
    fun getLocalIpAddress(context: Context): String? {
        return try {
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ip = wm.connectionInfo.ipAddress
            String.format(
                "%d.%d.%d.%d",
                ip and 0xff,
                ip shr 8 and 0xff,
                ip shr 16 and 0xff,
                ip shr 24 and 0xff
            )
        } catch (e: Exception) {
            Timber.e(e, "getLocalIpAddress failed: ${e.message}")
            null
        }
    }
}