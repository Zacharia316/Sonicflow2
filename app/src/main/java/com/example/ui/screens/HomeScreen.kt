package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
 * Home & Projects screen matching user reference images with lavender/mauve neumorphic theme.
 * Edge-to-edge layout with status bar insets padding, real Room-persisted projects,
 * real Continue card with reopen action, search, create project dialog, and rename/delete options.
 */
@Composable
fun HomeScreen(viewModel: StudioViewModel) {
    val projectsList by viewModel.projects.collectAsState(initial = emptyList())
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playProgress by viewModel.playbackProgress.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isGridView by remember { mutableStateOf(false) }

    // Dialog control states
    var showCreateDialog by remember { mutableStateOf(false) }
    var projectToRename by remember { mutableStateOf<ProjectEntity?>(null) }
    var projectToDelete by remember { mutableStateOf<ProjectEntity?>(null) }

    val filteredProjects = projectsList.filter {
        it.title.contains(searchQuery, ignoreCase = true) || it.subtitle.contains(searchQuery, ignoreCase = true)
    }

    val mostRecentProject = projectsList.firstOrNull()

    StudioScreenBackground(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                StudioBottomNavBar(
                    currentTab = StudioNavTab.HOME,
                    modifier = Modifier.navigationBarsPadding(),
                    onTabSelected = { tab ->
                        when (tab) {
                            StudioNavTab.HOME -> viewModel.navigateTo(StudioScreen.HOME)
                            StudioNavTab.PROJECTS -> viewModel.navigateTo(StudioScreen.MIXER)
                            StudioNavTab.CREATE -> showCreateDialog = true
                            StudioNavTab.LIBRARY -> viewModel.navigateTo(StudioScreen.INSTRUMENTS)
                            StudioNavTab.PROFILE -> viewModel.navigateTo(StudioScreen.OFFLINE_GUIDE)
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                // Header (STUDIO with waveform icon, notifications, profile avatar)
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
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = StudioTextMuted,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { searchQuery = "" }
                            )
                        }
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

                // Continue Hero Card or Friendly Empty State
                if (mostRecentProject != null) {
                    StudioCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        elevation = 2.dp,
                        onClick = { viewModel.selectProject(mostRecentProject) }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Continue",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = StudioTextSecondary
                                )

                                // Reopen button badge
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(StudioPrimary.copy(alpha = 0.12f))
                                        .clickable { viewModel.selectProject(mostRecentProject) }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                            contentDescription = "Reopen",
                                            tint = StudioPrimary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "Reopen",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = StudioPrimary
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "${mostRecentProject.title} • ${mostRecentProject.subtitle}",
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

                                // Play / Pause Button
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(StudioPrimary)
                                        .clickable { viewModel.togglePlay() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play continue",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Friendly Empty State Card when no projects exist yet
                    StudioCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        elevation = 2.dp,
                        onClick = { showCreateDialog = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(StudioPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "New Project",
                                    tint = StudioPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "No projects yet, tap + to start",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StudioTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Start composing beats, vocals, and melodies offline",
                                    fontSize = 12.sp,
                                    color = StudioTextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Projects Section Header with '+' button and Grid/List Toggle
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

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick + button to create new project
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(StudioPrimary.copy(alpha = 0.15f))
                                .clickable { showCreateDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create Project",
                                tint = StudioPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // List View Toggle
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

                        // Grid View Toggle
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

                // Real Projects List or Grid
                if (filteredProjects.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 28.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (searchQuery.isNotEmpty()) Icons.Default.Search else Icons.Default.Folder,
                                contentDescription = null,
                                tint = StudioTextMuted,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No projects matching \"$searchQuery\"" else "No saved projects yet",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = StudioTextSecondary
                            )
                            if (searchQuery.isEmpty()) {
                                OutlinedButton(
                                    onClick = { showCreateDialog = true },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = StudioPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Create Project", color = StudioPrimary)
                                }
                            }
                        }
                    }
                } else if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredProjects, key = { it.id }) { project ->
                            ProjectGridCard(
                                project = project,
                                onClick = { viewModel.selectProject(project) },
                                onRename = { projectToRename = project },
                                onDelete = { projectToDelete = project }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredProjects, key = { it.id }) { project ->
                            ProjectListCard(
                                project = project,
                                onClick = { viewModel.selectProject(project) },
                                onRename = { projectToRename = project },
                                onDelete = { projectToDelete = project }
                            )
                        }
                    }
                }
            }
        }
    }

    // Create New Project Dialog
    if (showCreateDialog) {
        CreateProjectDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, bpm ->
                viewModel.createProject(title = title, bpm = bpm)
                showCreateDialog = false
            }
        )
    }

    // Rename Project Dialog
    projectToRename?.let { project ->
        RenameProjectDialog(
            currentTitle = project.title,
            onDismiss = { projectToRename = null },
            onConfirm = { newTitle ->
                viewModel.renameProject(project, newTitle)
                projectToRename = null
            }
        )
    }

    // Delete Confirmation Dialog
    projectToDelete?.let { project ->
        DeleteProjectDialog(
            project = project,
            onDismiss = { projectToDelete = null },
            onConfirm = {
                viewModel.deleteProject(project)
                projectToDelete = null
            }
        )
    }
}

