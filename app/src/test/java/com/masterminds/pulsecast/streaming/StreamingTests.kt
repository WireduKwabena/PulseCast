package com.masterminds.pulsecast.streaming

import com.masterminds.pulsecast.core.TrackType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.ByteBuffer

class ReconnectBackoffTest {
    @Test
    fun delayGrowsAndCaps() {
        assertEquals(1_000L, ReconnectBackoff.delayForAttempt(1))
        assertEquals(2_000L, ReconnectBackoff.delayForAttempt(2))
        assertEquals(30_000L, ReconnectBackoff.delayForAttempt(20))
        assertTrue(ReconnectBackoff.shouldGiveUp(ReconnectBackoff.MAX_ATTEMPTS + 1))
        assertFalse(ReconnectBackoff.shouldGiveUp(ReconnectBackoff.MAX_ATTEMPTS))
    }
}

class DestinationConnectionTest {
    @Test
    fun reconnectingOneTargetDoesNotChangeAnother() {
        val first = DestinationConnection(StreamDestination("a", "A", "rtmp://a/live/key"))
        val second = DestinationConnection(StreamDestination("b", "B", "rtmp://b/live/key"))
        first.connect()
        second.connect()
        first.onConnected()
        second.onConnected()

        second.onDisconnected("network lost")

        assertTrue(first.isLive())
        assertTrue(second.state is ConnectionState.Reconnecting)
        second.retryNow()
        second.onConnected()
        assertTrue(second.isLive())
    }
}

class FanOutDistributorTest {
    @Test
    fun duplicatesBuffersAndIsolatesThrowingSink() {
        val successfulBuffers = mutableListOf<ByteBuffer>()
        val sinks = listOf(
            object : EncodedSampleSink {
                override fun onSample(sample: EncodedSample) { successfulBuffers += sample.data }
            },
            object : EncodedSampleSink {
                override fun onSample(sample: EncodedSample) { throw IllegalStateException("target disconnected") }
            },
            object : EncodedSampleSink {
                override fun onSample(sample: EncodedSample) { successfulBuffers += sample.data }
            }
        )
        val source = ByteBuffer.wrap(byteArrayOf(1, 2, 3))

        FanOutDistributor(sinks).distribute(
            EncodedSample(TrackType.VIDEO, source, presentationTimeUs = 42L, isKeyFrame = true)
        )

        assertEquals(2, successfulBuffers.size)
        assertNotSame(successfulBuffers[0], successfulBuffers[1])
        assertEquals(0, source.position())
        successfulBuffers.forEach { buffer ->
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            assertTrue(bytes.contentEquals(byteArrayOf(1, 2, 3)))
        }
    }
}

class RtmpAudioConfigurationTest {
    @Test
    fun forwardsEncoderAudioFormatBeforeFirstAudioFrame() {
        val publisher = RecordingPublisher()
        val sink = RtmpDestinationSink(
            DestinationConnection(StreamDestination("audio", "Audio", "rtmp://example.test/live/key")),
            publisher
        )
        sink.start()
        sink.setAudioConfig(byteArrayOf(0x12, 0x08), sampleRate = 48_000, channelCount = 2)
        sink.onSample(EncodedSample(TrackType.AUDIO, ByteBuffer.wrap(byteArrayOf(5, 6)), 123L, true))

        assertEquals(48_000, publisher.sampleRate)
        assertEquals(2, publisher.channelCount)
        assertEquals(1, publisher.configCalls)
        assertEquals(1, publisher.audioFrameCalls)
        sink.stop()
    }

    private class RecordingPublisher : RawRtmpPublisher {
        var sampleRate = 0
        var channelCount = 0
        var configCalls = 0
        var audioFrameCalls = 0

        override fun connect(url: String, onConnected: () -> Unit, onDisconnected: (String) -> Unit) = onConnected()
        override fun sendVideoConfig(sps: ByteArray, pps: ByteArray) = Unit
        override fun sendVideoFrame(data: ByteBuffer, presentationTimeUs: Long, isKeyFrame: Boolean) = Unit
        override fun sendAudioConfig(config: ByteArray, sampleRate: Int, channelCount: Int) {
            this.sampleRate = sampleRate
            this.channelCount = channelCount
            configCalls++
        }
        override fun sendAudioFrame(data: ByteBuffer, presentationTimeUs: Long) { audioFrameCalls++ }
        override fun disconnect() = Unit
    }
}

class NalUnitParserTest {
    @Test
    fun parsesThreeAndFourByteAnnexBStartCodes() {
        val sps = byteArrayOf(0x67, 0x42, 0x00, 0x1e)
        val pps = byteArrayOf(0x68, 0xce.toByte(), 0x3c, 0x80.toByte())
        val stream = byteArrayOf(
            0, 0, 0, 1, *sps,
            0, 0, 1, *pps
        )

        val parsed = NalUnitParser.findParameterSets(stream)
        assertTrue(parsed.sps!!.contentEquals(sps))
        assertTrue(parsed.pps!!.contentEquals(pps))
        assertNull(NalUnitParser.findParameterSets(byteArrayOf(1, 2, 3)).sps)
    }
}
