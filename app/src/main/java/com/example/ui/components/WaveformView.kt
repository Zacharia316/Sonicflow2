package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.StudioWaveform
import com.example.ui.theme.StudioWaveformDim
import kotlin.math.sin

/**
 * High-performance Canvas waveform visualizer.
 * Optimized for low-end GPUs (Mali-400) by drawing rounded vertical bars directly without expensive shaders.
 */
@Composable
fun WaveformView(
    progress: Float, // 0.0f to 1.0f
    modifier: Modifier = Modifier,
    height: Dp = 36.dp,
    barCount: Int = 42,
    activeColor: Color = StudioWaveform,
    inactiveColor: Color = StudioWaveformDim,
    isPlaying: Boolean = false,
    seed: Int = 42,
    onSeek: ((Float) -> Unit)? = null
) {
    // Pre-calculate deterministic bar heights using musical harmonic curves
    val barHeights = remember(barCount, seed) {
        FloatArray(barCount) { index ->
            val norm = index.toFloat() / barCount
            val h1 = (sin(norm * Math.PI * 3.5 + seed * 0.1) * 0.4 + 0.5).toFloat()
            val h2 = (sin(norm * Math.PI * 7.2) * 0.25).toFloat()
            val h3 = (Math.random() * 0.15).toFloat()
            (h1 + h2 + h3).coerceIn(0.15f, 0.95f)
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .then(
                if (onSeek != null) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val seekPos = (offset.x / size.width).coerceIn(0f, 1f)
                            onSeek(seekPos)
                        }
                    }
                } else Modifier
            )
    ) {
        val totalWidth = size.width
        val barWidth = (totalWidth / (barCount * 1.55f)).coerceAtLeast(2f)
        val gap = (totalWidth - (barWidth * barCount)) / (barCount - 1)
        val centerY = size.height / 2f

        for (i in 0 until barCount) {
            val x = i * (barWidth + gap)
            val barNorm = i.toFloat() / barCount
            val isPastPlayhead = barNorm <= progress

            val barH = barHeights[i] * size.height * 0.88f
            val top = centerY - (barH / 2f)

            val color = if (isPastPlayhead) activeColor else inactiveColor

            drawRoundRect(
                color = color,
                topLeft = Offset(x, top),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }

        // Draw playhead scrubber marker
        if (progress in 0f..1f) {
            val playheadX = progress * totalWidth
            drawLine(
                color = activeColor,
                start = Offset(playheadX, 0f),
                end = Offset(playheadX, size.height),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}
