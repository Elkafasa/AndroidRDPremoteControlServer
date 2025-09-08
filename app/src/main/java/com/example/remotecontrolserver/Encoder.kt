package com.example.remotecontrolserver

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.view.Surface

class Encoder(
    private val width: Int,
    private val height: Int,
    private val bitrate: Int,
    private val fps: Int,
    private val streamer: NetworkStreamer
) {
    val inputSurface: Surface
    private val codec: MediaCodec = MediaCodec.createEncoderByType("video/avc")

    init {
        val format = MediaFormat.createVideoFormat("video/avc", width, height)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, fps)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        inputSurface = codec.createInputSurface()
    }

    fun start() {
        codec.start()
        Thread {
            val bufferInfo = MediaCodec.BufferInfo()
            while (true) {
                val outputBufferId = codec.dequeueOutputBuffer(bufferInfo, 10000)
                if (outputBufferId >= 0) {
                    val encodedData = codec.getOutputBuffer(outputBufferId)
                    encodedData?.let { streamer.sendVideo(it, bufferInfo) }
                    codec.releaseOutputBuffer(outputBufferId, false)
                }
            }
        }.start()
    }

    fun stop() {
        codec.stop()
        codec.release()
    }
}
