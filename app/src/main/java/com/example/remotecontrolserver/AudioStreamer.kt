package com.example.remotecontrolserver

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaRecorder
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.DataOutputStream
import java.net.Socket

class AudioStreamer(private val context: Context) {
    private var running = false
    private var encoder: MediaCodec? = null
    private var audioRecord: AudioRecord? = null
    private var streamerSocket: Socket? = null

    fun start() {
        running = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sampleRate = 44100
                val channelConfig = AudioFormat.CHANNEL_IN_MONO
                val audioFormat = AudioFormat.ENCODING_PCM_16BIT
                val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
                audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize)
                audioRecord?.startRecording()

                val mime = MediaFormat.MIMETYPE_AUDIO_AAC
                val format = MediaFormat.createAudioFormat(mime, sampleRate, 1).apply {
                    setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
                    setInteger(MediaFormat.KEY_BIT_RATE, 128000)
                }
                encoder = MediaCodec.createEncoderByType(mime)
                encoder?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                encoder?.start()

                // No dedicated socket; we'll rely on NetworkStreamer to manage client socket; find local streamer
                val ns = NetworkStreamer(context, 6000) // in real prod tie to same socket
                ns.startListeningForClient()

                val buffer = ByteArray(bufferSize)
                while (running) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        // For simplicity, we skip full AAC encoding pipeline here for v0.1.0
                        // In production you'd feed into MediaCodec input buffers and retrieve AAC frames.
                        // We'll just send raw PCM payload (client must handle it) — simpler for this release.
                        ns.sendVideoFrame(buffer.copyOf(read))
                    }
                    delay(10)
                }
            } catch (e: Exception) {
                Timber.e(e, "AudioStreamer error")
            } finally {
                try {
                    audioRecord?.stop()
                    audioRecord?.release()
                } catch (e: Exception) {}
                try {
                    encoder?.stop()
                    encoder?.release()
                } catch (e: Exception) {}
            }
        }
    }

    fun stop() {
        running = false
    }
}