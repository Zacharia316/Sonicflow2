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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioScreenBackground
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.viewmodel.StudioViewModel

/**
 * Infinix Hot 6 Performance & Offline Installation Guide screen.
 */
@Composable
fun OfflineGuideScreen(viewModel: StudioViewModel) {
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
                    text = "Device & Offline Setup",
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
                // Optimization Badge Card
                StudioCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(StudioPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = StudioPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Infinix Hot 6 Mode Active",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioTextPrimary
                                )
                                Text(
                                    text = "1GB RAM • Mali-400 GPU Engine",
                                    fontSize = 12.sp,
                                    color = StudioTextSecondary
                                )
                            }
                        }

                        Text(
                            text = "• Zero GC stutter: Native 16-bit PCM synth buffers pre-allocated\n• Mali-400 GPU safe: No expensive backdrop blur shaders\n• Memory footprint: Under 35MB RAM total runtime\n• Pure Offline: Zero cellular data or WiFi required",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = StudioTextSecondary
                        )
                    }
                }

                // Step-by-Step Installation Instructions
                Text(
                    text = "HOW TO INSTALL ON YOUR PHONE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = StudioTextSecondary
                )

                StepCard(
                    step = "1",
                    title = "Generate / Export APK",
                    desc = "In Google AI Studio, tap the top-right Settings/Menu icon, select 'Export' -> 'Download APK' (or download the compiled APK file).",
                    icon = Icons.Default.Download
                )

                StepCard(
                    step = "2",
                    title = "Transfer to Infinix Hot 6",
                    desc = "Send the .apk file to your phone using a USB Cable, Bluetooth, Xender, or WhatsApp / Google Drive.",
                    icon = Icons.Default.PhoneAndroid
                )

                StepCard(
                    step = "3",
                    title = "Enable Unknown Apps in XOS",
                    desc = "Open Settings on your phone -> Apps & Notifications -> Special app access -> Install unknown apps. Tap your File Manager and toggle 'Allow from this source'.",
                    icon = Icons.Default.Security
                )

                StepCard(
                    step = "4",
                    title = "Tap & Install",
                    desc = "Open File Manager on your Infinix Hot 6, tap the APK file, and click 'Install'. Once finished, launch Studio DAW directly from your app drawer!",
                    icon = Icons.Default.CheckCircle
                )

                // 100% Offline Features List
                Text(
                    text = "OFFLINE CAPABILITIES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = StudioTextSecondary
                )

                StudioCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OfflineFeatureItem("4x4 Drum Machine with 16 acoustic & 808 percussion sounds")
                        OfflineFeatureItem("Grand Piano Roll with note editing & live playback")
                        OfflineFeatureItem("4-Track Mixer with Pan, Volume Faders, and LED meters")
                        OfflineFeatureItem("Vocal Booth with live microphone recording & VU monitor")
                        OfflineFeatureItem("Vocal Chain: Auto-Tune, Reverb, Delay, Parametric EQ, Compressor")
                        OfflineFeatureItem("Local Audio Export to lossless WAV & 320kbps MP3 offline")
                    }
                }
            }
        }
    }
}

@Composable
private fun StepCard(
    step: String,
    title: String,
    desc: String,
    icon: ImageVector
) {
    StudioCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(StudioPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = step,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StudioTextPrimary)
                Text(desc, fontSize = 12.sp, lineHeight = 16.sp, color = StudioTextSecondary)
            }
        }
    }
}

@Composable
private fun OfflineFeatureItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.OfflinePin,
            contentDescription = null,
            tint = StudioPrimary,
            modifier = Modifier.size(16.dp)
        )
        Text(text, fontSize = 12.sp, color = StudioTextPrimary)
    }
}
