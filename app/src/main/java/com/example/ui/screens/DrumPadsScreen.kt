package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.DrumType
import com.example.ui.components.RotaryKnob
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioPill
import com.example.ui.components.StudioScreenBackground
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.viewmodel.StudioScreen
import com.example.viewmodel.StudioViewModel

/**
 * Dedicated 16-Pad Drum Machine / MPC performance screen.
 * Designed with the app's clean light purple aesthetic, responsive touch feedback,
 * tactile haptic triggers, sound-shaping knobs, and direct link to the Mix Editor.
 */
@Composable
fun DrumPadsScreen(viewModel: StudioViewModel) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isRecording by viewModel.isRecordingMultitrack.collectAsState()
    val activeProject by viewModel.activeProject.collectAsState()

    var selectedKit by remember { mutableStateOf("808 Kit") }
    var reverbAmount by remember { mutableFloatStateOf(0.25f) }
    var delayAmount by remember { mutableFloatStateOf(0.15f) }
    var attackAmount by remember { mutableFloatStateOf(0.60f) }
    var velocityAmount by remember { mutableFloatStateOf(0.85f) }

    val kitPills = listOf("808 Kit", "Afro Kit", "Trap", "Lo-Fi", "Percs")

    val drumPads = listOf(
        DrumPadItem(DrumType.KICK, "Kick", Color(0xFFE53935)),
        DrumPadItem(DrumType.SNARE, "Snare", Color(0xFF8E24AA)),
        DrumPadItem(DrumType.HIHAT_CLOSED, "Hat Cls", Color(0xFF3949AB)),
        DrumPadItem(DrumType.HIHAT_OPEN, "Hat Opn", Color(0xFF1E88E5)),
        DrumPadItem(DrumType.CLAP, "Clap", Color(0xFF00897B)),
        DrumPadItem(DrumType.RIMSHOT, "Rim", Color(0xFF43A047)),
        DrumPadItem(DrumType.CONGA_HIGH, "Conga Hi", Color(0xFFFB8C00)),
        DrumPadItem(DrumType.CONGA_LOW, "Conga Low", Color(0xFFF4511E)),
        DrumPadItem(DrumType.SHAKER, "Shaker", Color(0xFF6D4C41)),
        DrumPadItem(DrumType.TOM, "Tom", Color(0xFF5E35B1)),
        DrumPadItem(DrumType.VOCAL_CHOP, "Vocal", Color(0xFFD81B60)),
        DrumPadItem(DrumType.CRASH, "Crash", Color(0xFF00ACC1)),
        DrumPadItem(DrumType.KICK, "808 Sub", Color(0xFFC2185B)),
        DrumPadItem(DrumType.CLAP, "Snap", Color(0xFF00897B)),
        DrumPadItem(DrumType.HIHAT_CLOSED, "Tick", Color(0xFF3949AB)),
        DrumPadItem(DrumType.CONGA_HIGH, "Perc", Color(0xFFFB8C00))
    )

    BackHandler {
        viewModel.navigateBack()
    }

    StudioScreenBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f))
                        .clickable { viewModel.navigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = StudioTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = "Drum Pads",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary
                    )
                    Text(
                        text = "${activeProject.title} • 16-Pad MPC",
                        fontSize = 12.sp,
                        color = StudioTextSecondary
                    )
                }

                // Quick jump to Mix Editor
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                        .clickable { viewModel.navigateTo(StudioScreen.MIXER) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = StudioPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Mixer",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioPrimary
                        )
                    }
                }
            }

            // Kit Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                kitPills.forEach { kit ->
                    StudioPill(
                        text = kit,
                        isSelected = selectedKit == kit,
                        onClick = {
                            selectedKit = kit
                            viewModel.postStatus("Kit selected: $kit")
                        }
                    )
                }
            }

            // Sound Shaping Controls Row (Neumorphic Knobs)
            StudioCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "PAD CONTROLS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = StudioTextMuted
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RotaryKnob(
                            label = "Reverb",
                            value = reverbAmount,
                            onValueChange = { reverbAmount = it }
                        )
                        RotaryKnob(
                            label = "Delay",
                            value = delayAmount,
                            onValueChange = { delayAmount = it }
                        )
                        RotaryKnob(
                            label = "Attack",
                            value = attackAmount,
                            onValueChange = { attackAmount = it }
                        )
                        RotaryKnob(
                            label = "Velocity",
                            value = velocityAmount,
                            onValueChange = { velocityAmount = it }
                        )
                    }
                }
            }

            // 4x4 Neumorphic Drum Pads Grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(vertical = 4.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    userScrollEnabled = false,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(drumPads) { pad ->
                        NeumorphicPad(
                            pad = pad,
                            onTap = {
                                viewModel.triggerPad(pad.drumType)
                                // Gentle tactile haptic tick
                                try {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        vibrator?.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
                                    } else {
                                        @Suppress("DEPRECATION")
                                        vibrator?.vibrate(18)
                                    }
                                } catch (_: Exception) {}
                            }
                        )
                    }
                }
            }

            // Bottom Quick Bar: Transport Sync & Back to Mixer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Stop Project Playhead
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isPlaying) StudioPrimary else Color.White.copy(alpha = 0.85f))
                        .clickable { viewModel.toggleMultitrackPlay() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (isPlaying) Color.White else StudioTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isPlaying) "Stop" else "Play Mix",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPlaying) Color.White else StudioTextPrimary
                        )
                    }
                }

                // Record into Mix
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isRecording) Color(0xFFD32F2F) else Color(0xFFE53935))
                        .clickable {
                            if (isRecording) {
                                viewModel.stopMultitrackRecording()
                            } else {
                                viewModel.startMultitrackRecording()
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isRecording) "Recording..." else "Record to Track",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

private data class DrumPadItem(
    val drumType: DrumType,
    val title: String,
    val accentColor: Color
)

@Composable
private fun NeumorphicPad(
    pad: DrumPadItem,
    onTap: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = tween(80),
        label = "padScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.0f)
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 1.dp else 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0x334A2E5B)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = if (isPressed) {
                    Brush.verticalGradient(
                        colors = listOf(
                            pad.accentColor.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.95f)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFF3EAF6)
                        )
                    )
                }
            )
            .border(
                width = if (isPressed) 1.8.dp else 0.8.dp,
                color = if (isPressed) pad.accentColor else Color(0xFFE2D6E6),
                shape = RoundedCornerShape(16.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onTap()
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isPressed) pad.accentColor else pad.accentColor.copy(alpha = 0.4f))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = pad.title,
                fontSize = 11.sp,
                fontWeight = if (isPressed) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (isPressed) StudioPrimary else StudioTextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
