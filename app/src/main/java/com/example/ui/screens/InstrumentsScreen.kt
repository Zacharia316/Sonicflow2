package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.DrumType
import com.example.ui.components.RotaryKnob
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioPill
import com.example.ui.components.StudioScreenBackground
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.viewmodel.StudioScreen
import com.example.viewmodel.StudioViewModel

data class InstrumentInfo(
    val name: String,
    val description: String,
    val category: String,
    val icon: ImageVector
)

val defaultInstruments = listOf(
    InstrumentInfo("Grand Piano", "Warm • Classical", "Keys", Icons.Default.Piano),
    InstrumentInfo("808 Kit", "Hip-hop • Lo-fi", "Drums", Icons.Default.GraphicEq),
    InstrumentInfo("Afro Drums", "Percussion • Ethnic", "Drums", Icons.Default.MusicNote),
    InstrumentInfo("Synth Pad", "Ambient • Pad", "Pads", Icons.Default.GraphicEq)
)

val drumPadsList = listOf(
    DrumType.KICK,
    DrumType.SNARE,
    DrumType.HIHAT_CLOSED,
    DrumType.HIHAT_OPEN,
    DrumType.CLAP,
    DrumType.RIMSHOT,
    DrumType.CONGA_HIGH,
    DrumType.CONGA_LOW,
    DrumType.SHAKER,
    DrumType.TOM,
    DrumType.VOCAL_CHOP,
    DrumType.CRASH,
    DrumType.KICK, // 808 Sub Kick
    DrumType.CLAP,
    DrumType.HIHAT_CLOSED,
    DrumType.CONGA_HIGH
)

/**
 * Instruments & 4x4 Neumorphic Drum Pad screen matching user reference Image 2.
 */
@Composable
fun InstrumentsScreen(viewModel: StudioViewModel) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("Pads") }
    val categories = listOf("Keys", "Pads", "Drums", "Bass")

    val reverb by viewModel.instReverb.collectAsState()
    val delay by viewModel.instDelay.collectAsState()
    val attack by viewModel.instAttack.collectAsState()

    var activePadIndex by remember { mutableIntStateOf(-1) }

    fun triggerVibration() {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (_: Exception) {}
    }

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
                    text = "INSTRUMENTS",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = StudioTextPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                )

                // Piano Roll shortcut
                StudioPill(
                    text = "Piano Roll",
                    isSelected = false,
                    onClick = { viewModel.navigateTo(StudioScreen.PIANO_ROLL) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Filter Pills: Keys, Pads, Drums, Bass
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                categories.forEach { cat ->
                    StudioPill(
                        text = cat,
                        isSelected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        modifier = Modifier.weight(1f).padding(horizontal = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Instruments List
            Text(
                text = "INSTRUMENTS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = StudioTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                defaultInstruments.forEach { inst ->
                    StudioCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        onClick = {
                            if (inst.name.contains("Piano")) {
                                viewModel.navigateTo(StudioScreen.PIANO_ROLL)
                            } else {
                                viewModel.triggerPad(DrumType.KICK)
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFEDE4EE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = inst.icon,
                                        contentDescription = inst.name,
                                        tint = StudioPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = inst.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = StudioTextPrimary
                                    )
                                    Text(
                                        text = inst.description,
                                        fontSize = 11.sp,
                                        color = StudioTextSecondary
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Select",
                                tint = StudioTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Controls Section (Reverb, Delay, Attack knobs)
            Text(
                text = "CONTROLS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = StudioTextSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RotaryKnob(
                    value = reverb,
                    onValueChange = { viewModel.instReverb.value = it },
                    label = "Reverb",
                    valueLabel = "${(reverb * 100).toInt()}%"
                )
                RotaryKnob(
                    value = delay,
                    onValueChange = { viewModel.instDelay.value = it },
                    label = "Delay",
                    valueLabel = "${(delay * 100).toInt()}%"
                )
                RotaryKnob(
                    value = attack,
                    onValueChange = { viewModel.instAttack.value = it },
                    label = "Attack",
                    valueLabel = "${(attack * 100).toInt()}ms"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4x4 Neumorphic Drum Pad Grid (Image 2)
            Text(
                text = "DRUM PAD",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = StudioTextSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(drumPadsList.size) { index ->
                    val drumType = drumPadsList[index]
                    val isPressed = activePadIndex == index
                    val padBg = if (isPressed) Color(0xFFC7B1D0) else Color(0xF2FFFFFF)

                    Box(
                        modifier = Modifier
                            .aspectRatio(1.2f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(padBg)
                            .clickable {
                                activePadIndex = index
                                triggerVibration()
                                viewModel.triggerPad(drumType)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = drumType.displayName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPressed) Color.White else StudioTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
