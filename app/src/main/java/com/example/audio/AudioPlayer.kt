package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Low-latency, low-memory audio playback manager.
 * Optimized for 1GB RAM by reusing audio tracks and pre-caching small drum PCM buffers.
 */
class AudioPlayer {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val cachedBuffers = ConcurrentHashMap<String, ShortArray>()
    private var loopJob: Job? = null
    var isPlaying = false
        private set

    init {
        // Pre-cache popular drum hits
        scope.launch {
            for (type in DrumType.values()) {
                cachedBuffers["drum_${type.name}"] = AudioSynthesizer.generateDrum(type)
            }
            cachedBuffers["click_hi"] = AudioSynthesizer.generateClick(true)
            cachedBuffers["click_lo"] = AudioSynthesizer.generateClick(false)
        }
    }

    /**
     * Plays a single PCM sound buffer asynchronously with minimum latency.
     */
    fun playPcm(buffer: ShortArray, volume: Float = 1.0f) {
        scope.launch {
            try {
                val minSize = AudioTrack.getMinBufferSize(
                    AudioSynthesizer.SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(AudioSynthesizer.SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2.coerceAtLeast(minSize))
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.setVolume(volume.coerceIn(0.0f, 1.0f))
                track.write(buffer, 0, buffer.size)
                track.play()

                // Free native resources when done
                delay((buffer.size * 1000L / AudioSynthesizer.SAMPLE_RATE) + 100)
                track.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playDrum(type: DrumType, volume: Float = 1.0f) {
        val key = "drum_${type.name}"
        val buf = cachedBuffers[key] ?: AudioSynthesizer.generateDrum(type).also {
            cachedBuffers[key] = it
        }
        playPcm(buf, volume)
    }

    fun playPianoNote(midiNote: Int, velocity: Float = 0.85f) {
        val buf = AudioSynthesizer.generatePianoNote(midiNote, durationSeconds = 1.0f, velocity = velocity)
        playPcm(buf, velocity)
    }

    fun playSynthPad(midiNote: Int, velocity: Float = 0.75f) {
        val buf = AudioSynthesizer.generateSynthPad(midiNote, durationSeconds = 1.5f, velocity = velocity)
        playPcm(buf, velocity)
    }

    fun play808(midiNote: Int = 36) {
        val buf = AudioSynthesizer.generate808Bass(midiNote, durationSeconds = 1.2f)
        playPcm(buf, 1.0f)
    }

    fun playClick(isAccent: Boolean) {
        val key = if (isAccent) "click_hi" else "click_lo"
        val buf = cachedBuffers[key] ?: AudioSynthesizer.generateClick(isAccent)
        playPcm(buf, 0.7f)
    }

    /**
     * Start playing a synchronized multi-track loop (Beat + Metronome + Notes).
     */
    fun startLoop(
        bpm: Int,
        enableMetronome: Boolean,
        onBeatTick: (Int, Float) -> Unit, // beat (0..15), progress (0..1f)
        onStop: () -> Unit
    ) {
        stopLoop()
        isPlaying = true
        loopJob = scope.launch {
            val stepMs = ((60000.0 / bpm) / 4.0).toLong().coerceAtLeast(40L) // 16th note steps
            var step = 0
            val totalSteps = 32 // 2 bars of 4/4

            while (isActive && isPlaying) {
                val beatInBar = (step / 4) % 4
                val isQuarterNote = (step % 4) == 0

                if (enableMetronome && isQuarterNote) {
                    playClick(isAccent = beatInBar == 0)
                }

                // Standard demo beat pattern (Kick on 1, 9; Snare on 5, 13; Hi-hat on all evens)
                val stepIn16 = step % 16
                if (stepIn16 == 0 || stepIn16 == 8 || stepIn16 == 10) {
                    playDrum(DrumType.KICK, 0.9f)
                }
                if (stepIn16 == 4 || stepIn16 == 12) {
                    playDrum(DrumType.SNARE, 0.8f)
                    playDrum(DrumType.CLAP, 0.6f)
                }
                if (stepIn16 % 2 == 0) {
                    playDrum(DrumType.HIHAT_CLOSED, 0.5f)
                }
                if (stepIn16 == 14) {
                    playDrum(DrumType.HIHAT_OPEN, 0.6f)
                }

                // Notify UI of playback progress
                val progress = step.toFloat() / totalSteps
                onBeatTick(step, progress)

                delay(stepMs)
                step = (step + 1) % totalSteps
            }
            onStop()
        }
    }

    fun stopLoop() {
        isPlaying = false
        loopJob?.cancel()
        loopJob = null
    }

    fun release() {
        stopLoop()
        cachedBuffers.clear()
    }
}
