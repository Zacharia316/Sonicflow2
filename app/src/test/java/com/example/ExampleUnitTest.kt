package com.example

import com.example.audio.AudioSynthesizer
import com.example.audio.DrumType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testAudioSynthesizer_pianoNoteGeneration() {
        val noteBuffer = AudioSynthesizer.generatePianoNote(60, durationSeconds = 0.5f)
        assertNotNull(noteBuffer)
        assertTrue(noteBuffer.isNotEmpty())
        assertEquals((AudioSynthesizer.SAMPLE_RATE * 0.5f).toInt(), noteBuffer.size)
    }

    @Test
    fun testAudioSynthesizer_drumHitsGeneration() {
        for (drum in DrumType.values()) {
            val buf = AudioSynthesizer.generateDrum(drum)
            assertNotNull(buf)
            assertTrue("Drum ${drum.name} should generate valid PCM audio", buf.isNotEmpty())
        }
    }

    @Test
    fun testMidiNoteToFreq() {
        // A4 = 440Hz
        val freqA4 = AudioSynthesizer.midiNoteToFreq(69)
        assertEquals(440.0f, freqA4, 0.5f)

        // C4 = ~261.63Hz
        val freqC4 = AudioSynthesizer.midiNoteToFreq(60)
        assertEquals(261.63f, freqC4, 0.5f)
    }
}
