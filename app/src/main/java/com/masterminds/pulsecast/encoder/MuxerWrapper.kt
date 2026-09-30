package com.masterminds.pulsecast.encoder

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import com.masterminds.pulsecast.core.TrackType
import java.io.FileDescriptor
import java.nio.ByteBuffer

/**
 * Wraps MediaMuxer to fix a real, common race condition: MediaMuxer.start()
 * may only be called once, and only after EVERY track that will ever be
 * written has been registered via addTrack() — but the video and audio
 * encoders produce their "format changed" callback (which is when you
 * learn the track's real MediaFormat) at different, unpredictable times.
 *
 * A naive implementation that calls start() as soon as the video track is
 * ready will crash (IllegalStateException) the moment the audio encoder
 * tries to add its track afterward. This wrapper buffers any sample data
 * that arrives before all expected tracks are registered, and flushes it
 * once the muxer actually starts.
 *
 * Takes a FileDescriptor rather than a path string so the same class works
 * for both the MediaStore-based save path (Android 10+, scoped storage)
 * and the legacy direct-file path (Android 9 and below) — see SaveLocation.
 *
 * Uses core.TrackType (shared with the Phase 3 streaming fan-out layer)
 * rather than its own nested enum, so a sample can flow through
 * FanOutDistributor to both this muxer AND an RTMP destination without a
 * translation step in between.
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
        check(!trackIndices.containsKey(type)) { "$type track already added" }
        trackIndices[type] = muxer.addTrack(format)
        maybeStart()
    }

    @Synchronized
    private fun maybeStart() {
        if (!started && trackIndices.size == expectedTrackCount) {
            muxer.start()
            started = true
            for (sample in pendingSamples) {
                writeNow(sample.type, sample.buffer, sample.info)
            }
            pendingSamples.clear()
        }
    }

    @Synchronized
    fun writeSample(type: TrackType, buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        if (started) {
            writeNow(type, buffer, info)
        } else {
            // Copy the buffer — the caller will reuse/release the original
            // MediaCodec buffer right after this call returns.
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

    private fun writeNow(type: TrackType, buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        val trackIndex = trackIndices[type] ?: error("Cannot write $type sample before its track is added")
        muxer.writeSampleData(trackIndex, buffer, info)
    }

    @Synchronized
    fun release() {
        if (started) {
            runCatching { muxer.stop() }
        }
        runCatching { muxer.release() }
    }
}
