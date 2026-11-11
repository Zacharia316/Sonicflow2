package com.example.audio

import android.content.Context
import java.io.File

/**
 * Audio player helper for Studio DAW.
 * Contains no programmatic sound generation, synthesizers, or demo beat sequencers.
 * All sound in the app originates exclusively from user recorded microphone audio or imported files.
 */
class AudioPlayer(private val context: Context) {

    var onError: ((String) -> Unit)? = null
    var onStatus: ((String) -> Unit)? = null

    var isPlaying = false
        private set

    init {
        // Clean up and delete any previously cached demo or generated audio files
        try {
            val soundDir = File(context.cacheDir, "sounds")
            if (soundDir.exists()) {
                soundDir.deleteRecursively()
            }
        } catch (_: Exception) {}
    }

    fun stopLoop() {
        isPlaying = false
    }

    fun release() {
        isPlaying = false
    }

    companion object {
        /**
         * Wraps raw 16-bit mono PCM bytes recorded from the device microphone
         * into a standard 44-byte RIFF/WAVE header file.
         */
        fun pcmToWav(pcmData: ShortArray, sampleRate: Int): ByteArray {
            val totalAudioLen = pcmData.size * 2
            val totalDataLen = totalAudioLen + 36
            val channels = 1
            val byteRate = sampleRate * channels * 2
            val header = ByteArray(44)
            // RIFF/WAVE header
            header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
            header[4] = (totalDataLen and 0xff).toByte()
            header[5] = ((totalDataLen shr 8) and 0xff).toByte()
            header[6] = ((totalDataLen shr 16) and 0xff).toByte()
            header[7] = ((totalDataLen shr 24) and 0xff).toByte()
            header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
            header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
            header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
            header[20] = 1; header[21] = 0 // PCM format
            header[22] = channels.toByte(); header[23] = 0
            header[24] = (sampleRate and 0xff).toByte()
            header[25] = ((sampleRate shr 8) and 0xff).toByte()
            header[26] = ((sampleRate shr 16) and 0xff).toByte()
            header[27] = ((sampleRate shr 24) and 0xff).toByte()
            header[28] = (byteRate and 0xff).toByte()
            header[29] = ((byteRate shr 8) and 0xff).toByte()
            header[30] = ((byteRate shr 16) and 0xff).toByte()
            header[31] = ((byteRate shr 24) and 0xff).toByte()
            header[32] = (channels * 2).toByte(); header[33] = 0
            header[34] = 16; header[35] = 0
            header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
            header[40] = (totalAudioLen and 0xff).toByte()
            header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
            header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
            header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

            val out = ByteArray(44 + totalAudioLen)
            System.arraycopy(header, 0, out, 0, 44)
            var outIdx = 44
            for (i in pcmData.indices) {
                val s = pcmData[i].toInt()
                out[outIdx++] = (s and 0xff).toByte()
                out[outIdx++] = ((s shr 8) and 0xff).toByte()
            }
            return out
        }
    }
}
