package com.example.remotecontrolserver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class ScreenCaptureService : Service() {

    private var projection: MediaProjection? = null
    private var encoder: VideoEncoder? = null
    private var audio: AudioStreamer? = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra("resultCode", 0) ?: 0
        val data = intent?.getParcelableExtra<Intent>("data") ?: run {
            stopSelf()
            return START_NOT_STICKY
        }

        val mgr = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        projection = mgr.getMediaProjection(resultCode, data)

        startForeground(101, createNotification())

        // Start discovery and control server
        NetworkDiscovery.start(this)
        RemoteService.start(this)

        // Start encoder (480p)
        encoder = VideoEncoder(this, projection!!, 854, 480, 30, 2_500_000)
        encoder?.start()

        // Start audio (mic)
        audio = AudioStreamer(this)
        audio?.start()

        return START_STICKY
    }

    override fun onDestroy() {
        encoder?.stop()
        audio?.stop()
        projection?.stop()
        NetworkDiscovery.stop()
        RemoteService.stop()
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        val channelId = "remote_control_channel"
        val channelName = "Remote Control Streaming"
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(ch)
        }
        return Notification.Builder(this, channelId)
            .setContentTitle("Remote Control Server")
            .setContentText("Streaming enabled")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}