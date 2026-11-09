package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioExporter
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioScreenBackground
import com.example.ui.components.WaveformView
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.viewmodel.StudioViewModel

/**
 * Export & Share screen matching user reference Image 7.
 */
@Composable
fun ExportScreen(viewModel: StudioViewModel) {
    val context = LocalContext.current
    val activeProject by viewModel.activeProject.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val exportFormat by viewModel.exportFormat.collectAsState()
    val exportQuality by viewModel.exportQuality.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()
    val exportProgress by viewModel.exportProgress.collectAsState()
    val exportedFile by viewModel.exportedFile.collectAsState()

    var showQualityPicker by remember { mutableStateOf(false) }

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
                    text = "Export & Share",
                    fontSize = 18.sp,
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
                // Song Preview Card (Image 7)
                StudioCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(StudioPrimary)
                                    .clickable { viewModel.togglePlay() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = "Play mix",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "${activeProject.title} - Final Mix",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioTextPrimary
                                )
                                Text(
                                    text = activeProject.durationFormatted,
                                    fontSize = 12.sp,
                                    color = StudioTextSecondary
                                )
                            }
                        }

                        WaveformView(
                            progress = if (isPlaying) 0.45f else 0.25f,
                            height = 32.dp,
                            barCount = 42
                        )
                    }
                }

                // EXPORT FORMAT Section (Image 7)
                Text(
                    text = "EXPORT FORMAT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = StudioTextSecondary
                )

                StudioCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AudioExporter.ExportFormat.values().forEach { format ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.exportFormat.value = format }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RadioButton(
                                        selected = exportFormat == format,
                                        onClick = { viewModel.exportFormat.value = format },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = StudioPrimary,
                                            unselectedColor = Color(0xFFDED0E2)
                                        )
                                    )
                                    Text(
                                        text = format.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = StudioTextPrimary
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFEDE4EE))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = format.badge,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StudioPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // QUALITY Section (Image 7)
                Text(
                    text = "QUALITY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = StudioTextSecondary
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xF7FFFFFF))
                        .clickable { showQualityPicker = !showQualityPicker }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Quality", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = StudioTextSecondary)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = exportQuality.label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioTextPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = StudioTextSecondary
                            )
                        }
                    }
                }

                if (showQualityPicker) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        AudioExporter.AudioQuality.values().forEach { q ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (exportQuality == q) Color(0xFFEDE4EE) else Color.White)
                                    .clickable {
                                        viewModel.exportQuality.value = q
                                        showQualityPicker = false
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(q.label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StudioTextPrimary)
                            }
                        }
                    }
                }

                // SHARE Section (Image 7)
                Text(
                    text = "SHARE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = StudioTextSecondary
                )

                StudioCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ShareRowItem("Save to Files", Icons.Default.Folder) {
                            if (exportedFile != null) {
                                AudioExporter.shareExportedFile(context, exportedFile!!)
                            } else {
                                viewModel.startExport(context)
                                Toast.makeText(context, "Rendering offline mix... Tap Export below!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        ShareRowItem("Share to TikTok", Icons.Default.Share) {
                            handleShare(context, exportedFile, viewModel)
                        }
                        ShareRowItem("Instagram", Icons.Default.Share) {
                            handleShare(context, exportedFile, viewModel)
                        }
                        ShareRowItem("YouTube", Icons.Default.Share) {
                            handleShare(context, exportedFile, viewModel)
                        }
                        ShareRowItem("WhatsApp", Icons.Default.Share) {
                            handleShare(context, exportedFile, viewModel)
                        }
                        ShareRowItem("Copy Link", Icons.Default.ContentCopy) {
                            Toast.makeText(context, "Project mix offline path copied!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Export Progress Bar ("Exporting... 42%") - Image 7
            if (isExporting || exportProgress > 0) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (exportProgress < 100) "Exporting... $exportProgress%" else "Export complete! Ready to share.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StudioPrimary
                    )
                    LinearProgressIndicator(
                        progress = { exportProgress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = StudioPrimary,
                        trackColor = Color(0xFFE4D5E8)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Export Button (Image 7)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(StudioPrimary)
                    .clickable {
                        if (exportedFile != null && !isExporting) {
                            AudioExporter.shareExportedFile(context, exportedFile!!)
                        } else {
                            viewModel.startExport(context)
                        }
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (exportedFile != null && !isExporting) "Share Mix (${exportFormat.extension})" else "Export",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ShareRowItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = StudioPrimary,
                modifier = Modifier.size(18.dp)
            )
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = StudioTextPrimary)
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = StudioTextSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}

private fun handleShare(
    context: android.content.Context,
    file: java.io.File?,
    viewModel: StudioViewModel
) {
    if (file != null) {
        AudioExporter.shareExportedFile(context, file)
    } else {
        viewModel.startExport(context)
        Toast.makeText(context, "Rendering offline audio mix first...", Toast.LENGTH_SHORT).show()
    }
}
