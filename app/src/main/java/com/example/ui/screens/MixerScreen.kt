package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.audio.AudioUtils
import com.example.data.StudioTrack
import com.example.ui.components.StudioScreenBackground
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.viewmodel.StudioScreen
import com.example.viewmodel.StudioViewModel

private const val DP_PER_SECOND = 18f
private const val RULER_TOTAL_SECONDS = 180 // 3 minutes timeline length
private val HEADER_WIDTH = 118.dp
private val TRACK_HEIGHT = 76.dp

/**
 * Mobile-first Multitrack DAW Mix Editor matching user reference screenshot layout.
 * Styled in the app's clean light-purple aesthetic with unsqueezed action icons,
 * prominent track selection indicator, live playhead, real clip waveforms, and studio bottom nav.
 */
@Composable
fun MixerScreen(viewModel: StudioViewModel) {
    val context = LocalContext.current
    val activeProject by viewModel.activeProject.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val selectedTrackId by viewModel.selectedTrackId.collectAsState()
    val selectedClipId by viewModel.selectedClipId.collectAsState()
    val playheadSeconds by viewModel.playheadSeconds.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isRecording by viewModel.isRecordingMultitrack.collectAsState()
    val isMetronomeOn by viewModel.isMetronomeOn.collectAsState()
    val isLoopOn by viewModel.isLoopOn.collectAsState()

    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    // Dialog states
    var showProjectMenu by remember { mutableStateOf(false) }
    var showRenameProjectDialog by remember { mutableStateOf(false) }
    var showAddTrackDialog by remember { mutableStateOf(false) }
    var trackToRename by remember { mutableStateOf<StudioTrack?>(null) }
    var trackToDelete by remember { mutableStateOf<StudioTrack?>(null) }

    // System File Picker for Audio Import
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importAudioClip(uri)
        }
    }

    // Microphone Permission Launcher
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startMultitrackRecording()
        } else {
            viewModel.postStatus("Microphone permission denied. Enable it in Settings to record.", isError = true)
        }
    }

    StudioScreenBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 1. Header: Back button, Title "Mix Editor • <Name>", Subtitle "<BPM> BPM • <N> tracks", and ⋯ Menu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.75f))
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
                        .padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Mix Editor • ${activeProject.title}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = "${activeProject.bpm} BPM • ${tracks.size} ${if (tracks.size == 1) "track" else "tracks"}",
                        fontSize = 11.sp,
                        color = StudioTextSecondary
                    )
                }

                // ⋯ Menu on the right
                Box {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.75f))
                            .clickable { showProjectMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "Project Options",
                            tint = StudioTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showProjectMenu,
                        onDismissRequest = { showProjectMenu = false },
                        modifier = Modifier.background(StudioCardBg)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Rename project", fontSize = 14.sp, color = StudioTextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = StudioPrimary, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                showProjectMenu = false
                                showRenameProjectDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Add track", fontSize = 14.sp, color = StudioTextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Default.Add, contentDescription = null, tint = StudioPrimary, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                showProjectMenu = false
                                showAddTrackDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Drum Pads MPC", fontSize = 14.sp, color = StudioTextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Default.GridOn, contentDescription = null, tint = StudioPrimary, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                showProjectMenu = false
                                viewModel.navigateTo(StudioScreen.DRUM_PADS)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete selected track", fontSize = 14.sp, color = Color(0xFFD32F2F)) },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                showProjectMenu = false
                                val selected = tracks.find { it.id == selectedTrackId } ?: tracks.firstOrNull()
                                if (selected != null) {
                                    trackToDelete = selected
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // 2. Time Ruler: Seconds markers (0:00, 0:10...) with red/coral playhead triangle & line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .background(Color.White.copy(alpha = 0.50f))
                    .border(0.5.dp, Color(0xFFE4D5E8)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fixed blank corner spacer aligned with left track header
                Box(
                    modifier = Modifier
                        .width(HEADER_WIDTH)
                        .fillMaxHeight()
                        .padding(start = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "TRACKS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = StudioTextMuted
                    )
                }

                // Horizontally scrollable ruler lane
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .horizontalScroll(horizontalScrollState)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val tappedSec = (offset.x / DP_PER_SECOND).coerceAtLeast(0f)
                                viewModel.seekPlayhead(tappedSec)
                            }
                        }
                ) {
                    val totalWidth = (RULER_TOTAL_SECONDS * DP_PER_SECOND).dp

                    Canvas(
                        modifier = Modifier
                            .width(totalWidth)
                            .fillMaxHeight()
                    ) {
                        for (s in 0..RULER_TOTAL_SECONDS) {
                            val xPos = s * DP_PER_SECOND
                            val isMajor = (s % 10) == 0
                            val isMid = (s % 5) == 0
                            val tickHeight = when {
                                isMajor -> 14f
                                isMid -> 9f
                                else -> 5f
                            }

                            drawLine(
                                color = if (isMajor) Color(0xFF8A6C9B).copy(alpha = 0.6f) else Color(0xFF8A6C9B).copy(alpha = 0.25f),
                                start = Offset(xPos, size.height - tickHeight),
                                end = Offset(xPos, size.height),
                                strokeWidth = if (isMajor) 1.5f else 1.0f
                            )
                        }

                        // Playhead marker on Ruler (Coral/Red triangle matching screenshot)
                        val playheadX = playheadSeconds * DP_PER_SECOND
                        val playheadColor = Color(0xFFE53935)
                        val trianglePath = Path().apply {
                            moveTo(playheadX - 6f, 0f)
                            lineTo(playheadX + 6f, 0f)
                            lineTo(playheadX, 12f)
                            close()
                        }
                        drawPath(trianglePath, color = playheadColor)
                        drawLine(
                            color = playheadColor,
                            start = Offset(playheadX, 12f),
                            end = Offset(playheadX, size.height),
                            strokeWidth = 2.2f
                        )
                    }

                    // Text labels every 10 seconds (0:00, 0:10, 0:20...)
                    Row(modifier = Modifier.width(totalWidth)) {
                        for (s in 0..RULER_TOTAL_SECONDS step 10) {
                            Box(
                                modifier = Modifier
                                    .padding(start = if (s == 0) 2.dp else (10 * DP_PER_SECOND - 24).dp)
                                    .height(18.dp)
                            ) {
                                Text(
                                    text = AudioUtils.formatRulerTime(s),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StudioTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // 3. Track Area: Fixed left header + horizontally scrolling timeline lanes
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScrollState)
                ) {
                    tracks.forEach { track ->
                        val isSelected = track.id == selectedTrackId
                        val trackColor = getTrackAccentColor(track.name)
                        val trackIcon = getTrackIcon(track.name)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(TRACK_HEIGHT)
                                .border(0.4.dp, Color(0xFFEADBEE)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Fixed Left Header Card (Matches screenshot with left highlight bar, icon, M, S, FX)
                            TrackHeaderCard(
                                track = track,
                                isSelected = isSelected,
                                accentColor = trackColor,
                                icon = trackIcon,
                                onSelect = { viewModel.selectTrack(track.id) },
                                onRename = { trackToRename = track },
                                onToggleMute = { viewModel.toggleTrackMute(track.id) },
                                onToggleSolo = { viewModel.toggleTrackSolo(track.id) },
                                onFxClick = { viewModel.navigateTo(StudioScreen.VOCAL_FX) }
                            )

                            // Horizontally Scrolling Lane
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .horizontalScroll(horizontalScrollState)
                                    .pointerInput(track.id) {
                                        detectTapGestures(
                                            onTap = { offset ->
                                                viewModel.selectTrack(track.id)
                                                val tappedSec = (offset.x / DP_PER_SECOND).coerceAtLeast(0f)
                                                viewModel.seekPlayhead(tappedSec)
                                            }
                                        )
                                    }
                            ) {
                                val laneWidth = (RULER_TOTAL_SECONDS * DP_PER_SECOND).dp

                                Canvas(
                                    modifier = Modifier
                                        .width(laneWidth)
                                        .fillMaxHeight()
                                ) {
                                    // Background lane fill
                                    drawRect(
                                        color = if (isSelected) Color.White.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.35f),
                                        size = size
                                    )

                                    // Subtle vertical timeline grid lines
                                    for (s in 0..RULER_TOTAL_SECONDS step 5) {
                                        val xPos = s * DP_PER_SECOND
                                        drawLine(
                                            color = Color(0xFF8A6C9B).copy(alpha = 0.12f),
                                            start = Offset(xPos, 0f),
                                            end = Offset(xPos, size.height),
                                            strokeWidth = 1f
                                        )
                                    }

                                    // Playhead vertical line (Coral/Red matching screenshot)
                                    val playheadX = playheadSeconds * DP_PER_SECOND
                                    drawLine(
                                        color = Color(0xFFE53935),
                                        start = Offset(playheadX, 0f),
                                        end = Offset(playheadX, size.height),
                                        strokeWidth = 2f
                                    )
                                }

                                // Clips on this lane
                                track.clips.forEach { clip ->
                                    val clipStartX = (clip.startOffsetSeconds * DP_PER_SECOND).dp
                                    val clipWidth = (clip.durationSeconds * DP_PER_SECOND).coerceAtLeast(48f).dp
                                    val isClipSelected = clip.id == selectedClipId

                                    Box(
                                        modifier = Modifier
                                            .padding(start = clipStartX)
                                            .width(clipWidth)
                                            .fillMaxHeight()
                                            .padding(vertical = 4.dp, horizontal = 1.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isClipSelected) trackColor.copy(alpha = 0.28f) else trackColor.copy(alpha = 0.15f)
                                            )
                                            .border(
                                                width = if (isClipSelected) 2.dp else 1.dp,
                                                color = if (isClipSelected) trackColor else trackColor.copy(alpha = 0.55f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                viewModel.selectTrack(track.id)
                                                viewModel.selectClip(clip.id)
                                            }
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = clip.name,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = trackColor,
                                                maxLines = 1
                                            )

                                            ClipWaveformCanvas(
                                                waveform = clip.waveformPoints,
                                                color = trackColor,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(28.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Action Chips Row (Horizontally scrollable, spacious, unsqueezed icons)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.65f))
                    .border(0.5.dp, Color(0xFFEADBEE))
                    .padding(vertical = 6.dp, horizontal = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Split
                UnsquishedActionChip(
                    title = "Split",
                    icon = Icons.Default.ContentCut,
                    isEnabled = selectedClipId != null,
                    onClick = { viewModel.splitSelectedClip() }
                )

                // Duplicate
                UnsquishedActionChip(
                    title = "Duplicate",
                    icon = Icons.Default.ContentCopy,
                    isEnabled = selectedClipId != null,
                    onClick = { viewModel.duplicateSelectedClip() }
                )

                // Delete
                UnsquishedActionChip(
                    title = "Delete",
                    icon = Icons.Default.Delete,
                    isEnabled = selectedClipId != null,
                    tintColor = if (selectedClipId != null) Color(0xFFD32F2F) else StudioTextMuted,
                    onClick = { viewModel.deleteSelectedClip() }
                )

                // Import
                UnsquishedActionChip(
                    title = "Import",
                    icon = Icons.Default.FileDownload,
                    isEnabled = true,
                    tintColor = StudioPrimary,
                    onClick = { audioPickerLauncher.launch("audio/*") }
                )

                // Loop
                UnsquishedActionChip(
                    title = "Loop",
                    icon = Icons.Default.Repeat,
                    isEnabled = true,
                    isActive = isLoopOn,
                    onClick = { viewModel.toggleLoop() }
                )

                // Region
                UnsquishedActionChip(
                    title = "Region",
                    icon = Icons.Default.Crop,
                    isEnabled = false,
                    onClick = {}
                )

                // Take
                UnsquishedActionChip(
                    title = "Take ▸",
                    icon = Icons.Default.Layers,
                    isEnabled = false,
                    onClick = {}
                )
            }

            // 5. Transport Bar (Matches screenshot layout: BPM, Rewind, Big glowing Mic REC, Play/Stop, Timecode)
            PinnedTransportBar(
                bpm = activeProject.bpm,
                isMetronomeOn = isMetronomeOn,
                isPlaying = isPlaying,
                isRecording = isRecording,
                playheadSeconds = playheadSeconds,
                onMetronomeToggle = { viewModel.toggleMetronome() },
                onRewind = { viewModel.rewindPlayhead() },
                onRecToggle = {
                    val hasMicPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasMicPermission) {
                        if (isRecording) {
                            viewModel.stopMultitrackRecording()
                        } else {
                            viewModel.startMultitrackRecording()
                        }
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onPlayToggle = { viewModel.toggleMultitrackPlay() }
            )

            // 6. Studio Bottom Navigation Bar (Matches screenshot tabs: Record, Mix, Pads, FX, Export)
            StudioMixerBottomNav(
                currentScreen = StudioScreen.MIXER,
                onNavigate = { viewModel.navigateTo(it) }
            )
        }
    }

    // Dialog: Rename Project
    if (showRenameProjectDialog) {
        var newTitle by remember { mutableStateOf(activeProject.title) }
        AlertDialog(
            onDismissRequest = { showRenameProjectDialog = false },
            title = { Text("Rename Project", fontWeight = FontWeight.Bold, color = StudioTextPrimary) },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = StudioPrimary)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            viewModel.renameProject(activeProject, newTitle)
                        }
                        showRenameProjectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRenameProjectDialog = false }) {
                    Text("Cancel", color = StudioTextSecondary)
                }
            },
            containerColor = StudioCardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Dialog: Add Track
    if (showAddTrackDialog) {
        var trackName by remember { mutableStateOf("Audio ${tracks.size + 1}") }
        val presets = listOf("Beat", "Main Vocal", "Ad-Libs", "Doubles", "Harmonies")

        AlertDialog(
            onDismissRequest = { showAddTrackDialog = false },
            title = { Text("Add Track", fontWeight = FontWeight.Bold, color = StudioTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = trackName,
                        onValueChange = { trackName = it },
                        label = { Text("Track Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = StudioPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Presets:", fontSize = 11.sp, color = StudioTextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.take(3).forEach { p ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFEDE4EE))
                                    .clickable { trackName = p }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(p, fontSize = 11.sp, color = StudioTextPrimary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addTrack(trackName)
                        showAddTrackDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary)
                ) {
                    Text("Add", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddTrackDialog = false }) {
                    Text("Cancel", color = StudioTextSecondary)
                }
            },
            containerColor = StudioCardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Dialog: Rename Track
    trackToRename?.let { track ->
        var renameText by remember { mutableStateOf(track.name) }
        AlertDialog(
            onDismissRequest = { trackToRename = null },
            title = { Text("Rename Track", fontWeight = FontWeight.Bold, color = StudioTextPrimary) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = StudioPrimary)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            viewModel.renameTrack(track.id, renameText)
                        }
                        trackToRename = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { trackToRename = null }) {
                    Text("Cancel", color = StudioTextSecondary)
                }
            },
            containerColor = StudioCardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Dialog: Delete Track Confirmation
    trackToDelete?.let { track ->
        AlertDialog(
            onDismissRequest = { trackToDelete = null },
            title = { Text("Delete Track?", fontWeight = FontWeight.Bold, color = StudioTextPrimary) },
            text = { Text("Delete \"${track.name}\" and all its audio clips? This cannot be undone.", color = StudioTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTrack(track.id)
                        trackToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { trackToDelete = null }) {
                    Text("Cancel", color = StudioTextSecondary)
                }
            },
            containerColor = StudioCardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

/**
 * Track Header card matching the screenshot layout:
 * - Selected track gets a solid accent bar on the left edge (like the coral bar on MAIN VO in screenshot 2).
 * - Track icon and track name in distinct accent color.
 * - Row of three buttons: M, S, FX.
 */
@Composable
private fun TrackHeaderCard(
    track: StudioTrack,
    isSelected: Boolean,
    accentColor: Color,
    icon: ImageVector,
    onSelect: () -> Unit,
    onRename: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSolo: () -> Unit,
    onFxClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(HEADER_WIDTH)
            .fillMaxHeight()
            .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.85f))
            .clickable(onClick = onSelect)
    ) {
        // Prominent colored highlight bar on the left edge when selected (Matches Screenshot 2)
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = if (isSelected) 8.dp else 6.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Track Icon + Name in bold all-caps (Matches screenshot)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onRename)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = track.name.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    maxLines = 1
                )
            }

            // Row of M, S, FX buttons (Matches screenshot)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // M (Mute)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (track.isMuted) Color(0xFF8A6C9B) else Color(0xFFEDE4EE))
                        .clickable(onClick = onToggleMute),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "M",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (track.isMuted) Color.White else Color(0xFF4A3457)
                    )
                }

                // S (Solo)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (track.isSoloed) Color(0xFF43A047) else Color(0xFFEDE4EE))
                        .clickable(onClick = onToggleSolo),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (track.isSoloed) Color.White else Color(0xFF4A3457)
                    )
                }

                // FX button
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFEDE4EE))
                        .clickable(onClick = onFxClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "FX",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioPrimary
                    )
                }
            }
        }
    }
}

