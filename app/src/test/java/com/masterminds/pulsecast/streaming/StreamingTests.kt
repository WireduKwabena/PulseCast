import com.masterminds.pulsecast.core.TrackType
import com.kwabena.screenrecorder.streaming.*
import java.nio.ByteBuffer

private var failures = 0

private fun check(name: String, condition: Boolean) {
    if (condition) {
        println("[PASS] $name")
    } else {
        println("[FAIL] $name")
        failures++
    }
}

private fun expectThrows(name: String, block: () -> Unit) {
    try {
        block()
        println("[FAIL] $name (expected an exception, none thrown)")
        failures++
    } catch (e: Exception) {
        println("[PASS] $name (threw ${e.javaClass.simpleName})")
    }
}

fun main() {
    println("--- ReconnectBackoff ---")
    run {
        check("attempt 1 delay is the base delay (1000ms)", ReconnectBackoff.delayForAttempt(1) == 1000L)
        check("attempt 2 delay doubles (2000ms)", ReconnectBackoff.delayForAttempt(2) == 2000L)
        check("attempt 3 delay doubles again (4000ms)", ReconnectBackoff.delayForAttempt(3) == 4000L)
        check("delay is capped at 30000ms for large attempts", ReconnectBackoff.delayForAttempt(20) == 30_000L)
        check("shouldGiveUp false within max attempts", !ReconnectBackoff.shouldGiveUp(ReconnectBackoff.MAX_ATTEMPTS))
        check("shouldGiveUp true beyond max attempts", ReconnectBackoff.shouldGiveUp(ReconnectBackoff.MAX_ATTEMPTS + 1))
        expectThrows("attempt 0 rejected") { ReconnectBackoff.delayForAttempt(0) }
    }

    println("\n--- DestinationConnection ---")
    run {
        val youtube = StreamDestination("yt", "YouTube", "rtmp://a.rtmp.youtube.com/live2/KEY")
        val conn = DestinationConnection(youtube)

        check("starts Idle", conn.state == ConnectionState.Idle)
        conn.connect()
        check("connect() -> Connecting", conn.state == ConnectionState.Connecting)
        conn.onConnected()
        check("onConnected() -> Live", conn.state == ConnectionState.Live)
        check("isLive() true while Live", conn.isLive())

        conn.onDisconnected("network blip")
        check(
            "onDisconnected() from Live -> Reconnecting(attempt=1)",
            conn.state == ConnectionState.Reconnecting(1, 1000L)
        )
        check("isLive() false while Reconnecting", !conn.isLive())

        conn.retryNow()
        check("retryNow() -> Connecting", conn.state == ConnectionState.Connecting)
        conn.onConnected()
        check("reconnected successfully -> Live again", conn.state == ConnectionState.Live)

        // Drive it to actually give up, and confirm the escalating backoff
        // along the way — this is the exact sequence that happens during a
        // sustained outage.
        val conn2 = DestinationConnection(youtube)
        conn2.connect()
        conn2.onConnected()
        var lastDelay = 0L
        for (i in 1..ReconnectBackoff.MAX_ATTEMPTS) {
            conn2.onDisconnected("attempt $i drop")
            val state = conn2.state
            if (state is ConnectionState.Reconnecting) {
                check("attempt $i backoff (${state.delayMs}ms) >= previous ($lastDelay ms)", state.delayMs >= lastDelay)
                lastDelay = state.delayMs
                conn2.retryNow()
            }
        }
        conn2.onDisconnected("final drop")
        check("gives up after MAX_ATTEMPTS -> Failed", conn2.state is ConnectionState.Failed)

        conn2.connect()
        check("manual retry from Failed -> Connecting again", conn2.state == ConnectionState.Connecting)

        expectThrows("onConnected() from Idle rejected") {
            DestinationConnection(youtube).onConnected()
        }
        expectThrows("connect() while already Live rejected") {
            val c = DestinationConnection(youtube)
            c.connect(); c.onConnected(); c.connect()
        }
        expectThrows("retryNow() while Live rejected") {
            val c = DestinationConnection(youtube)
            c.connect(); c.onConnected(); c.retryNow()
        }
    }

    println("\n--- DestinationConnection independence across instances ---")
    run {
        // The entire point of per-destination state: Twitch failing must
        // not touch YouTube's state at all.
        val youtube = DestinationConnection(StreamDestination("yt", "YouTube", "rtmp://yt/KEY"))
        val twitch = DestinationConnection(StreamDestination("tw", "Twitch", "rtmp://tw/KEY"))

        youtube.connect(); youtube.onConnected()
        twitch.connect(); twitch.onConnected()
        check("both start Live", youtube.isLive() && twitch.isLive())

        twitch.onDisconnected("twitch dropped")
        check("twitch is no longer live", !twitch.isLive())
        check("youtube is UNAFFECTED by twitch's disconnect", youtube.isLive())
    }

    println("\n--- FanOutDistributor ---")
    run {
        class RecordingSink(val name: String, val shouldThrow: Boolean = false) : EncodedSampleSink {
            val received = mutableListOf<EncodedSample>()
            override fun onSample(sample: EncodedSample) {
                if (shouldThrow) throw RuntimeException("$name: simulated failure")
                received.add(sample)
            }
        }

        val local = RecordingSink("local-file")
        val youtubeSink = RecordingSink("youtube")
        val twitchSink = RecordingSink("twitch", shouldThrow = true) // simulates a dropped RTMP connection

        val distributor = FanOutDistributor(listOf(local, youtubeSink, twitchSink))

        val originalData = ByteBuffer.wrap(byteArrayOf(1, 2, 3, 4, 5))
        val sample = EncodedSample(TrackType.VIDEO, originalData, presentationTimeUs = 12345L, isKeyFrame = true)

        distributor.distribute(sample)

        check("local sink received the sample despite twitch throwing", local.received.size == 1)
        check("youtube sink received the sample despite twitch throwing", youtubeSink.received.size == 1)
        check(
            "each sink got an independently-positioned buffer duplicate",
            local.received[0].data !== youtubeSink.received[0].data
        )
        check(
            "duplicated buffers still read the same underlying bytes",
            local.received[0].data.duplicate().let { buf ->
                val bytes = ByteArray(buf.remaining())
                buf.get(bytes)
                bytes.toList() == listOf<Byte>(1, 2, 3, 4, 5)
            }
        )

        // Distribute a second sample — the first distribute() call must not
        // have left the ORIGINAL buffer's position mutated in a way that
        // breaks a subsequent read of it.
        originalData.rewind()
        val sample2 = sample.copy(presentationTimeUs = 99999L)
        distributor.distribute(sample2)
        check("second distribute() also reaches unaffected sinks", local.received.size == 2 && youtubeSink.received.size == 2)
    }

    println("\n--- NalUnitParser ---")
    run {
        val sps = byteArrayOf(0x67, 0x42.toByte(), 0x00, 0x1E)
        val pps = byteArrayOf(0x68, 0xCE.toByte(), 0x3C, 0x80.toByte())
        val idr = byteArrayOf(0x65, 0x88.toByte(), 0x84.toByte(), 0x00)

        fun annexB(startCodeLen: Int, vararg nals: ByteArray): ByteArray {
            val startCode = if (startCodeLen == 4) byteArrayOf(0, 0, 0, 1) else byteArrayOf(0, 0, 1)
            val out = mutableListOf<Byte>()
            for (nal in nals) {
                out.addAll(startCode.toList())
                out.addAll(nal.toList())
            }
            return out.toByteArray()
        }

        val fourByteStream = annexB(4, sps, pps, idr)
        val units4 = NalUnitParser.splitNalUnits(fourByteStream)
        check("4-byte start codes: splits into exactly 3 NAL units", units4.size == 3)
        check("4-byte start codes: unit 0 matches SPS bytes", units4[0].toList() == sps.toList())
        check("4-byte start codes: unit 2 matches IDR bytes (last unit, no trailing start code)", units4[2].toList() == idr.toList())

        val parsed4 = NalUnitParser.findParameterSets(fourByteStream)
        check("4-byte start codes: SPS correctly identified by NAL type", parsed4.sps?.toList() == sps.toList())
        check("4-byte start codes: PPS correctly identified by NAL type", parsed4.pps?.toList() == pps.toList())

        val threeByteStream = annexB(3, sps, pps)
        val parsed3 = NalUnitParser.findParameterSets(threeByteStream)
        check("3-byte start codes also parse correctly", parsed3.sps?.toList() == sps.toList() && parsed3.pps?.toList() == pps.toList())

        val noStartCode = byteArrayOf(1, 2, 3, 4, 5)
        check("no start code present -> empty result", NalUnitParser.splitNalUnits(noStartCode).isEmpty())

        val idrOnlyStream = annexB(4, idr)
        val idrOnlyParsed = NalUnitParser.findParameterSets(idrOnlyStream)
        check("a keyframe with no SPS/PPS present yields nulls, not a crash", idrOnlyParsed.sps == null && idrOnlyParsed.pps == null)
    }

    println("\n--- RtmpDestinationSink ---")
    run {
        class FakeRtmpPublisher : RawRtmpPublisher {
            var connectCalls = 0
            var disconnectCalls = 0
            var videoConfigCalls = mutableListOf<Pair<ByteArray, ByteArray>>()
            var videoFrameCalls = 0
            var audioConfigCalls = mutableListOf<ByteArray>()
            var audioFrameCalls = 0
            private var connectedCallback: (() -> Unit)? = null
            private var disconnectedCallback: ((String) -> Unit)? = null

            override fun connect(url: String, onConnected: () -> Unit, onDisconnected: (String) -> Unit) {
                connectCalls++
                connectedCallback = onConnected
                disconnectedCallback = onDisconnected
            }

            fun simulateConnected() = connectedCallback?.invoke()
            fun simulateDropped(reason: String) = disconnectedCallback?.invoke(reason)

            override fun sendVideoConfig(sps: ByteArray, pps: ByteArray) { videoConfigCalls.add(sps to pps) }
            override fun sendVideoFrame(data: ByteBuffer, presentationTimeUs: Long, isKeyFrame: Boolean) { videoFrameCalls++ }
            override fun sendAudioConfig(config: ByteArray) { audioConfigCalls.add(config) }
            override fun sendAudioFrame(data: ByteBuffer, presentationTimeUs: Long) { audioFrameCalls++ }
            override fun disconnect() { disconnectCalls++ }
        }

        fun keyframeSample(): EncodedSample {
            val sps = byteArrayOf(0x67, 0x42.toByte(), 0x00, 0x1E)
            val pps = byteArrayOf(0x68, 0xCE.toByte(), 0x3C, 0x80.toByte())
            val idr = byteArrayOf(0x65, 0x01, 0x02, 0x03)
            val bytes = mutableListOf<Byte>()
            for (nal in listOf(sps, pps, idr)) {
                bytes.addAll(listOf(0, 0, 0, 1))
                bytes.addAll(nal.toList())
            }
            return EncodedSample(TrackType.VIDEO, ByteBuffer.wrap(bytes.toByteArray()), 0L, isKeyFrame = true)
        }

        val fake = FakeRtmpPublisher()
        val destination = StreamDestination("yt", "YouTube", "rtmp://a.rtmp.youtube.com/live2/KEY")
        val sink = RtmpDestinationSink(DestinationConnection(destination), fake)

        sink.start()
        check("start() calls publisher.connect() exactly once", fake.connectCalls == 1)
        sink.onSample(keyframeSample())
        check("not live yet, so a sample before onConnected is dropped", fake.videoFrameCalls == 0)

        fake.simulateConnected()
        check("connection is live after simulateConnected()", sink.connection.isLive())

        sink.onSample(keyframeSample())
        check("first keyframe after connecting sends video config exactly once", fake.videoConfigCalls.size == 1)
        check("first keyframe's frame data is also sent", fake.videoFrameCalls == 1)

        sink.onSample(keyframeSample())
        check("second keyframe does NOT re-send video config", fake.videoConfigCalls.size == 1)
        check("second keyframe's frame data is still forwarded", fake.videoFrameCalls == 2)

        sink.setAudioConfig(byteArrayOf(0x12, 0x10))
        val audioSample = EncodedSample(TrackType.AUDIO, ByteBuffer.wrap(byteArrayOf(1, 2, 3)), 1000L, isKeyFrame = true)
        sink.onSample(audioSample)
        check("audio config sent exactly once on first audio sample", fake.audioConfigCalls.size == 1)
        check("audio frame forwarded", fake.audioFrameCalls == 1)
        sink.onSample(audioSample)
        check("audio config NOT re-sent on second audio sample", fake.audioConfigCalls.size == 1)

        fake.simulateDropped("network lost")
        check("connection no longer live after simulateDropped()", !sink.connection.isLive())
        val framesBefore = fake.videoFrameCalls
        sink.onSample(keyframeSample())
        check("samples after a drop are NOT forwarded to a dead connection", fake.videoFrameCalls == framesBefore)

        sink.stop()
        check("stop() calls publisher.disconnect()", fake.disconnectCalls == 1)
        check("stop() resets connection to Idle", sink.connection.state == ConnectionState.Idle)
    }

    println("\n" + if (failures == 0) "ALL TESTS PASSED" else "$failures TEST(S) FAILED")
    if (failures > 0) kotlin.system.exitProcess(1)
}