@Composable
private fun ProjectListCard(
    project: ProjectEntity,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

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
                modifier = Modifier.weight(1f),
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

            // Options menu (⋯)
            Box {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { menuExpanded = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "Options",
                        tint = StudioTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(StudioCardBg)
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename", color = StudioTextPrimary, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = StudioPrimary, modifier = Modifier.size(18.dp))
                        },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color(0xFFD32F2F), fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                        },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectGridCard(
    project: ProjectEntity,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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

                // ⋯ Options Menu
                Box {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable { menuExpanded = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "Options",
                            tint = StudioTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(StudioCardBg)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Rename", color = StudioTextPrimary, fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = StudioPrimary, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                menuExpanded = false
                                onRename()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color(0xFFD32F2F), fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = project.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioTextPrimary,
                    maxLines = 1
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

@Composable
private fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, bpm: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var bpmText by remember { mutableStateOf("120") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Project",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = StudioTextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Name your track and set the project tempo:",
                    fontSize = 13.sp,
                    color = StudioTextSecondary
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Project Title") },
                    placeholder = { Text("e.g. Afrobeat Groove") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioPrimary,
                        unfocusedBorderColor = Color(0xFFD5CBD8)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bpmText,
                    onValueChange = { bpmText = it.filter { ch -> ch.isDigit() }.take(3) },
                    label = { Text("BPM (Tempo)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioPrimary,
                        unfocusedBorderColor = Color(0xFFD5CBD8)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bpmVal = bpmText.toIntOrNull() ?: 120
                    onConfirm(title, bpmVal)
                },
                colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Create", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel", color = StudioTextSecondary)
            }
        },
        containerColor = StudioCardBg,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun RenameProjectDialog(
    currentTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (newTitle: String) -> Unit
) {
    var newTitle by remember { mutableStateOf(currentTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Rename Project",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = StudioTextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Project Title") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioPrimary,
                        unfocusedBorderColor = Color(0xFFD5CBD8)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newTitle.isNotBlank()) {
                        onConfirm(newTitle)
                    }
                },
                enabled = newTitle.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel", color = StudioTextSecondary)
            }
        },
        containerColor = StudioCardBg,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun DeleteProjectDialog(
    project: ProjectEntity,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Project?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = StudioTextPrimary
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"${project.title}\"? This action cannot be undone.",
                fontSize = 14.sp,
                color = StudioTextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Delete", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel", color = StudioTextSecondary)
            }
        },
        containerColor = StudioCardBg,
        shape = RoundedCornerShape(20.dp)
    )
}
