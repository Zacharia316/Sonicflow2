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
 * Robust microphone recorder for Studio Vocal Booth.
 * Supports hardware sample rate probing, live VU metering, and automatic WAV formatting.
 */
class AudioRecorder(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var recordJob: Job? = null
    private var audioRecord: AudioRecord? = null

    var onError: ((String) -> Unit)? = null
    var onStatus: ((String) -> Unit)? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _inputLevel = MutableStateFlow(0f)
    val inputLevel: StateFlow<Float> = _inputLevel.asStateFlow()

    private val _inputDb = MutableStateFlow(-60f)
    val inputDb: StateFlow<Float> = _inputDb.asStateFlow()

    var lastRecordedFile: File? = null
        private set

    private var activeSampleRate = 44100

    @SuppressLint("MissingPermission")
    fun startRecording(onCountDown: (Int) -> Unit = {}, onFinishedCountDown: () -> Unit = {}) {
        if (_isRecording.value) return

        recordJob = scope.launch {
            try {
                // Count-in: 3, 2, 1
                for (count in 3 downTo 1) {
                    onCountDown(count)
                    onStatus?.invoke("Recording in $count...")
                    kotlinx.coroutines.delay(600)
                }
                onFinishedCountDown()

                // Probe supported sample rates on hardware (44100, 48000, 16000)
                val candidateRates = intArrayOf(44100, 48000, 16000)
                var recordInstance: AudioRecord? = null
                var chosenRate = 44100
                var bufferSize = 4096

                for (rate in candidateRates) {
                    val minBuf = AudioRecord.getMinBufferSize(
                        rate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    )
                    if (minBuf > 0) {
                        try {
                            val candidateBufSize = maxOf(minBuf * 2, 4096)
                            val candidate = AudioRecord(
                                MediaRecorder.AudioSource.MIC,
                                rate,
                                AudioFormat.CHANNEL_IN_MONO,
                                AudioFormat.ENCODING_PCM_16BIT,
                                candidateBufSize
                            )
                            if (candidate.state == AudioRecord.STATE_INITIALIZED) {
                                recordInstance = candidate
                                chosenRate = rate
                                bufferSize = candidateBufSize
                                break
                            } else {
                                candidate.release()
                            }
                        } catch (_: Exception) {}
                    }
                }

                if (recordInstance == null) {
                    _isRecording.value = false
                    onError?.invoke("Microphone failed to initialize. Please check app permissions in Android Settings.")
                    return@launch
                }

                activeSampleRate = chosenRate
                audioRecord = recordInstance
                audioRecord?.startRecording()
                _isRecording.value = true
                onStatus?.invoke("Vocal Recording Active ($chosenRate Hz)")

                val pcmFile = File(context.cacheDir, "temp_vocal_${System.currentTimeMillis()}.pcm")
                val fos = FileOutputStream(pcmFile)
                val buffer = ShortArray(1024)
                var totalBytesWritten = 0

                while (isActive && _isRecording.value) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        var sum = 0.0
                        for (i in 0 until read) {
                            sum += buffer[i] * buffer[i]
                            val s = buffer[i].toInt()
                            fos.write(s and 0xFF)
                            fos.write((s shr 8) and 0xFF)
                            totalBytesWritten += 2
                        }
                        val rms = sqrt(sum / read) / Short.MAX_VALUE
                        val normalized = rms.toFloat().coerceIn(0f, 1f)
                        _inputLevel.value = normalized

                        val db = if (rms > 0.0001) (20 * log10(rms)).toFloat().coerceIn(-60f, 0f) else -60f
                        _inputDb.value = db
                    }
                }

                fos.flush()
                fos.close()

                // Convert raw PCM to a proper WAV file with header
                if (pcmFile.exists() && totalBytesWritten > 0) {
                    val pcmBytes = pcmFile.readBytes()
                    val shortCount = pcmBytes.size / 2
                    val shorts = ShortArray(shortCount)
                    for (i in 0 until shortCount) {
                        val low = pcmBytes[i * 2].toInt() and 0xFF
                        val high = pcmBytes[i * 2 + 1].toInt()
                        shorts[i] = ((high shl 8) or low).toShort()
                    }
                    val wavBytes = AudioPlayer.pcmToWav(shorts, chosenRate)
                    val finalWavFile = File(context.cacheDir, "vocal_take_${System.currentTimeMillis()}.wav")
                    FileOutputStream(finalWavFile).use { it.write(wavBytes) }
                    lastRecordedFile = finalWavFile
                    pcmFile.delete()
                    onStatus?.invoke("Vocal Take Saved (${finalWavFile.length() / 1024} KB)")
                }
            } catch (e: SecurityException) {
                _isRecording.value = false
                onError?.invoke("Microphone permission denied. Grant permission to record.")
            } catch (e: Exception) {
                _isRecording.value = false
                onError?.invoke("Recording error: ${e.message}")
            } finally {
                cleanUp()
            }
        }
    }

    fun stopRecording() {
        if (_isRecording.value) {
            _isRecording.value = false
            onStatus?.invoke("Recording Stopped. Processing audio...")
        }
        recordJob?.cancel()
        recordJob = null
        cleanUp()
    }

    private fun cleanUp() {
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
        _inputLevel.value = 0f
        _inputDb.value = -60f
    }
}
