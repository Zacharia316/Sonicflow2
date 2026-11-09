package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioTextSecondary
import kotlin.math.cos
import kotlin.math.sin

/**
 * Neumorphic rotary control knob for PAN, Reverb, Delay, and Attack.
 * Allows touch drag (vertical or horizontal) to smoothly adjust parameters.
 */
@Composable
fun RotaryKnob(
    value: Float, // 0.0f to 1.0f (or -1.0f to 1.0f for Pan)
    onValueChange: (Float) -> Unit,
    label: String,
    valueLabel: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    range: ClosedFloatingPointRange<Float> = 0.0f..1.0f,
    indicatorColor: Color = StudioPrimary
) {
    var accumulatedDrag by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Canvas(
            modifier = Modifier
                .size(size)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val delta = -dragAmount.y * 0.005f
                        val newValue = (value + delta).coerceIn(range.start, range.endInclusive)
                        onValueChange(newValue)
                    }
                }
        ) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = (this.size.minDimension / 2f) - 6.dp.toPx()

            // Outer subtle track
            drawCircle(
                color = Color(0xFFDED0E2),
                radius = radius,
                center = center
            )

            // Inner knob face with soft bevel
            drawCircle(
                color = Color(0xFFEDE4EE),
                radius = radius * 0.88f,
                center = center
            )

            // Normalized angle (-135 deg to +135 deg = 270 deg range)
            val norm = (value - range.start) / (range.endInclusive - range.start)
            val angleDeg = -135f + (norm * 270f)
            val angleRad = Math.toRadians(angleDeg.toDouble())

            val innerRadius = radius * 0.25f
            val outerRadius = radius * 0.78f

            val startPoint = Offset(
                x = (center.x + innerRadius * cos(angleRad)).toFloat(),
                y = (center.y + innerRadius * sin(angleRad)).toFloat()
            )
            val endPoint = Offset(
                x = (center.x + outerRadius * cos(angleRad)).toFloat(),
                y = (center.y + outerRadius * sin(angleRad)).toFloat()
            )

            // Notch indicator line
            drawLine(
                color = indicatorColor,
                start = startPoint,
                end = endPoint,
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Outer indicator ring arc
            drawArc(
                color = indicatorColor.copy(alpha = 0.4f),
                startAngle = 135f,
                sweepAngle = (norm * 270f),
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Text(
            text = label,
            fontSize = 11.sp,
            color = StudioTextSecondary,
            fontWeight = FontWeight.Medium
        )

        if (valueLabel != null) {
            Text(
                text = valueLabel,
                fontSize = 10.sp,
                color = indicatorColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
