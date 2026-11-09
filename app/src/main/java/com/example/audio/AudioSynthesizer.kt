package com.example.audio

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * High-performance, low-RAM audio synthesizer engine.
 * Tailored for budget devices (1GB RAM, Mali-400) by avoiding heavy allocations,
 * using pure 16-bit PCM math, and pre-computed envelopes.
 */
object AudioSynthesizer {
    const val SAMPLE_RATE = 44100

    // Standard piano frequencies from A0 to C8
    fun midiNoteToFreq(midiNote: Int): Float {
        return (440.0 * Math.pow(2.0, (midiNote - 69).toDouble() / 12.0)).toFloat()
    }

    /**
     * Synthesizes Grand Piano note with physical hammer strike simulation,
     * dual detuned harmonics, and natural acoustic soundboard decay.
     */
    fun generatePianoNote(midiNote: Int, durationSeconds: Float = 1.2f, velocity: Float = 0.85f): ShortArray {
        val numSamples = (SAMPLE_RATE * durationSeconds).toInt()
        val buffer = ShortArray(numSamples)
        val freq = midiNoteToFreq(midiNote)
        val twoPi = 2.0 * PI

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // Natural ADSR decay
            val env = exp(-t * (2.8 + freq * 0.002)) * velocity
            // Piano harmonics (1st, 2nd, 3rd, 4th partials)
            val s1 = sin(twoPi * freq * t)
            val s2 = 0.45 * sin(twoPi * freq * 2.002 * t)
            val s3 = 0.25 * sin(twoPi * freq * 3.005 * t)
            val s4 = 0.12 * sin(twoPi * freq * 4.009 * t)
            // Hammer strike click transient
            val click = if (t < 0.008) (1.0 - t / 0.008) * 0.35 * (Math.random() - 0.5) else 0.0

            val sample = (s1 + s2 + s3 + s4 + click) * env * 0.45
            buffer[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }

    /**
     * Synthesizes Ambient Synth Pad with dual detuned chorus oscillators and slow attack.
     */
    fun generateSynthPad(midiNote: Int, durationSeconds: Float = 2.0f, velocity: Float = 0.75f): ShortArray {
        val numSamples = (SAMPLE_RATE * durationSeconds).toInt()
        val buffer = ShortArray(numSamples)
        val freq = midiNoteToFreq(midiNote)
        val twoPi = 2.0 * PI
        val attackTime = 0.35
        val releaseTime = 0.45

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val timeLeft = durationSeconds - t
            val attackGain = if (t < attackTime) (t / attackTime) else 1.0
            val releaseGain = if (timeLeft < releaseTime) (timeLeft / releaseTime).coerceAtLeast(0.0) else 1.0
            val env = attackGain * releaseGain * velocity

            // Dual detuned lush chorus oscillators
            val osc1 = sin(twoPi * freq * t)
            val osc2 = sin(twoPi * (freq * 1.004) * t)
            val osc3 = 0.5 * sin(twoPi * (freq * 0.5) * t) // Warm sub undertone

            val sample = (osc1 * 0.4 + osc2 * 0.4 + osc3 * 0.2) * env * 0.6
            buffer[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }

    /**
     * Synthesizes deep 808 Sub Bass with pitch glide and subtle harmonic saturation.
     */
    fun generate808Bass(midiNote: Int = 36, durationSeconds: Float = 1.5f): ShortArray {
        val numSamples = (SAMPLE_RATE * durationSeconds).toInt()
        val buffer = ShortArray(numSamples)
        val baseFreq = midiNoteToFreq(midiNote)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // Pitch glide down at start
            val pitchEnv = if (t < 0.06) 1.0 + (1.0 - t / 0.06) * 1.2 else 1.0
            val currentFreq = baseFreq * pitchEnv
            phase += 2.0 * PI * currentFreq / SAMPLE_RATE

            val ampEnv = exp(-t * 2.2)
            var sample = sin(phase) * ampEnv
            // Soft tube saturation
            sample = Math.tanh(sample * 1.8) * 0.75
            buffer[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }

    /**
     * Drum Kit hits for the 16-pad drum machine and beat engine.
     */
    fun generateDrum(type: DrumType): ShortArray {
        return when (type) {
            DrumType.KICK -> {
                val samples = (SAMPLE_RATE * 0.38f).toInt()
                val buf = ShortArray(samples)
                var phase = 0.0
                for (i in 0 until samples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val freq = 140.0 * exp(-t * 38.0) + 42.0
                    phase += 2.0 * PI * freq / SAMPLE_RATE
                    val env = exp(-t * 12.0)
                    val click = if (t < 0.005) (1.0 - t / 0.005) * 0.4 else 0.0
                    val sample = (sin(phase) + click) * env * 0.95
                    buf[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
                }
                buf
            }
            DrumType.SNARE -> {
                val samples = (SAMPLE_RATE * 0.28f).toInt()
                val buf = ShortArray(samples)
                for (i in 0 until samples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val tone = sin(2.0 * PI * 185.0 * t) * exp(-t * 24.0)
                    val noise = (Math.random() * 2.0 - 1.0) * exp(-t * 16.0)
                    val sample = (tone * 0.4 + noise * 0.6) * 0.9
                    buf[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
                }
                buf
            }
            DrumType.HIHAT_CLOSED -> {
                val samples = (SAMPLE_RATE * 0.07f).toInt()
                val buf = ShortArray(samples)
                for (i in 0 until samples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val noise = (Math.random() * 2.0 - 1.0) * exp(-t * 60.0)
                    buf[i] = (noise.coerceIn(-1.0, 1.0) * 0.75 * Short.MAX_VALUE).toInt().toShort()
                }
                buf
            }
            DrumType.HIHAT_OPEN -> {
                val samples = (SAMPLE_RATE * 0.32f).toInt()
                val buf = ShortArray(samples)
                for (i in 0 until samples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val noise = (Math.random() * 2.0 - 1.0) * exp(-t * 14.0)
                    buf[i] = (noise.coerceIn(-1.0, 1.0) * 0.65 * Short.MAX_VALUE).toInt().toShort()
                }
                buf
            }
            DrumType.CLAP -> {
                val samples = (SAMPLE_RATE * 0.25f).toInt()
                val buf = ShortArray(samples)
                for (i in 0 until samples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    var env = 0.0
                    // Multi-burst clap spikes
                    if (t < 0.012) env += 0.8
                    if (t in 0.015..0.027) env += 0.7
                    if (t in 0.030..0.042) env += 0.9
                    if (t > 0.042) env += exp(-(t - 0.042) * 22.0)
                    val noise = (Math.random() * 2.0 - 1.0) * env * 0.75
                    buf[i] = (noise.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
                }
                buf
            }
            DrumType.CONGA_HIGH -> {
                val samples = (SAMPLE_RATE * 0.22f).toInt()
                val buf = ShortArray(samples)
                for (i in 0 until samples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val freq = 340.0 * exp(-t * 18.0) + 260.0
                    val sample = sin(2.0 * PI * freq * t) * exp(-t * 18.0) * 0.85
                    buf[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
                }
                buf
            }
            DrumType.CONGA_LOW -> {
                val samples = (SAMPLE_RATE * 0.32f).toInt()
                val buf = ShortArray(samples)
                for (i in 0 until samples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val freq = 190.0 * exp(-t * 14.0) + 120.0
                    val sample = sin(2.0 * PI * freq * t) * exp(-t * 12.0) * 0.9
                    buf[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
                }
                buf
            }
            DrumType.SHAKER -> {
                val samples = (SAMPLE_RATE * 0.12f).toInt()
                val buf = ShortArray(samples)
                for (i in 0 until samples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val noise = (Math.random() * 2.0 - 1.0) * sin(PI * t / 0.12) * 0.45
                    buf[i] = (noise.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
                }
                buf
            }
            DrumType.RIMSHOT -> {
                val samples = (SAMPLE_RATE * 0.10f).toInt()
                val buf = ShortArray(samples)
                for (i in 0 until samples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val sample = sin(2.0 * PI * 850.0 * t) * exp(-t * 45.0) * 0.8
                    buf[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
                }
                buf
            }
            DrumType.TOM -> {
                val samples = (SAMPLE_RATE * 0.35f).toInt()
                val buf = ShortArray(samples)
                for (i in 0 until samples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val freq = 160.0 * exp(-t * 12.0) + 75.0
                    val sample = sin(2.0 * PI * freq * t) * exp(-t * 9.0) * 0.85
                    buf[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
                }
                buf
            }
            DrumType.VOCAL_CHOP -> {
                val samples = (SAMPLE_RATE * 0.45f).toInt()
                val buf = ShortArray(samples)
                for (i in 0 until samples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val f = 440.0
                    // Formant vowel simulation
                    val formant = sin(2.0 * PI * f * t) * 0.6 + sin(2.0 * PI * (f * 2.2) * t) * 0.3
                    val env = exp(-t * 6.0)
                    buf[i] = (formant * env * 0.7 * Short.MAX_VALUE).toInt().toShort()
                }
                buf
            }
            DrumType.CRASH -> {
                val samples = (SAMPLE_RATE * 0.8f).toInt()
                val buf = ShortArray(samples)
                for (i in 0 until samples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val noise = (Math.random() * 2.0 - 1.0) * exp(-t * 5.0) * 0.65
                    buf[i] = (noise.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
                }
                buf
            }
        }
    }

    /**
     * Synthesizes metronome click for timing accuracy.
     */
    fun generateClick(isAccent: Boolean): ShortArray {
        val freq = if (isAccent) 2000.0 else 1000.0
        val samples = (SAMPLE_RATE * 0.025f).toInt()
        val buf = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toDouble() / SAMPLE_RATE
            val s = sin(2.0 * PI * freq * t) * exp(-t * 120.0)
            buf[i] = (s * Short.MAX_VALUE * 0.6).toInt().toShort()
        }
        return buf
    }
}

enum class DrumType(val displayName: String, val category: String) {
    KICK("Kick", "Drums"),
    SNARE("Snare", "Drums"),
    HIHAT_CLOSED("Closed Hat", "Drums"),
    HIHAT_OPEN("Open Hat", "Drums"),
    CLAP("Clap", "Drums"),
    RIMSHOT("Rimshot", "Perc"),
    CONGA_HIGH("Conga Hi", "Afro"),
    CONGA_LOW("Conga Low", "Afro"),
    SHAKER("Shaker", "Afro"),
    TOM("Tom", "Perc"),
    VOCAL_CHOP("Vocal Chop", "Vocals"),
    CRASH("Crash", "Cymbal")
}
