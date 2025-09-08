package com.example.remotecontrolserver

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import kotlinx.coroutines.*
import timber.log.Timber

object TelemetryManager {
    private var job: Job? = null

    fun start(context: Context) {
        if (job?.isActive == true) return
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                val batt = readBattery(context)
                val temp = readTemperature()
                Timber.i("Telemetry batt=$batt temp=$temp")
                delay(5_000)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private fun readBattery(context: Context): Int {
        return try {
            val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, ifilter)
            val level = batteryStatus?.getIntExtra("level", -1) ?: -1
            val scale = batteryStatus?.getIntExtra("scale", -1) ?: -1
            if (level >= 0 && scale > 0) level * 100 / scale else -1
        } catch (e: Exception) {
            -1
        }
    }

    private fun readTemperature(): Float {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val tm = android.os.Temperature()
                // fallback stub: read from thermal service requires privileged API; we provide -1 for v0.1.0
                -1f
            } else -1f
        } catch (e: Exception) {
            -1f
        }
    }
}