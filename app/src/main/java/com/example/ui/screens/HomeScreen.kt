package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProjectEntity
import com.example.ui.components.StudioBottomNavBar
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioNavTab
import com.example.ui.components.StudioScreenBackground
import com.example.ui.components.WaveformView
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.viewmodel.StudioScreen
import com.example.viewmodel.StudioViewModel

/**
 * Home & Projects screen matching user reference Images 3 and 6.
 * Features Continue banner with interactive waveform, search, and dual List/Grid project view.
 */
@Composable
fun HomeScreen(viewModel: StudioViewModel) {
    val projectsList by viewModel.projects.collectAsState(initial = emptyList())
    val activeProject by viewModel.activeProject.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playProgress by viewModel.playbackProgress.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isGridView by remember { mutableStateOf(false) }

    val filteredProjects = projectsList.filter {
        it.title.contains(searchQuery, ignoreCase = true) || it.subtitle.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        bottomBar = {
            StudioBottomNavBar(
                currentTab = StudioNavTab.HOME,
                onTabSelected = { tab ->
                    when (tab) {
                        StudioNavTab.HOME -> viewModel.navigateTo(StudioScreen.HOME)
                        StudioNavTab.PROJECTS -> viewModel.navigateTo(StudioScreen.MIXER)
                        StudioNavTab.CREATE -> viewModel.navigateTo(StudioScreen.RECORDING_SESSION)
                        StudioNavTab.LIBRARY -> viewModel.navigateTo(StudioScreen.INSTRUMENTS)
                        StudioNavTab.PROFILE -> viewModel.navigateTo(StudioScreen.OFFLINE_GUIDE)
                    }
                }
            )
        }
    ) { innerPadding ->
        StudioScreenBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Header (STUDIO with waveform icon, notifications, profile)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(StudioPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Logo",
                                tint = StudioPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "STUDIO",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = StudioTextPrimary
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(StudioCardBg)
                                .clickable { viewModel.navigateTo(StudioScreen.OFFLINE_GUIDE) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = StudioTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Profile Avatar Pill (J)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCC8E4))
                                .clickable { viewModel.navigateTo(StudioScreen.OFFLINE_GUIDE) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "J",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text("Search projects...", color = StudioTextMuted, fontSize = 14.sp)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = StudioTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = StudioCardBg,
                        unfocusedContainerColor = StudioCardBg,
                        focusedBorderColor = StudioPrimary.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color(0xFFEADBEE)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Continue Hero Card (Image 3 & 6)
                StudioCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    elevation = 2.dp,
                    onClick = { viewModel.navigateTo(StudioScreen.MIXER) }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Continue",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = StudioTextSecondary
                        )
                        Text(
                            text = "${activeProject.title} • ${activeProject.subtitle}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioTextPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            WaveformView(
                                progress = if (isPlaying) playProgress else 0.35f,
                                isPlaying = isPlaying,
                                modifier = Modifier.weight(1f),
                                onSeek = { viewModel.startPlayback() }
                            )

                            // Play Button
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(StudioPrimary)
                                    .clickable { viewModel.togglePlay() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play continue",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Projects Section Header with Grid/List Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Projects",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isGridView) StudioPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { isGridView = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ViewList,
                                contentDescription = "List View",
                                tint = if (!isGridView) StudioPrimary else StudioTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isGridView) StudioPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { isGridView = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ViewModule,
                                contentDescription = "Grid View",
                                tint = if (isGridView) StudioPrimary else StudioTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Projects List or Grid
                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredProjects) { project ->
                            ProjectGridCard(project = project, onClick = { viewModel.selectProject(project) })
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredProjects) { project ->
                            ProjectListCard(project = project, onClick = { viewModel.selectProject(project) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectListCard(project: ProjectEntity, onClick: () -> Unit) {
    StudioCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category icon box
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEDE4EE)),
                    contentAlignment = Alignment.Center
                ) {
                    val icon: ImageVector = when (project.category) {
                        "Vocals" -> Icons.Default.Mic
                        "Keys" -> Icons.Default.MusicNote
                        else -> Icons.Default.GraphicEq
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = project.category,
                        tint = StudioPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = project.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StudioTextPrimary
                    )
                    Text(
                        text = "${project.lastEditedDisplay} • ${project.bpm} BPM • ${project.durationFormatted}",
                        fontSize = 11.sp,
                        color = StudioTextSecondary
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = "Options",
                tint = StudioTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ProjectGridCard(project: ProjectEntity, onClick: () -> Unit) {
    // Pastel tint per card (Image 6 style)
    val cardBg = when (project.category) {
        "Lo-Fi" -> Color(0xFFC7B6C8).copy(alpha = 0.5f)
        "Vocals" -> Color(0xFFDEC5C8).copy(alpha = 0.55f)
        "Pads" -> Color(0xFFD3CCE8).copy(alpha = 0.55f)
        else -> Color(0xFFD5CBD8).copy(alpha = 0.55f)
    }

    StudioCard(
        modifier = Modifier.height(130.dp),
        shape = RoundedCornerShape(22.dp),
        backgroundColor = cardBg,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            val icon: ImageVector = when (project.category) {
                "Vocals" -> Icons.Default.Mic
                "Keys" -> Icons.Default.MusicNote
                else -> Icons.Default.GraphicEq
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = StudioPrimary,
                modifier = Modifier.size(24.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = project.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioTextPrimary
                )
                Text(
                    text = "${project.bpm} BPM • ${project.durationFormatted}",
                    fontSize = 11.sp,
                    color = StudioTextSecondary
                )
            }
        }
    }
}