/**
 * Draws real normalized waveform peak points into an audio block canvas.
 */
@Composable
private fun ClipWaveformCanvas(
    waveform: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (waveform.isEmpty()) {
            drawLine(
                color = color.copy(alpha = 0.6f),
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = 1.5f
            )
            return@Canvas
        }

        val stepX = size.width / waveform.size.coerceAtLeast(1)
        val midY = size.height / 2

        waveform.forEachIndexed { index, amp ->
            val barHeight = (amp * (size.height * 0.9f)).coerceAtLeast(3f)
            val xPos = index * stepX + (stepX / 2)
            drawLine(
                color = color,
                start = Offset(xPos, midY - barHeight / 2),
                end = Offset(xPos, midY + barHeight / 2),
                strokeWidth = maxOf(stepX * 0.75f, 1.5f)
            )
        }
    }
}

/**
 * Action chip for the timeline action row.
 * Specifically avoids squeezed/squished icons with fixed square size and comfortable padding.
 */
@Composable
private fun UnsquishedActionChip(
    title: String,
    icon: ImageVector,
    isEnabled: Boolean,
    isActive: Boolean = false,
    tintColor: Color = StudioPrimary,
    onClick: () -> Unit
) {
    val bgColor = when {
        isActive -> StudioPrimary
        isEnabled -> Color.White
        else -> Color.White.copy(alpha = 0.40f)
    }

    val contentColor = when {
        isActive -> Color.White
        isEnabled -> tintColor
        else -> StudioTextMuted
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = 0.8.dp,
                color = if (isEnabled || isActive) contentColor.copy(alpha = 0.35f) else Color(0xFFEADBEE),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp) // Fixed square icon, NEVER squeezed
            )
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isEnabled || isActive) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isActive) Color.White else if (isEnabled) StudioTextPrimary else StudioTextMuted,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

