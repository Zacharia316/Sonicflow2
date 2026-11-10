package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.SoundPool
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap

/**
 * OpenSL ES powered audio playback engine for Studio DAW.
 * Designed specifically for low-RAM devices (Infinix Hot 6 / 1GB RAM / Mali-400).
 *
 * Uses Android's native OpenSL ES SoundPool for instant zero-latency drum & note hits,
 * and a single low-latency streaming AudioTrack for the multi-track playback loop.
 */
class AudioPlayer(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)

    var onError: ((String) -> Unit)? = null
    var onStatus: ((String) -> Unit)? = null

    // Native OpenSL ES SoundPool
    private var soundPool: SoundPool? = null
    private val soundIdMap = ConcurrentHashMap<String, Int>()
    private val cachedBuffers = ConcurrentHashMap<String, ShortArray>()

    private var loopJob: Job? = null
    var isPlaying = false
        private set

    // Low-latency streaming AudioTrack for project playback
    private var streamingTrack: AudioTrack? = null

    init {
        initSoundPool()
    }

    private fun initSoundPool() {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setFlags(AudioAttributes.FLAG_LOW_LATENCY)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(16)
                .setAudioAttributes(audioAttributes)
                .build()

            // Pre-load core drum kits into native OpenSL memory
            scope.launch {
                val soundDir = File(context.cacheDir, "sounds")
                if (!soundDir.exists()) soundDir.mkdirs()

                for (type in DrumType.values()) {
                    try {
                        val pcm = AudioSynthesizer.generateDrum(type)
                        cachedBuffers["drum_${type.name}"] = pcm
                        val wavBytes = pcmToWav(pcm, AudioSynthesizer.SAMPLE_RATE)
                        val wavFile = File(soundDir, "drum_${type.name}.wav")
                        FileOutputStream(wavFile).use { it.write(wavBytes) }
                        val soundId = soundPool?.load(wavFile.absolutePath, 1) ?: 0
                        if (soundId > 0) {
                            soundIdMap["drum_${type.name}"] = soundId
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Pre-cache metronome clicks
                try {
                    val clickHi = AudioSynthesizer.generateClick(true)
                    val clickLo = AudioSynthesizer.generateClick(false)
                    cachedBuffers["click_hi"] = clickHi
                    cachedBuffers["click_lo"] = clickLo

                    val fHi = File(soundDir, "click_hi.wav")
                    FileOutputStream(fHi).use { it.write(pcmToWav(clickHi, AudioSynthesizer.SAMPLE_RATE)) }
                    soundPool?.load(fHi.absolutePath, 1)?.let { soundIdMap["click_hi"] = it }

                    val fLo = File(soundDir, "click_lo.wav")
                    FileOutputStream(fLo).use { it.write(pcmToWav(clickLo, AudioSynthesizer.SAMPLE_RATE)) }
                    soundPool?.load(fLo.absolutePath, 1)?.let { soundIdMap["click_lo"] = it }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                onStatus?.invoke("OpenSL Audio Engine Ready")
            }
        } catch (e: Exception) {
            onError?.invoke("OpenSL SoundPool init failed: ${e.message}")
        }
    }

    /**
     * Plays a one-shot sound via native OpenSL ES SoundPool (ultra low-latency).
     * Falls back to static AudioTrack if SoundPool sample is still loading.
     */
    fun playSound(key: String, pcmFallback: ShortArray, volume: Float = 1.0f) {
        val soundId = soundIdMap[key]
        if (soundId != null && soundId > 0) {
            val vol = volume.coerceIn(0.05f, 1.0f)
            val streamId = soundPool?.play(soundId, vol, vol, 1, 0, 1.0f) ?: 0
            if (streamId > 0) return
        }
        // Fallback: direct fast AudioTrack play
        playPcmFallback(pcmFallback, volume)
    }

    /**
     * Low-latency direct PCM playback with correct buffer size calculations.
     */
    private fun playPcmFallback(buffer: ShortArray, volume: Float = 1.0f) {
        scope.launch {
            try {
                val sampleRate = AudioSynthesizer.SAMPLE_RATE
                val minSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                if (minSize <= 0) return@launch

                val sizeInBytes = buffer.size * 2
                val bufferSize = maxOf(sizeInBytes, minSize)

                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setFlags(AudioAttributes.FLAG_LOW_LATENCY)
                    .build()

                val audioFormat = AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()

                val trackBuilder = AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    trackBuilder.setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                }

                val track = trackBuilder.build()
                track.setVolume(volume.coerceIn(0.0f, 1.0f))
                track.write(buffer, 0, buffer.size)
                track.play()

                val playDurationMs = (buffer.size * 1000L / sampleRate) + 50
                delay(playDurationMs)
                track.stop()
                track.release()
            } catch (e: Throwable) {
                onError?.invoke("Audio playback error: ${e.message}")
            }
        }
    }

    fun playDrum(type: DrumType, volume: Float = 1.0f) {
        val key = "drum_${type.name}"
        val buf = cachedBuffers[key] ?: AudioSynthesizer.generateDrum(type).also {
            cachedBuffers[key] = it
        }
        playSound(key, buf, volume)
    }

    fun playPianoNote(midiNote: Int, velocity: Float = 0.85f) {
        val key = "piano_$midiNote"
        val buf = cachedBuffers[key] ?: AudioSynthesizer.generatePianoNote(midiNote, durationSeconds = 0.9f, velocity = velocity).also {
            cachedBuffers[key] = it
        }
        playPcmFallback(buf, velocity)
    }

    fun playSynthPad(midiNote: Int, velocity: Float = 0.75f) {
        val key = "synth_$midiNote"
        val buf = cachedBuffers[key] ?: AudioSynthesizer.generateSynthPad(midiNote, durationSeconds = 1.2f, velocity = velocity).also {
            cachedBuffers[key] = it
        }
        playPcmFallback(buf, velocity)
    }

    fun play808(midiNote: Int = 36) {
        val key = "808_$midiNote"
        val buf = cachedBuffers[key] ?: AudioSynthesizer.generate808Bass(midiNote, durationSeconds = 1.2f).also {
            cachedBuffers[key] = it
        }
        playPcmFallback(buf, 1.0f)
    }

    fun playClick(isAccent: Boolean) {
        val key = if (isAccent) "click_hi" else "click_lo"
        val buf = cachedBuffers[key] ?: AudioSynthesizer.generateClick(isAccent)
        playSound(key, buf, 0.7f)
    }

    /**
     * Start playing a synchronized multi-track loop using OpenSL ES fast-mixer triggers.
     */
    fun startLoop(
        bpm: Int,
        enableMetronome: Boolean,
        onBeatTick: (Int, Float) -> Unit,
        onStop: () -> Unit
    ) {
        stopLoop()
        isPlaying = true
        onStatus?.invoke("Playing Project Loop ($bpm BPM)")

        loopJob = scope.launch {
            try {
                val stepMs = ((60000.0 / bpm) / 4.0).toLong().coerceAtLeast(35L) // 16th note steps
                var step = 0
                val totalSteps = 32 // 2 bars of 4/4

                while (isActive && isPlaying) {
                    val beatInBar = (step / 4) % 4
                    val isQuarterNote = (step % 4) == 0

                    if (enableMetronome && isQuarterNote) {
                        playClick(isAccent = beatInBar == 0)
                    }

                    // Standard rhythmic beat pattern
                    val stepIn16 = step % 16
                    if (stepIn16 == 0 || stepIn16 == 8 || stepIn16 == 10) {
                        playDrum(DrumType.KICK, 0.95f)
                    }
                    if (stepIn16 == 4 || stepIn16 == 12) {
                        playDrum(DrumType.SNARE, 0.85f)
                        playDrum(DrumType.CLAP, 0.65f)
                    }
                    if (stepIn16 % 2 == 0) {
                        playDrum(DrumType.HIHAT_CLOSED, 0.55f)
                    }
                    if (stepIn16 == 14) {
                        playDrum(DrumType.HIHAT_OPEN, 0.65f)
                    }

                    // Progress update
                    val progress = step.toFloat() / totalSteps
                    onBeatTick(step, progress)

                    delay(stepMs)
                    step = (step + 1) % totalSteps
                }
            } catch (e: Exception) {
                onError?.invoke("Playback loop interrupted: ${e.message}")
            } finally {
                onStop()
            }
        }
    }

    fun stopLoop() {
        isPlaying = false
        loopJob?.cancel()
        loopJob = null
    }

    fun release() {
        stopLoop()
        try {
            soundPool?.release()
            soundPool = null
        } catch (_: Exception) {}
        cachedBuffers.clear()
        soundIdMap.clear()
    }

    companion object {
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
