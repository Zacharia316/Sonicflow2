package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioMeterGreen
import com.example.ui.theme.StudioMeterRed
import com.example.ui.theme.StudioMeterYellow
import com.example.ui.theme.StudioPillInactive
import com.example.ui.theme.StudioPillInactiveText
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary

/**
 * Studio channel strip mixer fader with volume slider, dB readout, pan, Mute, Solo, and LED meter.
 */
@Composable
fun ChannelStrip(
    trackName: String,
    volume: Float, // 0.0 to 1.0 (maps to -inf dB to +3dB)
    onVolumeChange: (Float) -> Unit,
    pan: Float,    // -1.0 (Left) to +1.0 (Right)
    onPanChange: (Float) -> Unit,
    isMuted: Boolean,
    onMuteToggle: () -> Unit,
    isSolo: Boolean,
    onSoloToggle: () -> Unit,
    levelMeter: Float, // 0.0 to 1.0
    dotColor: Color = Color(0xFF9E8CA8),
    modifier: Modifier = Modifier
) {
    StudioCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Track label with colored status dot
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.width(90.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Text(
                    text = trackName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioTextPrimary
                )
            }

            // Rotary Pan
            val panLabel = when {
                pan < -0.05f -> "PAN L${(-pan * 100).toInt()}"
                pan > 0.05f -> "PAN R${(pan * 100).toInt()}"
                else -> "PAN C"
            }
            RotaryKnob(
                value = pan,
                onValueChange = onPanChange,
                label = panLabel,
                size = 38.dp,
                range = -1.0f..1.0f
            )

            // Volume Fader & dB text
            val db = if (volume > 0.01f) {
                val dbVal = (20 * Math.log10(volume.toDouble())).toFloat()
                if (dbVal >= 0f) "+%.1fdB".format(dbVal) else "%.1fdB".format(dbVal)
            } else "-inf dB"

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Vertical Fader slot
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(54.dp)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val delta = -dragAmount.y / 54.dp.toPx()
                                onVolumeChange((volume + delta).coerceIn(0f, 1f))
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(width = 24.dp, height = 50.dp)) {
                        val trackW = 4.dp.toPx()
                        val trackX = (size.width - trackW) / 2f
                        // Track slot
                        drawRoundRect(
                            color = Color(0xFFDED0E2),
                            topLeft = Offset(trackX, 0f),
                            size = Size(trackW, size.height),
                            cornerRadius = CornerRadius(2.dp.toPx())
                        )
                        // Fader handle thumb
                        val thumbY = (1f - volume) * (size.height - 14.dp.toPx())
                        val thumbW = 20.dp.toPx()
                        val thumbH = 12.dp.toPx()
                        val thumbX = (size.width - thumbW) / 2f

                        // Handle body
                        drawRoundRect(
                            color = StudioPrimary,
                            topLeft = Offset(thumbX, thumbY),
                            size = Size(thumbW, thumbH),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                        // Center grip notch
                        drawLine(
                            color = Color.White,
                            start = Offset(thumbX + 4.dp.toPx(), thumbY + thumbH / 2f),
                            end = Offset(thumbX + thumbW - 4.dp.toPx(), thumbY + thumbH / 2f),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                }

                Text(
                    text = "VOL $db",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = StudioTextSecondary
                )
            }

            // Mute & Solo buttons (Image 9 style)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isMuted) Color(0xFF9E4B5E) else StudioPillInactive)
                        .clickable(onClick = onMuteToggle),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "M",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMuted) Color.White else StudioPillInactiveText
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSolo) StudioPrimary else StudioPillInactive)
                        .clickable(onClick = onSoloToggle),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSolo) Color.White else StudioPillInactiveText
                    )
                }
            }

            // Segmented LED VU Meter
            Column(
                modifier = Modifier
                    .width(10.dp)
                    .height(46.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                val segments = 8
                for (s in segments downTo 1) {
                    val threshold = s.toFloat() / segments
                    val isActive = levelMeter >= threshold && !isMuted
                    val color = when {
                        s >= 7 -> if (isActive) StudioMeterRed else StudioMeterRed.copy(alpha = 0.2f)
                        s >= 5 -> if (isActive) StudioMeterYellow else StudioMeterYellow.copy(alpha = 0.2f)
                        else -> if (isActive) StudioMeterGreen else StudioMeterGreen.copy(alpha = 0.2f)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(color)
                    )
                }
            }
        }
    }
}
