package com.example.remotecontrolserver

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.os.Build
import android.util.Log
import timber.log.Timber
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.concurrent.thread

class VideoEncoder(
    private val context: Context,
    private val projection: MediaProjection,
    private val width: Int,
    private val height: Int,
    private val fps: Int,
    private var bitrate: Int
) {
    private var codec: MediaCodec? = null
    private var inputSurface: android.view.Surface? = null
    private var running = false
    private var streamer: NetworkStreamer? = null

    fun start() {
        try {
            val mime = MediaFormat.MIMETYPE_VIDEO_AVC
            val format = MediaFormat.createVideoFormat(mime, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }
            codec = MediaCodec.createEncoderByType(mime)
            codec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            inputSurface = codec?.createInputSurface()
            codec?.start()

            // Create virtual display using this surface
            projection.createVirtualDisplay(
                "rct_display",
                width,
                height,
                context.resources.displayMetrics.densityDpi,
                0,
                inputSurface,
                null,
                null
            )

            // Start streamer (will broadcast to client if connected)
            streamer = NetworkStreamer(null, 6000) // port used when client connects; will handle connection
            streamer?.startListeningForClient()

            running = true
            thread(name = "video-encoder") {
                drainEncoderLoop()
            }
        } catch (e: IOException) {
            Timber.e(e, "VideoEncoder start failed")
        }
    }

    private fun drainEncoderLoop() {
        val bufferInfo = MediaCodec.BufferInfo()
        while (running) {
            val outIndex = codec?.dequeueOutputBuffer(bufferInfo, 10_000L) ?: -1
            if (outIndex >= 0) {
                val encoded = codec?.getOutputBuffer(outIndex)
                encoded?.let { buf ->
                    val data = ByteArray(bufferInfo.size)
                    buf.get(data)
                    streamer?.sendVideoFrame(data)
                }
                codec?.releaseOutputBuffer(outIndex, false)
            } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                val newFormat = codec?.outputFormat
                Timber.i("Encoder format changed: $newFormat")
            }
        }
        codec?.stop()
        codec?.release()
    }

    fun stop() {
        running = false
        try {
            inputSurface?.release()
        } catch (e: Exception) {
        }
        try {
            codec?.stop()
            codec?.release()
        } catch (e: Exception) {
        }
        streamer?.stop()
    }

    fun setBitrate(newBps: Int) {
        // For API21+, try codec.setParameters (API 19+?). We'll attempt dynamic update where supported.
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val params = android.os.Bundle()
                params.putInt("video-bitrate", newBps)
                codec?.setParameters(params)
                bitrate = newBps
            }
        } catch (e: Exception) {
            Timber.w(e, "setBitrate failed")
        }
    }
}