package com.example.audio

import java.io.File
import java.io.FileInputStream
import kotlin.math.abs

object AudioUtils {

    /**
     * Extracts normalized peak waveform amplitudes (0.12f to 1.0f) from an audio file (WAV, MP3, etc.).
     * Returns a list of [pointCount] floating-point amplitudes suitable for drawing real waveforms.
     */
    fun extractWaveform(file: File, pointCount: Int = 48): List<Float> {
        if (!file.exists() || file.length() < 100L) {
            return List(pointCount) { 0.35f }
        }
        return try {
            val length = file.length()
            val stream = FileInputStream(file)
            val buffer = ByteArray(4096)

            val isWav = file.name.endsWith(".wav", ignoreCase = true)
            val startByte = if (isWav && length > 44L) 44L else 0L
            if (startByte > 0) {
                stream.skip(startByte)
            }
            val dataLength = length - startByte
            val chunkSize = (dataLength / pointCount).coerceAtLeast(100L)

            val peaks = mutableListOf<Float>()
            var maxObserved = 1

            for (i in 0 until pointCount) {
                val toRead = minOf(chunkSize, buffer.size.toLong()).toInt()
                val read = stream.read(buffer, 0, toRead)
                if (read > 0) {
                    var chunkPeak = 0
                    for (b in 0 until read - 1 step 2) {
                        val sample = (buffer[b + 1].toInt() shl 8) or (buffer[b].toInt() and 0xFF)
                        val absVal = abs(sample.toShort().toInt())
                        if (absVal > chunkPeak) chunkPeak = absVal
                    }
                    if (chunkPeak > maxObserved) maxObserved = chunkPeak
                    peaks.add(chunkPeak.toFloat())
                } else {
                    peaks.add(100f)
                }
                val remainder = chunkSize - toRead
                if (remainder > 0) {
                    stream.skip(remainder)
                }
            }
            stream.close()

            val normMax = maxOf(maxObserved.toFloat(), 1000f)
            peaks.map { (it / normMax).coerceIn(0.12f, 1.0f) }
        } catch (_: Exception) {
            List(pointCount) { 0.4f }
        }
    }

    /**
     * Formats seconds into a studio timecode string "00:00.00"
     */
    fun formatTimecode(totalSeconds: Float): String {
        val safeSeconds = totalSeconds.coerceAtLeast(0f)
        val mins = (safeSeconds / 60).toInt()
        val secs = (safeSeconds % 60).toInt()
        val hundredths = ((safeSeconds - safeSeconds.toInt()) * 100).toInt()
        return String.format("%02d:%02d.%02d", mins, secs, hundredths)
    }

    /**
     * Formats seconds into a ruler marker string "0:00", "0:05", "1:15"
     */
    fun formatRulerTime(seconds: Int): String {
        val mins = seconds / 60
        val remSecs = seconds % 60
        return String.format("%d:%02d", mins, remSecs)
    }
}
