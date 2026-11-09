package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioPill
import com.example.ui.components.StudioScreenBackground
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.viewmodel.StudioScreen
import com.example.viewmodel.StudioViewModel

/**
 * Vocal FX Chain screen matching user reference Image 5.
 */
@Composable
fun VocalFxScreen(viewModel: StudioViewModel) {
    val presets = listOf("Clean", "Warm", "Airy", "Drill", "Afrobeats")
    val selectedPreset by viewModel.vocalFxPreset.collectAsState()

    val autoTuneOn by viewModel.autoTuneEnabled.collectAsState()
    val autoTuneAmount by viewModel.autoTuneAmount.collectAsState()
    val reverbOn by viewModel.vocalReverbEnabled.collectAsState()
    val reverbAmount by viewModel.vocalReverbAmount.collectAsState()
    val delayOn by viewModel.vocalDelayEnabled.collectAsState()
    val delayAmount by viewModel.vocalDelayAmount.collectAsState()
    val compOn by viewModel.compressorEnabled.collectAsState()
    val deEsserOn by viewModel.deEsserEnabled.collectAsState()
    val isBypassed by viewModel.isBypassed.collectAsState()

    StudioScreenBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header: < Vocal FX, Bypass, Save Preset
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.clickable { viewModel.navigateBack() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = StudioTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Vocal FX",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isBypassed) StudioPrimary else StudioCardBg)
                            .clickable { viewModel.isBypassed.value = !isBypassed }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Bypass",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isBypassed) Color.White else StudioTextPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(StudioCardBg)
                            .clickable { /* Save preset */ }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Save Preset", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StudioTextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Presets Horizontal Row: Clean, Warm, Airy, Drill, Afrobeats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                presets.forEach { preset ->
                    StudioPill(
                        text = preset,
                        isSelected = selectedPreset == preset,
                        onClick = { viewModel.vocalFxPreset.value = preset }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scrollable Effects Chain
            Text(
                text = "EFFECTS / VOCAL CHAIN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = StudioTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Auto-Tune Module
                StudioCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Switch(
                                    checked = autoTuneOn,
                                    onCheckedChange = { viewModel.autoTuneEnabled.value = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = StudioPrimary),
                                    modifier = Modifier.scale(0.8f)
                                )
                                Text("Auto-Tune", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StudioTextPrimary)
                            }
                            Text(
                                text = if (autoTuneOn) "ON" else "OFF",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioPrimary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Key C Major", fontSize = 12.sp, color = StudioTextSecondary)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFEDE4EE))
                                    .clickable { viewModel.navigateTo(StudioScreen.AUTO_TUNE) }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("C Major >", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StudioTextPrimary)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Amount", fontSize = 11.sp, color = StudioTextSecondary)
                            Slider(
                                value = autoTuneAmount,
                                onValueChange = { viewModel.autoTuneAmount.value = it },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(thumbColor = StudioPrimary, activeTrackColor = StudioPrimary),
                                modifier = Modifier.weight(1f)
                            )
                            Text("${autoTuneAmount.toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StudioPrimary)
                        }
                    }
                }

                // Reverb Module
                StudioCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Switch(
                                    checked = reverbOn,
                                    onCheckedChange = { viewModel.vocalReverbEnabled.value = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = StudioPrimary),
                                    modifier = Modifier.scale(0.8f)
                                )
                                Text("Reverb", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StudioTextPrimary)
                            }
                            Text("${reverbAmount.toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StudioPrimary)
                        }

                        Slider(
                            value = reverbAmount,
                            onValueChange = { viewModel.vocalReverbAmount.value = it },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(thumbColor = StudioPrimary, activeTrackColor = StudioPrimary)
                        )
                    }
                }

                // Delay Module
                StudioCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Switch(
                                    checked = delayOn,
                                    onCheckedChange = { viewModel.vocalDelayEnabled.value = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = StudioPrimary),
                                    modifier = Modifier.scale(0.8f)
                                )
                                Text("Delay", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StudioTextPrimary)
                            }
                            Text("${delayAmount.toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StudioPrimary)
                        }

                        Slider(
                            value = delayAmount,
                            onValueChange = { viewModel.vocalDelayAmount.value = it },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(thumbColor = StudioPrimary, activeTrackColor = StudioPrimary)
                        )
                    }
                }

                // Graphic EQ Spectrum Curve Module (Image 5)
                StudioCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Parametric Equalizer", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StudioTextPrimary)

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                        ) {
                            val w = size.width
                            val h = size.height

                            // Draw subtle grid lines
                            drawLine(Color(0xFFEADBEE), Offset(0f, h / 2f), Offset(w, h / 2f), strokeWidth = 1f)

                            // Draw interactive EQ parametric curve
                            val path = Path().apply {
                                moveTo(0f, h * 0.7f)
                                lineTo(w * 0.2f, h * 0.68f)
                                lineTo(w * 0.45f, h * 0.38f) // Mid peak
                                cubicTo(w * 0.6f, h * 0.42f, w * 0.75f, h * 0.62f, w, h * 0.65f)
                            }
                            drawPath(path, StudioPrimary, style = Stroke(width = 2.dp.toPx()))

                            // Draw subtle shaded area under curve
                            val fillPath = Path().apply {
                                addPath(path)
                                lineTo(w, h)
                                lineTo(0f, h)
                                close()
                            }
                            drawPath(fillPath, StudioPrimary.copy(alpha = 0.08f))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("60Hz", fontSize = 9.sp, color = StudioTextSecondary)
                            Text("300Hz", fontSize = 9.sp, color = StudioTextSecondary)
                            Text("1kHz", fontSize = 9.sp, color = StudioTextSecondary)
                            Text("3kHz", fontSize = 9.sp, color = StudioTextSecondary)
                            Text("8kHz", fontSize = 9.sp, color = StudioTextSecondary)
                        }
                    }
                }

                // Compressor Module
                StudioCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Switch(
                                checked = compOn,
                                onCheckedChange = { viewModel.compressorEnabled.value = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = StudioPrimary),
                                modifier = Modifier.scale(0.8f)
                            )
                            Column {
                                Text("Compressor", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StudioTextPrimary)
                                Text("Ratio 4:1 • Threshold -14dB", fontSize = 11.sp, color = StudioTextSecondary)
                            }
                        }
                    }
                }

                // De-esser Module
                StudioCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Switch(
                                checked = deEsserOn,
                                onCheckedChange = { viewModel.deEsserEnabled.value = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = StudioPrimary),
                                modifier = Modifier.scale(0.8f)
                            )
                            Column {
                                Text("De-esser", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StudioTextPrimary)
                                Text("Frequency 4.5kHz • Amount 35%", fontSize = 11.sp, color = StudioTextSecondary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Actions (Image 5): Bypass All, Save Preset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEDE4EE))
                        .clickable { viewModel.isBypassed.value = !isBypassed }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Bypass All", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = StudioTextPrimary)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(StudioPrimary)
                        .clickable { /* Save preset */ }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Save Preset", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
