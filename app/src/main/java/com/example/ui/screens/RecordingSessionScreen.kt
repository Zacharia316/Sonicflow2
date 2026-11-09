package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioScreenBackground
import com.example.ui.components.WaveformView
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioRecordRed
import com.example.ui.theme.StudioRecordRedActive
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.viewmodel.StudioScreen
import com.example.viewmodel.StudioViewModel

/**
 * Recording Session / Vocal Booth screen matching user reference Image 4.
 */
@Composable
fun RecordingSessionScreen(viewModel: StudioViewModel) {
    val context = LocalContext.current
    val activeProject by viewModel.activeProject.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isRecording by viewModel.audioRecorder.isRecording.collectAsState()
    val inputLevel by viewModel.audioRecorder.inputLevel.collectAsState()
    val isMetronomeOn by viewModel.isMetronomeOn.collectAsState()
    val isMonitorOn by viewModel.isMonitorOn.collectAsState()

    var countInNumber by remember { mutableIntStateOf(0) }
    var lyricsText by remember { mutableStateOf(activeProject.lyrics) }
    var isBeatLoaded by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.audioRecorder.startRecording(
                onCountDown = { countInNumber = it },
                onFinishedCountDown = {
                    countInNumber = 0
                    viewModel.startPlayback()
                }
            )
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            isBeatLoaded = true
        }
    }

    // Pulse animation for armed recording button
    val infiniteTransition = rememberInfiniteTransition(label = "recPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "recScale"
    )

    StudioScreenBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top Bar: < New Session, Import Beat
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
                        text = "New Session",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary
                    )
                }

                Text(
                    text = "Import Beat",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StudioPrimary,
                    modifier = Modifier.clickable { filePickerLauncher.launch("audio/*") }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // IMPORT BEAT Section (Image 4)
            Text(
                text = "IMPORT BEAT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = StudioTextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Drop MP3 / WAV here box
            StudioCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Upload",
                        tint = StudioPrimary,
                        modifier = Modifier.size(36.dp)
                    )

                    Text(
                        text = "Drop MP3 / WAV here",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StudioTextPrimary
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEDE4EE))
                                .clickable { filePickerLauncher.launch("audio/*") }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Browse Files", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StudioTextPrimary)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEDE4EE))
                                .clickable { viewModel.navigateTo(StudioScreen.HOME) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("From Library", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StudioTextPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Loaded beat card
            StudioCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEDE4EE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = StudioPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${activeProject.title} - ${activeProject.bpm} BPM - 2:34",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioTextPrimary
                            )
                            WaveformView(
                                progress = if (isPlaying) 0.35f else 0.1f,
                                height = 18.dp,
                                barCount = 30
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(StudioPrimary)
                            .clickable { viewModel.togglePlay() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isPlaying) "Stop" else "Use Beat",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // RECORD Section (Image 4)
            Text(
                text = "RECORD",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = StudioTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Big REC Button
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val scale = if (isRecording) pulseScale else 1f
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(if (isRecording) StudioRecordRedActive else Color(0xFFC9A2AF))
                        .clickable {
                            if (isRecording) {
                                viewModel.audioRecorder.stopRecording()
                            } else {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    viewModel.audioRecorder.startRecording(
                                        onCountDown = { countInNumber = it },
                                        onFinishedCountDown = {
                                            countInNumber = 0
                                            viewModel.startPlayback()
                                        }
                                    )
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            }

            Text(
                text = "REC",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = StudioTextSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
            )

            // Vocal Track • Armed
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Vocal Track • Armed",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StudioTextPrimary
                )
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Armed mic",
                    tint = if (isRecording) StudioRecordRedActive else StudioPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isRecording) StudioRecordRedActive else Color(0xFF4CAF50))
                )
            }

            // Count-in: 3 2 1
            if (countInNumber > 0) {
                Text(
                    text = "$countInNumber",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioPrimary,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 4.dp)
                )
            } else {
                Row(
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("3", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StudioTextSecondary)
                    Text("2", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StudioTextSecondary)
                    Text("1", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StudioTextSecondary)
                }
            }

            // INPUT LEVEL Meter (VU Bar)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("INPUT LEVEL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StudioTextSecondary)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFFE4D5E8))
                ) {
                    val level = if (isRecording) inputLevel.coerceIn(0.05f, 1f) else 0.25f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(level)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(StudioPrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Monitor & Metronome Toggles (Image 4)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isMonitorOn) "Monitor: On" else "Monitor: Off",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = StudioTextPrimary
                    )
                    Switch(
                        checked = isMonitorOn,
                        onCheckedChange = { viewModel.toggleMonitor() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = StudioPrimary,
                            uncheckedTrackColor = Color(0xFFDED0E2)
                        ),
                        modifier = Modifier.scale(0.8f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isMetronomeOn) "Metronome: On" else "Metronome: Off",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = StudioTextPrimary
                    )
                    Switch(
                        checked = isMetronomeOn,
                        onCheckedChange = { viewModel.toggleMetronome() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = StudioPrimary,
                            uncheckedTrackColor = Color(0xFFDED0E2)
                        ),
                        modifier = Modifier.scale(0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lyric hint
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Lyric hint:", fontSize = 11.sp, color = StudioTextSecondary)
                Text(
                    text = "\"$lyricsText\"",
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    color = StudioTextPrimary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Transport Controls: Prev, Stop, Play, Loop
            StudioCard(
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                backgroundColor = Color(0xF5FFFFFF)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Rewind",
                        tint = StudioTextSecondary,
                        modifier = Modifier.size(24.dp).clickable { /* Rewind */ }
                    )
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = StudioTextSecondary,
                        modifier = Modifier.size(24.dp).clickable {
                            viewModel.stopPlayback()
                            viewModel.audioRecorder.stopRecording()
                        }
                    )
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = StudioPrimary,
                        modifier = Modifier.size(28.dp).clickable { viewModel.togglePlay() }
                    )
                    Icon(
                        imageVector = Icons.Default.Loop,
                        contentDescription = "Loop",
                        tint = StudioTextSecondary,
                        modifier = Modifier.size(24.dp).clickable { viewModel.toggleLoop() }
                    )
                }
            }
        }
    }
}
