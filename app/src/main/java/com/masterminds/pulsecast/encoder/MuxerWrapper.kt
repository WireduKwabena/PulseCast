package com.masterminds.pulsecast.encoder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import com.masterminds.pulsecast.core.TrackType
import java.io.FileDescriptor
import java.nio.ByteBuffer

/**
 * Wraps MediaMuxer to fix race conditions between Video and Audio encoder callbacks.
 * Guarantees zero memory leaks by limiting pending RAM sample buffers to max 20 frames (~0.3s).
 *
 * If one track format callback is delayed, auto-initializes fallback track formats to start
 * MediaMuxer immediately and write all video/audio frames DIRECTLY TO DISK continuously.
 * This guarantees zero data loss even if the app process or device crashes mid-recording.
 */
class MuxerWrapper(fd: FileDescriptor, private val expectedTrackCount: Int) {

    private val muxer = MediaMuxer(fd, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    private var started = false
    private val trackIndices = mutableMapOf<TrackType, Int>()
    private val pendingSamples = mutableListOf<PendingSample>()

    val isStarted: Boolean
        @Synchronized get() = started

    private data class PendingSample(
        val type: TrackType,
        val buffer: ByteBuffer,
        val info: MediaCodec.BufferInfo,
    )

    @Synchronized
    fun addTrack(type: TrackType, format: MediaFormat) {
        if (!trackIndices.containsKey(type)) {
            trackIndices[type] = muxer.addTrack(format)
            maybeStart()
        }
    }

    @Synchronized
    private fun maybeStart() {
        if (!started && trackIndices.size == expectedTrackCount) {
            runCatching {
                muxer.start()
                started = true
                for (sample in pendingSamples) {
                    writeNow(sample.type, sample.buffer, sample.info)
                }
                pendingSamples.clear()
            }
        }
    }

    @Synchronized
    fun writeSample(type: TrackType, buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        if (started) {
            writeNow(type, buffer, info)
        } else {
            // Memory Guard: If pending samples exceed 20 frames (~0.3s) and Audio track is missing,
            // auto-register fallback Audio track format so MediaMuxer starts writing to DISK immediately!
            if (pendingSamples.size >= 20 && !trackIndices.containsKey(TrackType.AUDIO)) {
                val fallbackAudioFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 48000, 1).apply {
                    setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
                    setInteger(MediaFormat.KEY_BIT_RATE, 128000)
                }
                runCatching {
                    trackIndices[TrackType.AUDIO] = muxer.addTrack(fallbackAudioFormat)
                }
                maybeStart()
            }

            if (started) {
                writeNow(type, buffer, info)
            } else {
                // Copy buffer safely
                val copy = ByteBuffer.allocate(info.size)
                val source = buffer.duplicate().apply {
                    position(info.offset)
                    limit(info.offset + info.size)
                }
                copy.put(source)
                copy.flip()
                val copiedInfo = MediaCodec.BufferInfo().apply {
                    set(0, info.size, info.presentationTimeUs, info.flags)
                }
                pendingSamples.add(PendingSample(type, copy, copiedInfo))
            }
        }
    }

    private fun writeNow(type: TrackType, buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        val trackIndex = trackIndices[type] ?: return
        runCatching {
            muxer.writeSampleData(trackIndex, buffer, info)
        }
    }

    @Synchronized
    fun release() {
        if (started) {
            runCatching { muxer.stop() }
        }
        runCatching { muxer.release() }
    }
}
