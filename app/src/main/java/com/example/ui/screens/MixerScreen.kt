package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ChannelStrip
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioPill
import com.example.ui.components.StudioScreenBackground
import com.example.ui.components.TransportBar
import com.example.ui.components.WaveformView
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.viewmodel.StudioScreen
import com.example.viewmodel.StudioViewModel

/**
 * Multi-track Mixer and Timeline screen matching user reference Image 9.
 */
@Composable
fun MixerScreen(viewModel: StudioViewModel) {
    val activeProject by viewModel.activeProject.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playProgress by viewModel.playbackProgress.collectAsState()
    val isMetronomeOn by viewModel.isMetronomeOn.collectAsState()
    val isLoopOn by viewModel.isLoopOn.collectAsState()

    val beatMeter by viewModel.beatMeter.collectAsState()
    val vocalMeter by viewModel.vocalMeter.collectAsState()
    val adlibMeter by viewModel.adlibMeter.collectAsState()
    val harmonyMeter by viewModel.harmonyMeter.collectAsState()

    val beatMuted by viewModel.beatMuted.collectAsState()
    val vocalMuted by viewModel.vocalMuted.collectAsState()
    val adlibMuted by viewModel.adlibMuted.collectAsState()
    val harmonyMuted by viewModel.harmonyMuted.collectAsState()

    val beatSolo by viewModel.beatSolo.collectAsState()
    val vocalSolo by viewModel.vocalSolo.collectAsState()
    val adlibSolo by viewModel.adlibSolo.collectAsState()
    val harmonySolo by viewModel.harmonySolo.collectAsState()

    StudioScreenBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header: Back button, Title, Subtitle
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

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "STUDIO / MIXER",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = StudioTextPrimary
                    )
                    Text(
                        text = "Project • ${activeProject.title} • ${activeProject.bpm} BPM",
                        fontSize = 11.sp,
                        color = StudioTextSecondary
                    )
                }

                // Quick export action
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.6f))
                        .clickable { viewModel.navigateTo(StudioScreen.EXPORT_SHARE) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export",
                        tint = StudioPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Studio Navigation Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StudioPill(
                    text = "Piano Roll",
                    isSelected = false,
                    onClick = { viewModel.navigateTo(StudioScreen.PIANO_ROLL) },
                    modifier = Modifier.weight(1f)
                )
                StudioPill(
                    text = "16 Pads",
                    isSelected = false,
                    onClick = { viewModel.navigateTo(StudioScreen.INSTRUMENTS) },
                    modifier = Modifier.weight(1f)
                )
                StudioPill(
                    text = "Vocal FX",
                    isSelected = false,
                    onClick = { viewModel.navigateTo(StudioScreen.VOCAL_FX) },
                    modifier = Modifier.weight(1f)
                )
                StudioPill(
                    text = "Tuner",
                    isSelected = false,
                    onClick = { viewModel.navigateTo(StudioScreen.AUTO_TUNE) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Timeline Waveform Display Card (Image 9)
            StudioCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                elevation = 1.dp,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Timeline Top Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEDE4EE))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "TIMELINE — 00:12 / 01:24",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StudioTextPrimary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.ContentCut,
                                contentDescription = "Split",
                                tint = StudioTextSecondary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { /* Split region */ }
                            )
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "FX",
                                tint = StudioPrimary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { viewModel.navigateTo(StudioScreen.VOCAL_FX) }
                            )
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Zoom",
                                tint = StudioTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // BEAT Waveform Track
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF8A6C9B))
                            )
                            Text("BEAT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StudioTextSecondary)
                        }
                        WaveformView(
                            progress = if (isPlaying) playProgress else 0.22f,
                            height = 28.dp,
                            barCount = 48,
                            seed = 10,
                            onSeek = { viewModel.startPlayback() }
                        )
                    }

                    // VOCAL Waveform Track
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFB57088))
                            )
                            Text("VOCAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StudioTextSecondary)
                        }
                        WaveformView(
                            progress = if (isPlaying) playProgress else 0.22f,
                            height = 28.dp,
                            barCount = 48,
                            seed = 30,
                            activeColor = Color(0xFF9E6580),
                            onSeek = { viewModel.startPlayback() }
                        )
                    }

                    // Timeline Ruler markers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0s", fontSize = 9.sp, color = StudioTextSecondary)
                        Text("4", fontSize = 9.sp, color = StudioTextSecondary)
                        Text("8", fontSize = 9.sp, color = StudioTextSecondary)
                        Text("12", fontSize = 9.sp, color = StudioTextSecondary)
                        Text("16", fontSize = 9.sp, color = StudioTextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // MIXER Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MIXER",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioTextPrimary
                )
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "Mixer Options",
                    tint = StudioTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Channel Strips Scrollable List (BEAT, VOCAL, ADLIB, HARMONY)
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ChannelStrip(
                        trackName = "BEAT",
                        volume = activeProject.beatVolume,
                        onVolumeChange = { viewModel.updateBeatVolume(it) },
                        pan = activeProject.beatPan,
                        onPanChange = { viewModel.updateBeatPan(it) },
                        isMuted = beatMuted,
                        onMuteToggle = { viewModel.beatMuted.value = !beatMuted },
                        isSolo = beatSolo,
                        onSoloToggle = { viewModel.beatSolo.value = !beatSolo },
                        levelMeter = beatMeter,
                        dotColor = Color(0xFF8A6C9B)
                    )
                }

                item {
                    ChannelStrip(
                        trackName = "VOCAL",
                        volume = activeProject.vocalVolume,
                        onVolumeChange = { viewModel.updateVocalVolume(it) },
                        pan = activeProject.vocalPan,
                        onPanChange = { viewModel.updateVocalPan(it) },
                        isMuted = vocalMuted,
                        onMuteToggle = { viewModel.vocalMuted.value = !vocalMuted },
                        isSolo = vocalSolo,
                        onSoloToggle = { viewModel.vocalSolo.value = !vocalSolo },
                        levelMeter = vocalMeter,
                        dotColor = Color(0xFFB57088)
                    )
                }

                item {
                    ChannelStrip(
                        trackName = "ADLIB",
                        volume = activeProject.adlibVolume,
                        onVolumeChange = { viewModel.updateAdlibVolume(it) },
                        pan = activeProject.adlibPan,
                        onPanChange = { viewModel.updateAdlibPan(it) },
                        isMuted = adlibMuted,
                        onMuteToggle = { viewModel.adlibMuted.value = !adlibMuted },
                        isSolo = adlibSolo,
                        onSoloToggle = { viewModel.adlibSolo.value = !adlibSolo },
                        levelMeter = adlibMeter,
                        dotColor = Color(0xFFAA80B2)
                    )
                }

                item {
                    ChannelStrip(
                        trackName = "HARMONY",
                        volume = activeProject.harmonyVolume,
                        onVolumeChange = { viewModel.updateHarmonyVolume(it) },
                        pan = activeProject.harmonyPan,
                        onPanChange = { viewModel.updateHarmonyPan(it) },
                        isMuted = harmonyMuted,
                        onMuteToggle = { viewModel.harmonyMuted.value = !harmonyMuted },
                        isSolo = harmonySolo,
                        onSoloToggle = { viewModel.harmonySolo.value = !harmonySolo },
                        levelMeter = harmonyMeter,
                        dotColor = Color(0xFF90709E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Transport Bar
            TransportBar(
                isPlaying = isPlaying,
                isRecording = false,
                isMetronomeOn = isMetronomeOn,
                isLoopOn = isLoopOn,
                onRecordClick = { viewModel.navigateTo(StudioScreen.RECORDING_SESSION) },
                onPlayClick = { viewModel.togglePlay() },
                onStopClick = { viewModel.stopPlayback() },
                onMetronomeToggle = { viewModel.toggleMetronome() },
                onLoopToggle = { viewModel.toggleLoop() }
            )
        }
    }
}