/**
 * Pinned Transport Bar at the bottom of the screen matching screenshot:
 * 1. BPM with Metronome toggle
 * 2. Rewind button
 * 3. Big round glowing REC button with mic icon
 * 4. Play/Stop toggle button
 * 5. Timecode display (00:00.00)
 */
@Composable
private fun PinnedTransportBar(
    bpm: Int,
    isMetronomeOn: Boolean,
    isPlaying: Boolean,
    isRecording: Boolean,
    playheadSeconds: Float,
    onMetronomeToggle: () -> Unit,
    onRewind: () -> Unit,
    onRecToggle: () -> Unit,
    onPlayToggle: () -> Unit
) {
    val recTransition = rememberInfiniteTransition(label = "recPulse")
    val recPulseAlpha by recTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.40f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "recAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFBF8FCFE))
            .border(0.6.dp, Color(0xFFEADBEE))
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. BPM with Metronome Toggle (Stacked like screenshot: metronome icon + 90 BPM)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isMetronomeOn) StudioPrimary.copy(alpha = 0.20f) else Color(0xFFEDE4EE))
                    .clickable(onClick = onMetronomeToggle)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Metronome",
                        tint = if (isMetronomeOn) StudioPrimary else StudioTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "$bpm BPM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMetronomeOn) StudioPrimary else StudioTextPrimary
                    )
                }
            }

            // 2. Rewind Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEDE4EE))
                    .clickable(onClick = onRewind),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FastRewind,
                    contentDescription = "Rewind to Start",
                    tint = StudioTextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // 3. Big Round REC Button with Mic Icon (Prominent coral/red glowing circular button from screenshot)
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape, spotColor = Color(0x66E53935))
                    .clip(CircleShape)
                    .background(if (isRecording) Color(0xFFD32F2F).copy(alpha = recPulseAlpha) else Color(0xFFE53935))
                    .border(2.5.dp, Color.White, CircleShape)
                    .clickable(onClick = onRecToggle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = if (isRecording) "Stop Recording" else "Record",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            // 4. Play / Stop Toggle Button (Matches screenshot round play button)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(StudioPrimary)
                    .clickable(onClick = onPlayToggle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying || isRecording) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying || isRecording) "Stop" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            // 5. Timecode Display (00:00.00 in monospaced bold digital font)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(0.8.dp, Color(0xFFE2D6E6), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = AudioUtils.formatTimecode(playheadSeconds),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioTextPrimary
                )
            }
        }
    }
}

