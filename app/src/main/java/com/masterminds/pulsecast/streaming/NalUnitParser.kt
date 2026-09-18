package com.masterminds.pulsecast.streaming

/**
 * Extracts SPS/PPS NAL units from an Annex-B formatted H.264 bitstream.
 * RTMP (via the AVCDecoderConfigurationRecord in its FLV video tag) needs
 * these separately from frame data — MediaCodec's AVC encoder outputs
 * Annex-B by default (each NAL unit prefixed with a 0x000001 or
 * 0x00000001 start code), so scanning for start codes and reading the NAL
 * type from the byte that follows is enough. No MediaFormat csd-0/csd-1
 * buffers needed, which is what keeps this pure and library-agnostic.
 *
 * Reliable per the H.264 spec's own design: encoders insert emulation-
 * prevention bytes (0x03) specifically so a raw payload can never
 * accidentally contain a byte sequence that looks like a start code —
 * that's what makes naive start-code scanning like this safe in practice,
 * not just "usually works."
 *
 * Pure byte manipulation — no Android dependency, directly testable.
 */
object NalUnitParser {

    private const val NAL_TYPE_SPS = 7
    private const val NAL_TYPE_PPS = 8

    data class ParameterSets(val sps: ByteArray?, val pps: ByteArray?)

    fun findParameterSets(data: ByteArray): ParameterSets {
        var sps: ByteArray? = null
        var pps: ByteArray? = null
        for (nal in splitNalUnits(data)) {
            if (nal.isEmpty()) continue
            when (nal[0].toInt() and 0x1F) {
                NAL_TYPE_SPS -> sps = nal
                NAL_TYPE_PPS -> pps = nal
            }
        }
        return ParameterSets(sps, pps)
    }

    /** Splits an Annex-B bytestream into individual NAL units, start codes stripped. */
    fun splitNalUnits(data: ByteArray): List<ByteArray> {
        val startCodes = findStartCodes(data)
        if (startCodes.isEmpty()) return emptyList()

        val units = mutableListOf<ByteArray>()
        for (i in startCodes.indices) {
            val (start, codeLength) = startCodes[i]
            val nalStart = start + codeLength
            val nalEnd = if (i + 1 < startCodes.size) startCodes[i + 1].first else data.size
            if (nalStart < nalEnd) {
                units.add(data.copyOfRange(nalStart, nalEnd))
            }
        }
        return units
    }

    /** Returns (offset, startCodeLength) for every 3- or 4-byte start code found. */
    private fun findStartCodes(data: ByteArray): List<Pair<Int, Int>> {
        val results = mutableListOf<Pair<Int, Int>>()
        var i = 0
        while (i < data.size - 2) {
            if (data[i] == 0.toByte() && data[i + 1] == 0.toByte() && data[i + 2] == 1.toByte()) {
                if (i > 0 && data[i - 1] == 0.toByte()) {
                    results.add((i - 1) to 4)
                } else {
                    results.add(i to 3)
                }
                i += 3
            } else {
                i++
            }
        }
        return results
    }
}
