package com.example.audio

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.ProjectEntity
import com.example.data.StudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Handles audio rendering and export for Studio DAW.
 * Exports real multitrack recordings and imported audio to a RIFF WAV file offline.
 * Contains no programmatic sound generation or synthetic beats.
 */
object AudioExporter {

    enum class ExportFormat(val title: String, val badge: String, val extension: String) {
        MP3("MP3 320kbps", "Recommended", ".mp3"),
        WAV("WAV", "Lossless", ".wav"),
        STEMS("Stems — Vocal / Beat / Adlibs", "Multi-track", ".zip")
    }

    enum class AudioQuality(val label: String, val sampleRate: Int, val bitDepth: Int) {
        CD_QUALITY("44.1kHz / 16-bit", 44100, 16),
        PRO_STUDIO("48kHz / 24-bit", 48000, 24),
        HI_RES("96kHz / 24-bit", 96000, 24)
    }

    /**
     * Renders real project audio clips to a valid .wav file in app storage.
     * If no clips exist, exports silence without crashing or generating fake sounds.
     */
    suspend fun renderAndExport(
        context: Context,
        project: ProjectEntity,
        format: ExportFormat,
        quality: AudioQuality,
        onProgress: (Int) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val exportDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()

        val cleanTitle = project.title.replace("\\s+".toRegex(), "_")
        val outputFile = File(exportDir, "${cleanTitle}_Final_Mix.wav")

        val sampleRate = quality.sampleRate
        val channels = 2 // Stereo
        val durationSec = project.durationSeconds.coerceIn(5, 300)
        val totalSamples = sampleRate * durationSec

        val fos = FileOutputStream(outputFile)
        // Write initial 44-byte WAV header placeholder
        val dummyHeader = ByteArray(44)
        fos.write(dummyHeader)

        val bufferSize = 4096
        val byteBuffer = ByteBuffer.allocate(bufferSize * channels * 2).order(ByteOrder.LITTLE_ENDIAN)

        // Parse real project tracks and clips
        val tracks = StudioTrack.listFromJson(project.notesData)
        val activeClips = mutableListOf<Pair<com.example.data.AudioClip, Float>>()
        val hasSolo = tracks.any { it.isSoloed }
        for (track in tracks) {
            val isAudible = if (hasSolo) track.isSoloed else !track.isMuted
            if (isAudible) {
                for (clip in track.clips) {
                    val file = File(clip.filePath)
                    if (file.exists() && file.length() > 44L) {
                        activeClips.add(Pair(clip, track.volume))
                    }
                }
            }
        }

        var samplesWritten = 0

        while (samplesWritten < totalSamples) {
            val chunk = minOf(bufferSize, totalSamples - samplesWritten)
            byteBuffer.clear()

            val leftSamples = FloatArray(chunk)
            val rightSamples = FloatArray(chunk)

            // Mix real audio clips if present at the current playback position
            val chunkStartSec = samplesWritten.toFloat() / sampleRate
            val chunkEndSec = (samplesWritten + chunk).toFloat() / sampleRate

            for ((clip, trackVol) in activeClips) {
                val clipStart = clip.startOffsetSeconds
                val clipEnd = clip.startOffsetSeconds + clip.durationSeconds
                if (chunkEndSec > clipStart && chunkStartSec < clipEnd) {
                    try {
                        val file = File(clip.filePath)
                        val fis = FileInputStream(file)
                        // Skip WAV header
                        fis.skip(44L)

                        val clipSampleOffset = ((chunkStartSec - clipStart) * sampleRate).toInt()
                        if (clipSampleOffset > 0) {
                            fis.skip(clipSampleOffset.toLong() * 2)
                        }

                        val readBuf = ByteArray(chunk * 2)
                        val bytesRead = fis.read(readBuf)
                        fis.close()

                        if (bytesRead > 0) {
                            val shortsRead = bytesRead / 2
                            for (s in 0 until shortsRead) {
                                val outIdx = if (clipSampleOffset < 0) -clipSampleOffset + s else s
                                if (outIdx in 0 until chunk) {
                                    val low = readBuf[s * 2].toInt() and 0xFF
                                    val high = readBuf[s * 2 + 1].toInt()
                                    val sampleVal = ((high shl 8) or low).toShort().toFloat() / Short.MAX_VALUE
                                    leftSamples[outIdx] += sampleVal * trackVol
                                    rightSamples[outIdx] += sampleVal * trackVol
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
            }

            for (i in 0 until chunk) {
                val l = leftSamples[i].coerceIn(-1.0f, 1.0f)
                val r = rightSamples[i].coerceIn(-1.0f, 1.0f)
                byteBuffer.putShort((l * Short.MAX_VALUE).toInt().toShort())
                byteBuffer.putShort((r * Short.MAX_VALUE).toInt().toShort())
            }

            fos.write(byteBuffer.array(), 0, chunk * channels * 2)
            samplesWritten += chunk

            val percent = ((samplesWritten.toDouble() / totalSamples) * 100).toInt()
            onProgress(percent)
            delay(5)
        }

        fos.flush()
        fos.close()

        // Patch standard WAV header with actual data size
        val totalAudioLen = totalSamples.toLong() * channels * 2
        val totalDataLen = totalAudioLen + 36
        writeWavHeader(outputFile, totalAudioLen, totalDataLen, sampleRate, channels, 16)

        onProgress(100)
        outputFile
    }

    private fun writeWavHeader(
        file: File,
        audioLength: Long,
        dataLength: Long,
        sampleRate: Int,
        channels: Int,
        bitsPerSample: Int
    ) {
        val raf = RandomAccessFile(file, "rw")
        raf.seek(0)
        val byteRate = (sampleRate * channels * bitsPerSample / 8).toLong()

        val header = ByteArray(44)
        // RIFF chunk
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (dataLength and 0xff).toByte()
        header[5] = ((dataLength shr 8) and 0xff).toByte()
        header[6] = ((dataLength shr 16) and 0xff).toByte()
        header[7] = ((dataLength shr 24) and 0xff).toByte()
        // WAVE
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        // fmt
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // 16 for PCM format
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // Audio format 1 = PCM
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * bitsPerSample / 8).toByte() // block align
        header[33] = 0
        header[34] = bitsPerSample.toByte()
        header[35] = 0
        // data
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (audioLength and 0xff).toByte()
        header[41] = ((audioLength shr 8) and 0xff).toByte()
        header[42] = ((audioLength shr 16) and 0xff).toByte()
        header[43] = ((audioLength shr 24) and 0xff).toByte()

        raf.write(header)
        raf.close()
    }

    fun shareExportedFile(context: Context, file: File, mimeType: String = "audio/wav") {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Exported from Studio DAW: ${file.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Audio Mix via"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
