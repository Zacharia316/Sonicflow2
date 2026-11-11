package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioScreenBackground
import com.example.ui.theme.StudioNoteBlock
import com.example.ui.theme.StudioNoteBorder
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioRecordRed
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.viewmodel.StudioScreen
import com.example.viewmodel.StudioViewModel

/**
 * Piano Roll screen matching user reference Image 1.
 * Features full interactive note grid, piano keys on left, tempo/quantize/velocity pills,
 * and live playhead scrubber.
 */
@Composable
fun PianoRollScreen(viewModel: StudioViewModel) {
    val bpm by viewModel.bpm.collectAsState()
    val quantize by viewModel.quantize.collectAsState()
    val velocity by viewModel.velocity.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    val notes by viewModel.pianoNotes.collectAsState()

    // MIDI Note range to display: B4 (71) down to C3 (48) = 24 semi-tones
    val visibleNotes = (71 downTo 48).toList()
    val totalSteps = 32 // 8 beats of 16th notes (2 bars)
    val stepWidthDp = 24.dp
    val keyHeightDp = 18.dp

    StudioScreenBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Top Bar: Back, "Piano Roll - Grand Piano", [BPM 140], [Quantize 1/16], [Velocity 85]
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.6f))
                            .clickable { viewModel.navigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = StudioTextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "Piano Roll - Grand Piano",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary
                    )
                }

                // Controls pills (Image 1 top right)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFEDE4EE))
                            .clickable {
                                val nextBpm = if (bpm >= 160) 90 else bpm + 10
                                viewModel.setBpm(nextBpm)
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text("BPM $bpm", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StudioTextPrimary)
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFEDE4EE))
                            .clickable {
                                val nextQ = when (quantize) {
                                    "1/16" -> "1/8"
                                    "1/8" -> "1/4"
                                    else -> "1/16"
                                }
                                viewModel.setQuantize(nextQ)
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text("Quantize $quantize", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StudioTextPrimary)
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFEDE4EE))
                            .clickable {
                                val nextV = if (velocity >= 120) 70 else velocity + 15
                                viewModel.setVelocity(nextV)
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text("Velocity $velocity", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StudioTextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Editor: Left Piano Keys + Scrollable Note Grid (Image 1)
            StudioCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Piano Keyboard (White and Black Keys)
                    Column(
                        modifier = Modifier
                            .width(52.dp)
                            .fillMaxHeight()
                            .background(Color.White)
                            .border(0.5.dp, Color(0xFFDED0E2))
                    ) {
                        visibleNotes.forEach { midi ->
                            val isBlack = isBlackKey(midi)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(keyHeightDp)
                                    .background(if (isBlack) Color(0xFF423447) else Color.White)
                                    .border(0.3.dp, Color(0xFFE4D5E8))
                                    .clickable {
                                        viewModel.postStatus("Key: ${StudioViewModel.midiToNoteName(midi)}")
                                    },
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                if (midi % 12 == 0) { // C notes
                                    Text(
                                        text = "C${(midi / 12) - 1}",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isBlack) Color.White else StudioTextSecondary,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Right Note Grid with measure ruler and interactive notes
                    val scrollState = rememberScrollState()
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(scrollState)
                    ) {
                        Column {
                            // Timeline Measure Numbers (1, 2, 3, 4, 5, 6, 7, 8)
                            Row(
                                modifier = Modifier
                                    .height(20.dp)
                                    .background(Color(0xFFF3EAF4))
                            ) {
                                for (bar in 1..8) {
                                    Box(
                                        modifier = Modifier
                                            .width(stepWidthDp * 4)
                                            .fillMaxHeight(),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = "$bar",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StudioTextSecondary,
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }
                                }
                            }

                            // Interactive Note Canvas
                            Canvas(
                                modifier = Modifier
                                    .width(stepWidthDp * totalSteps)
                                    .height(keyHeightDp * visibleNotes.size)
                                    .pointerInput(Unit) {
                                        detectTapGestures { offset ->
                                            val stepWidthPx = stepWidthDp.toPx()
                                            val keyHeightPx = keyHeightDp.toPx()

                                            val clickedStep = (offset.x / stepWidthPx).toInt().coerceIn(0, totalSteps - 1)
                                            val keyIndex = (offset.y / keyHeightPx).toInt().coerceIn(0, visibleNotes.size - 1)
                                            val clickedMidi = visibleNotes[keyIndex]

                                            viewModel.addOrRemoveNote(clickedMidi, clickedStep)
                                        }
                                    }
                            ) {
                                val stepW = stepWidthDp.toPx()
                                val keyH = keyHeightDp.toPx()

                                // Background horizontal lanes
                                for (i in visibleNotes.indices) {
                                    val y = i * keyH
                                    val midi = visibleNotes[i]
                                    val rowColor = if (isBlackKey(midi)) Color(0xFFF0E5F2) else Color(0xFFF9F3F9)
                                    drawRect(
                                        color = rowColor,
                                        topLeft = Offset(0f, y),
                                        size = Size(size.width, keyH)
                                    )
                                    // Row divider
                                    drawLine(
                                        color = Color(0xFFEADBEE),
                                        start = Offset(0f, y + keyH),
                                        end = Offset(size.width, y + keyH),
                                        strokeWidth = 0.5f
                                    )
                                }

                                // Vertical beat lines
                                for (step in 0..totalSteps) {
                                    val x = step * stepW
                                    val isBarLine = step % 4 == 0
                                    drawLine(
                                        color = if (isBarLine) Color(0xFFC7B1D0) else Color(0xFFEADBEE),
                                        start = Offset(x, 0f),
                                        end = Offset(x, size.height),
                                        strokeWidth = if (isBarLine) 1.2f else 0.5f
                                    )
                                }

                                // Drawn Note Blocks (Mauve pills)
                                notes.forEach { note ->
                                    val keyIdx = visibleNotes.indexOf(note.midiNote)
                                    if (keyIdx != -1) {
                                        val x = note.startStep * stepW
                                        val y = keyIdx * keyH
                                        val w = note.lengthSteps * stepW - 2f
                                        val h = keyH - 2f

                                        drawRoundRect(
                                            color = StudioNoteBlock,
                                            topLeft = Offset(x + 1f, y + 1f),
                                            size = Size(w, h),
                                            cornerRadius = CornerRadius(4.dp.toPx())
                                        )
                                        drawRoundRect(
                                            color = StudioNoteBorder,
                                            topLeft = Offset(x + 1f, y + 1f),
                                            size = Size(w, h),
                                            cornerRadius = CornerRadius(4.dp.toPx()),
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.8f)
                                        )
                                    }
                                }

                                // Playhead Scrubber Line & Triangle Marker (Image 1 style)
                                if (isPlaying) {
                                    val playheadX = currentStep * stepW
                                    // Pink/red playhead line
                                    drawLine(
                                        color = Color(0xFFD85D78),
                                        start = Offset(playheadX, 0f),
                                        end = Offset(playheadX, size.height),
                                        strokeWidth = 2.dp.toPx()
                                    )
                                    // Playhead top triangle marker
                                    val trianglePath = androidx.compose.ui.graphics.Path().apply {
                                        moveTo(playheadX - 6.dp.toPx(), 0f)
                                        lineTo(playheadX + 6.dp.toPx(), 0f)
                                        lineTo(playheadX, 8.dp.toPx())
                                        close()
                                    }
                                    drawPath(trianglePath, Color(0xFFD85D78))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Transport Bar (Play, Record, Loop, Undo) - Image 1
            StudioCard(
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                backgroundColor = Color(0xF5FFFFFF)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play
                    Row(
                        modifier = Modifier
                            .clickable { viewModel.togglePlay() }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = StudioPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isPlaying) "Stop" else "Play",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StudioTextPrimary
                        )
                    }

                    // Record
                    Row(
                        modifier = Modifier
                            .clickable { viewModel.navigateTo(StudioScreen.RECORDING_SESSION) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(StudioRecordRed)
                        )
                        Text(
                            text = "Record",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StudioRecordRed
                        )
                    }

                    // Loop
                    Row(
                        modifier = Modifier
                            .clickable { viewModel.toggleLoop() }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Loop,
                            contentDescription = "Loop",
                            tint = StudioPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Loop",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StudioTextPrimary
                        )
                    }

                    // Undo
                    Row(
                        modifier = Modifier
                            .clickable { viewModel.undoLastNote() }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = StudioTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Undo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StudioTextSecondary
                        )
                    }
                }
            }
        }
    }
}

private fun isBlackKey(midiNote: Int): Boolean {
    val noteInOctave = midiNote % 12
    return noteInOctave == 1 || noteInOctave == 3 || noteInOctave == 6 || noteInOctave == 8 || noteInOctave == 10
}
