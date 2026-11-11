package com.example

import com.example.audio.AudioPlayer
import com.example.audio.AudioUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testPcmToWavHeader() {
        val pcm = ShortArray(100) { (it * 10).toShort() }
        val wavBytes = AudioPlayer.pcmToWav(pcm, 44100)
        assertNotNull(wavBytes)
        // 44-byte RIFF/WAVE header + 200 bytes PCM = 244 bytes
        assertEquals(244, wavBytes.size)
        assertEquals('R'.code.toByte(), wavBytes[0])
        assertEquals('I'.code.toByte(), wavBytes[1])
        assertEquals('F'.code.toByte(), wavBytes[2])
        assertEquals('F'.code.toByte(), wavBytes[3])
        assertEquals('W'.code.toByte(), wavBytes[8])
        assertEquals('A'.code.toByte(), wavBytes[9])
        assertEquals('V'.code.toByte(), wavBytes[10])
        assertEquals('E'.code.toByte(), wavBytes[11])
    }

    @Test
    fun testTimecodeFormatting() {
        assertEquals("00:00.00", AudioUtils.formatTimecode(0f))
        assertEquals("01:15.50", AudioUtils.formatTimecode(75.5f))
        assertEquals("0:05", AudioUtils.formatRulerTime(5))
        assertEquals("1:00", AudioUtils.formatRulerTime(60))
    }
}