/**
 * Studio Bottom Navigation Bar for Mixer screen (Matches screenshot tabs: Record, Mix, Pads, FX, Export)
 */
@Composable
private fun StudioMixerBottomNav(
    currentScreen: StudioScreen,
    onNavigate: (StudioScreen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xF5FAF5FB))
            .border(0.6.dp, Color(0xFFEADBEE))
            .navigationBarsPadding()
            .padding(vertical = 4.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.Mic,
                label = "Record",
                isSelected = currentScreen == StudioScreen.RECORDING_SESSION,
                onClick = { onNavigate(StudioScreen.RECORDING_SESSION) }
            )

            BottomNavItem(
                icon = Icons.Default.Tune,
                label = "Mix",
                isSelected = currentScreen == StudioScreen.MIXER,
                onClick = { onNavigate(StudioScreen.MIXER) }
            )

            BottomNavItem(
                icon = Icons.Default.GridOn,
                label = "Pads",
                isSelected = currentScreen == StudioScreen.DRUM_PADS,
                onClick = { onNavigate(StudioScreen.DRUM_PADS) }
            )

            BottomNavItem(
                icon = Icons.Default.AutoAwesome,
                label = "FX",
                isSelected = currentScreen == StudioScreen.VOCAL_FX,
                onClick = { onNavigate(StudioScreen.VOCAL_FX) }
            )

            BottomNavItem(
                icon = Icons.Default.FileDownload,
                label = "Export",
                isSelected = currentScreen == StudioScreen.EXPORT_SHARE,
                onClick = { onNavigate(StudioScreen.EXPORT_SHARE) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = Color(0xFFE53935) // Coral/red tint when active like screenshot
    val inactiveColor = StudioTextSecondary

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) activeColor else inactiveColor
        )
    }
}

