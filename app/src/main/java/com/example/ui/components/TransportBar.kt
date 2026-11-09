package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioPillInactive
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioRecordRed
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary

/**
 * DAW playback transport bar: REC, PLAY/PAUSE, STOP, METRO, LOOP.
 * Matches Image 9 bottom bar.
 */
@Composable
fun TransportBar(
    isPlaying: Boolean,
    isRecording: Boolean,
    isMetronomeOn: Boolean,
    isLoopOn: Boolean,
    onRecordClick: () -> Unit,
    onPlayClick: () -> Unit,
    onStopClick: () -> Unit,
    onMetronomeToggle: () -> Unit,
    onLoopToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    StudioCard(
        modifier = modifier.fillMaxWidth(),
        shape = CircleShape,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        backgroundColor = Color(0xF7FFFFFF)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // REC Button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isRecording) StudioRecordRed else Color(0xFFE57373).copy(alpha = 0.2f))
                    .clickable(onClick = onRecordClick)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isRecording) Color.White else StudioRecordRed)
                    )
                    Text(
                        text = "REC",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRecording) Color.White else StudioRecordRed
                    )
                }
            }

            // PLAY Button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isPlaying) StudioPrimary else StudioPillInactive)
                    .clickable(onClick = onPlayClick)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = if (isPlaying) Color.White else StudioTextPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isPlaying) "PAUSE" else "PLAY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPlaying) Color.White else StudioTextPrimary
                    )
                }
            }

            // STOP Button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(StudioPillInactive)
                    .clickable(onClick = onStopClick)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = StudioTextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "STOP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StudioTextSecondary
                    )
                }
            }

            // METRO Toggle
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isMetronomeOn) StudioPrimary else Color.Transparent)
                    .clickable(onClick = onMetronomeToggle)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "METRO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isMetronomeOn) Color.White else StudioTextSecondary
                )
            }

            // LOOP Toggle
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isLoopOn) StudioPrimary else Color.Transparent)
                    .clickable(onClick = onLoopToggle)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Loop,
                        contentDescription = "Loop",
                        tint = if (isLoopOn) Color.White else StudioTextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "LOOP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isLoopOn) Color.White else StudioTextSecondary
                    )
                }
            }
        }
    }
}

/**
 * Bottom App Navigation Bar: Home, Projects, Create, Library, Profile.
 * Matches Image 3 & Image 6.
 */
@Composable
fun StudioBottomNavBar(
    currentTab: StudioNavTab,
    onTabSelected: (StudioNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xE6FAF5FB))
            .border(0.5.dp, StudioCardBorder)
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavButton(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = currentTab == StudioNavTab.HOME,
                onClick = { onTabSelected(StudioNavTab.HOME) }
            )
            NavButton(
                icon = Icons.Default.Folder,
                label = "Projects",
                isSelected = currentTab == StudioNavTab.PROJECTS,
                onClick = { onTabSelected(StudioNavTab.PROJECTS) }
            )

            // Centered prominent Create (+) button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(StudioPrimary)
                    .clickable { onTabSelected(StudioNavTab.CREATE) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            NavButton(
                icon = Icons.AutoMirrored.Filled.QueueMusic,
                label = "Library",
                isSelected = currentTab == StudioNavTab.LIBRARY,
                onClick = { onTabSelected(StudioNavTab.LIBRARY) }
            )
            NavButton(
                icon = Icons.Default.Person,
                label = "Profile",
                isSelected = currentTab == StudioNavTab.PROFILE,
                onClick = { onTabSelected(StudioNavTab.PROFILE) }
            )
        }
    }
}

@Composable
private fun NavButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    androidx.compose.foundation.layout.Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) StudioPrimary else StudioTextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) StudioPrimary else StudioTextSecondary
        )
    }
}

enum class StudioNavTab {
    HOME,
    PROJECTS,
    CREATE,
    LIBRARY,
    PROFILE
}
