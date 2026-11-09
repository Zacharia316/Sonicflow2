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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioPill
import com.example.ui.components.StudioScreenBackground
import com.example.ui.theme.StudioMeterGreen
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.viewmodel.StudioViewModel

val voicePresetsList = listOf(
    "Clean", "Warm",
    "Airy", "Drill",
    "Afrobeats", "R&B",
    "Amapiano", "Trap Soul"
)

val musicalKeys = listOf(
    "C Major", "C Minor", "D Major", "D Minor",
    "E Major", "E Minor", "F Major", "F Minor",
    "G Major", "G Minor", "A Major", "A Minor",
    "B Major", "B Minor"
)

/**
 * Auto-Tune & Voice screen matching user reference Image 8.
 */
@Composable
fun AutoTuneScreen(viewModel: StudioViewModel) {
    val autoTuneKey by viewModel.autoTuneKey.collectAsState()
    val retuneSpeed by viewModel.retuneSpeed.collectAsState()
    val humanize by viewModel.humanize.collectAsState()
    val flexTune by viewModel.flexTune.collectAsState()
    val selectedVoicePreset by viewModel.voicePreset.collectAsState()
    val livePitchCent by viewModel.livePitchCent.collectAsState()
    val isBypassed by viewModel.isBypassed.collectAsState()

    var isMajor by remember { mutableStateOf(true) }
    var showKeyPicker by remember { mutableStateOf(false) }

    StudioScreenBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header with Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.6f))
                        .clickable { viewModel.navigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = StudioTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "Auto-Tune & Voice",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioTextPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // KEY & TUNING Card (Image 8)
                StudioCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "KEY & TUNING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = StudioTextSecondary
                        )

                        // Key Dropdown Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEDE4EE))
                                .clickable { showKeyPicker = !showKeyPicker }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Key $autoTuneKey",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StudioTextPrimary
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Select Key",
                                    tint = StudioTextSecondary
                                )
                            }
                        }

                        // Major / Minor toggle pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                                    .background(if (isMajor) Color(0xFFC7B1D0) else Color(0xFFEDE4EE))
                                    .clickable { isMajor = true }
                                    .padding(horizontal = 24.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Major", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isMajor) Color.White else StudioTextPrimary)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                                    .background(if (!isMajor) Color(0xFFC7B1D0) else Color(0xFFEDE4EE))
                                    .clickable { isMajor = false }
                                    .padding(horizontal = 24.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Minor", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (!isMajor) Color.White else StudioTextPrimary)
                            }
                        }

                        // Retune Speed Slider (Image 8: 45%)
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Retune Speed", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = StudioTextPrimary)
                                Text("${retuneSpeed.toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StudioPrimary)
                            }
                            Slider(
                                value = retuneSpeed,
                                onValueChange = { viewModel.retuneSpeed.value = it },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(thumbColor = StudioPrimary, activeTrackColor = StudioPrimary)
                            )
                        }

                        // Humanize Slider (Image 8: 22%)
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Humanize", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = StudioTextPrimary)
                                Text("${humanize.toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StudioPrimary)
                            }
                            Slider(
                                value = humanize,
                                onValueChange = { viewModel.humanize.value = it },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(thumbColor = StudioPrimary, activeTrackColor = StudioPrimary)
                            )
                        }

                        // Flex-Tune Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Flex-Tune", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = StudioTextPrimary)
                            Switch(
                                checked = flexTune,
                                onCheckedChange = { viewModel.flexTune.value = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = StudioPrimary),
                                modifier = Modifier.scale(0.85f)
                            )
                        }
                    }
                }

                // VOICE PRESET Section (2-column grid in Image 8)
                Text(
                    text = "VOICE PRESET",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = StudioTextSecondary
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (row in 0 until voicePresetsList.size step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val preset1 = voicePresetsList[row]
                            val isSel1 = selectedVoicePreset == preset1
                            VoicePresetCard(
                                title = preset1,
                                isSelected = isSel1,
                                onClick = { viewModel.voicePreset.value = preset1 },
                                modifier = Modifier.weight(1f)
                            )

                            if (row + 1 < voicePresetsList.size) {
                                val preset2 = voicePresetsList[row + 1]
                                val isSel2 = selectedVoicePreset == preset2
                                VoicePresetCard(
                                    title = preset2,
                                    isSelected = isSel2,
                                    onClick = { viewModel.voicePreset.value = preset2 },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // LIVE PITCH MONITOR (Image 8)
                StudioCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "LIVE PITCH MONITOR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = StudioTextSecondary
                        )

                        // Meter Scale: -50c to +50c
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("-50¢", fontSize = 10.sp, color = StudioTextSecondary)
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFFEDE4EE))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Input Monitor", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = StudioPrimary)
                            }
                            Text("+50¢", fontSize = 10.sp, color = StudioTextSecondary)
                        }

                        // Needle Pitch Line Canvas
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                        ) {
                            val w = size.width
                            val h = size.height
                            val centerX = w / 2f

                            // Ruler ticks
                            for (tick in 0..20) {
                                val x = tick * (w / 20f)
                                val tickH = if (tick % 5 == 0) h * 0.7f else h * 0.4f
                                drawLine(Color(0xFFDED0E2), Offset(x, h - tickH), Offset(x, h), strokeWidth = 1f)
                            }

                            // Center in-tune line
                            drawLine(StudioPrimary, Offset(centerX, 0f), Offset(centerX, h), strokeWidth = 1.5f)

                            // Dynamic Live needle position (-50c to +50c)
                            val normCent = (livePitchCent + 50f) / 100f
                            val needleX = normCent * w

                            drawLine(
                                color = Color(0xFFC75D76),
                                start = Offset(needleX, 0f),
                                end = Offset(needleX, h),
                                strokeWidth = 3.dp.toPx()
                            )
                        }

                        // Segmented LED meter bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFE4D5E8))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.65f)
                                    .height(6.dp)
                                    .background(StudioMeterGreen)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Buttons: Bypass, Save Preset (Image 8)
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
                    Text("Bypass", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = StudioTextPrimary)
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

@Composable
private fun VoicePresetCard(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) Color(0xFFC7B1D0) else Color(0xF7FFFFFF)
    val textCol = if (isSelected) Color.White else StudioTextPrimary

    Box(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else StudioPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = textCol
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(StudioPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