/**
 * Returns distinct accent colors matching the screenshot track types.
 */
private fun getTrackAccentColor(trackName: String): Color {
    val name = trackName.lowercase()
    return when {
        name.contains("beat") || name.contains("drum") -> Color(0xFF0097A7) // Cyan (BEAT in screenshot)
        name.contains("vocal") || name.contains("lead") || name.contains("mic") -> Color(0xFFE53935) // Coral / Red (MAIN VO in screenshot)
        name.contains("ad-lib") || name.contains("adlib") || name.contains("sparkle") -> Color(0xFF8E24AA) // Purple (AD-LIBS in screenshot)
        name.contains("double") || name.contains("layer") -> Color(0xFF00897B) // Teal (DOUBLES in screenshot)
        name.contains("harmon") -> Color(0xFFF57C00) // Amber / Gold (HARMONIES in screenshot)
        name.contains("bass") || name.contains("808") -> Color(0xFFC2185B) // Magenta
        else -> Color(0xFF6B4E82) // Studio Purple
    }
}

/**
 * Returns distinct icons matching the screenshot track types.
 */
private fun getTrackIcon(trackName: String): ImageVector {
    val name = trackName.lowercase()
    return when {
        name.contains("beat") || name.contains("drum") -> Icons.Default.GraphicEq
        name.contains("vocal") || name.contains("lead") || name.contains("mic") -> Icons.Default.Mic
        name.contains("ad-lib") || name.contains("adlib") || name.contains("sparkle") -> Icons.Default.AutoAwesome
        name.contains("double") || name.contains("layer") -> Icons.Default.Layers
        name.contains("harmon") -> Icons.Default.MusicNote
        name.contains("bass") || name.contains("808") -> Icons.Default.GraphicEq
        else -> Icons.Default.Audiotrack
    }
}
