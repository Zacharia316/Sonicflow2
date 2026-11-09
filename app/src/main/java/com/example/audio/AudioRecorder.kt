package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * Microphone recorder for the Vocal Booth / Session screen.
 * Tracks live input level (VU meter in dB) and records 16-bit PCM WAV.
 */
class AudioRecorder(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var recordJob: Job? = null
    private var audioRecord: AudioRecord? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _inputLevel = MutableStateFlow(0f) // 0.0 to 1.0 scale
    val inputLevel: StateFlow<Float> = _inputLevel.asStateFlow()

    private val _inputDb = MutableStateFlow(-60f)
    val inputDb: StateFlow<Float> = _inputDb.asStateFlow()

    var lastRecordedFile: File? = null
        private set

    @SuppressLint("MissingPermission")
    fun startRecording(onCountDown: (Int) -> Unit = {}, onFinishedCountDown: () -> Unit = {}) {
        if (_isRecording.value) return

        recordJob = scope.launch {
            // Count-in: 3, 2, 1
            for (count in 3 downTo 1) {
                onCountDown(count)
                kotlinx.coroutines.delay(600)
            }
            onFinishedCountDown()

            val sampleRate = AudioSynthesizer.SAMPLE_RATE
            val minBufSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufSize
                )

                if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    _isRecording.value = false
                    return@launch
                }

                audioRecord?.startRecording()
                _isRecording.value = true

                val outputFile = File(context.cacheDir, "vocal_take_${System.currentTimeMillis()}.pcm")
                lastRecordedFile = outputFile
                val fos = FileOutputStream(outputFile)
                val buffer = ShortArray(1024)

                while (isActive && _isRecording.value) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        // Calculate RMS amplitude
                        var sum = 0.0
                        for (i in 0 until read) {
                            sum += buffer[i] * buffer[i]
                            // Write little-endian bytes
                            fos.write(buffer[i].toInt() and 0xFF)
                            fos.write((buffer[i].toInt() shr 8) and 0xFF)
                        }
                        val rms = sqrt(sum / read) / Short.MAX_VALUE
                        val normalized = rms.toFloat().coerceIn(0f, 1f)
                        _inputLevel.value = normalized

                        // dB scale: 20 * log10(rms)
                        val db = if (rms > 0.0001) (20 * log10(rms)).toFloat().coerceIn(-60f, 0f) else -60f
                        _inputDb.value = db
                    }
                }

                fos.flush()
                fos.close()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cleanUp()
            }
        }
    }

    fun stopRecording() {
        _isRecording.value = false
        recordJob?.cancel()
        recordJob = null
        cleanUp()
    }

    private fun cleanUp() {
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioRecord = null
        _inputLevel.value = 0f
        _inputDb.value = -60f
    }
}
