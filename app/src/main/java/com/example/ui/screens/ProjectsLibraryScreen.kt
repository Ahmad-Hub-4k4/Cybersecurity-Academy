package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CurriculumCatalog
import com.example.data.LibraryNoteEntity
import com.example.data.ProjectEntity
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberSectionHeader
import com.example.ui.theme.CyberCyanPrimary
import com.example.ui.theme.CyberCyanSecondary
import com.example.ui.theme.CyberNavyBackground
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyElevated
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusPartialAmber
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondarySlate
import com.example.viewmodel.AcademyUiState

@Composable
fun ProjectsLibraryScreen(
    uiState: AcademyUiState,
    onUpdateSearchQuery: (String) -> Unit,
    onSaveProject: (ProjectEntity) -> Unit,
    onDeleteProject: (Long) -> Unit,
    onSaveNote: (title: String, content: String, category: String, day: Int?, week: Int?) -> Unit,
    onDeleteNote: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSubTab by rememberSaveable { mutableIntStateOf(0) } // 0: Projects, 1: Notes Library, 2: Resources Library

    // New Project Form state
    var newProjTitle by rememberSaveable { mutableStateOf("") }
    var newProjObjectives by rememberSaveable { mutableStateOf("") }
    var newProjTools by rememberSaveable { mutableStateOf("Kali Linux VM, Python 3, Git") }
    var newProjGithub by rememberSaveable { mutableStateOf("") }

    // New Note Form state
    var newNoteTitle by rememberSaveable { mutableStateOf("") }
    var newNoteContent by rememberSaveable { mutableStateOf("") }
    var newNoteCategory by rememberSaveable { mutableStateOf("Linux & Security") }
    var newNoteWeek by rememberSaveable { mutableStateOf("1") }

    val query = uiState.librarySearchQuery.trim()
    val filteredProjects = remember(uiState.projects, query) {
        if (query.isEmpty()) uiState.projects
        else uiState.projects.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.objectives.contains(query, ignoreCase = true) ||
                it.requiredTools.contains(query, ignoreCase = true)
        }
    }

    val filteredNotes = remember(uiState.libraryNotes, query) {
        if (query.isEmpty()) uiState.libraryNotes
        else uiState.libraryNotes.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.content.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
        }
    }

    val curatedResources = remember(query) {
        val sampleLessons = (1..26).map { week ->
            CurriculumCatalog.getLessonsForWeek(week).first()
        }
        if (query.isEmpty()) sampleLessons
        else sampleLessons.filter {
            it.primaryResource.title.contains(query, ignoreCase = true) ||
                it.phaseTitle.contains(query, ignoreCase = true)
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CyberCyanPrimary,
        unfocusedBorderColor = CyberNavyBorder,
        focusedLabelColor = CyberCyanPrimary,
        unfocusedLabelColor = TextSecondarySlate,
        focusedTextColor = TextPrimaryWhite,
        unfocusedTextColor = TextPrimaryWhite
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("projects_library_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CyberSectionHeader(
                title = "Projects, Notes & Resource Library",
                subtitle = "GitHub portfolio projects, searchable lab notes, and verified free cybersecurity references",
                badgeText = "${uiState.projects.size} Projects"
            )
        }

        // Search & Sub-Tab Selector
        item {
            CyberCard {
                OutlinedTextField(
                    value = uiState.librarySearchQuery,
                    onValueChange = onUpdateSearchQuery,
                    label = { Text("Search Projects, Notes, or Verified Resources") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = CyberCyanPrimary)
                    },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("library_search_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf(
                        Triple(0, "Portfolio Projects (${filteredProjects.size})", "tab_projects"),
                        Triple(1, "Notes Library (${filteredNotes.size})", "tab_notes_library"),
                        Triple(2, "Resources (${curatedResources.size})", "tab_resource_library")
                    )
                    tabs.forEach { (idx, label, tag) ->
                        val selected = activeSubTab == idx
                        Surface(
                            onClick = { activeSubTab = idx },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) CyberCyanPrimary.copy(alpha = 0.2f) else CyberNavySurface,
                            border = BorderStroke(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) CyberCyanPrimary else CyberNavyBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag(tag)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) CyberCyanPrimary else TextPrimaryWhite,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }

        when (activeSubTab) {
            0 -> {
                // Add Portfolio Project Card
                item {
                    CyberCard(borderColor = CyberCyanPrimary.copy(alpha = 0.4f)) {
                        Text(
                            text = "Add Portfolio Project",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryWhite
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newProjTitle,
                            onValueChange = { newProjTitle = it },
                            label = { Text("Project Title") },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("project_title_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newProjObjectives,
                            onValueChange = { newProjObjectives = it },
                            label = { Text("Objectives & Milestones") },
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("project_objectives_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newProjGithub,
                            onValueChange = { newProjGithub = it },
                            label = { Text("GitHub Repository URL") },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("project_github_input")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (newProjTitle.isNotBlank()) {
                                    onSaveProject(
                                        ProjectEntity(
                                            title = newProjTitle.trim(),
                                            difficulty = "Custom Portfolio Lab",
                                            status = "In Progress",
                                            objectives = newProjObjectives.trim().ifBlank { "Practical cybersecurity implementation" },
                                            requiredTools = newProjTools,
                                            tasksMilestones = "1. Design\n2. Implement in Kali VM\n3. Publish to GitHub",
                                            githubUrl = newProjGithub.trim()
                                        )
                                    )
                                    newProjTitle = ""
                                    newProjObjectives = ""
                                    newProjGithub = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyanPrimary,
                                contentColor = CyberNavyBackground
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_project_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Portfolio Project")
                        }
                    }
                }

                items(filteredProjects, key = { it.id }) { project ->
                    ProjectItemCard(
                        project = project,
                        onToggleStatus = {
                            val nextStatus = when (project.status) {
                                "Planned" -> "In Progress"
                                "In Progress" -> "Completed"
                                else -> "Planned"
                            }
                            onSaveProject(
                                project.copy(
                                    status = nextStatus,
                                    completionDate = if (nextStatus == "Completed") uiState.todayIso else ""
                                )
                            )
                        },
                        onDelete = { onDeleteProject(project.id) }
                    )
                }
            }

            1 -> {
                // Add Library Note Card
                item {
                    CyberCard(borderColor = CyberCyanSecondary.copy(alpha = 0.45f)) {
                        Text(
                            text = "Add Note to Searchable Library",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryWhite
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newNoteTitle,
                            onValueChange = { newNoteTitle = it },
                            label = { Text("Note Title (e.g., Nmap Scan Flags Cheat Sheet)") },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("library_note_title_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newNoteContent,
                            onValueChange = { newNoteContent = it },
                            label = { Text("Note Content / Commands / Findings") },
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("library_note_content_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newNoteCategory,
                                onValueChange = { newNoteCategory = it },
                                label = { Text("Category") },
                                singleLine = true,
                                colors = fieldColors,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = newNoteWeek,
                                onValueChange = { newNoteWeek = it },
                                label = { Text("Week (1-26)") },
                                singleLine = true,
                                colors = fieldColors,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (newNoteTitle.isNotBlank() && newNoteContent.isNotBlank()) {
                                    onSaveNote(
                                        newNoteTitle,
                                        newNoteContent,
                                        newNoteCategory,
                                        null,
                                        newNoteWeek.toIntOrNull() ?: 1
                                    )
                                    newNoteTitle = ""
                                    newNoteContent = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyanPrimary,
                                contentColor = CyberNavyBackground
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_library_note_button")
                        ) {
                            Icon(imageVector = Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Note to Library")
                        }
                    }
                }

                if (filteredNotes.isEmpty()) {
                    item {
                        CyberCard {
                            Text(
                                text = "No notes match your search yet. Add a structured cybersecurity note above.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondarySlate
                            )
                        }
                    }
                } else {
                    items(filteredNotes, key = { it.id }) { note ->
                        LibraryNoteCard(
                            note = note,
                            onDelete = { onDeleteNote(note.id) }
                        )
                    }
                }
            }

            else -> {
                items(curatedResources, key = { it.dayNumber }) { lesson ->
                    val res = lesson.primaryResource
                    val tracked = uiState.resourceProgressMap[res.resourceId]
                    CyberCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LibraryBooks,
                                    contentDescription = null,
                                    tint = CyberCyanPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Week ${lesson.weekNumber} • ${lesson.phaseTitle}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CyberCyanPrimary
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = StatusPresentGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (tracked?.isCompleted == true) "Completed"
                                    else if (tracked?.isViewed == true) "Viewed"
                                    else res.verificationStatus,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusPresentGreen
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = res.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryWhite
                        )
                        Text(
                            text = "Official URL: ${res.url}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyanSecondary
                        )
                        Text(
                            text = "Fallback Resource: ${res.alternativeTitle} (${res.alternativeUrl})",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondarySlate
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectItemCard(
    project: ProjectEntity,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (project.status) {
        "Completed" -> StatusPresentGreen
        "In Progress" -> CyberCyanPrimary
        else -> StatusPartialAmber
    }
    CyberCard(
        borderColor = statusColor.copy(alpha = 0.5f),
        modifier = Modifier.testTag("project_card_${project.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Week ${project.weekAssociation} • ${project.difficulty}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyanSecondary
                )
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimaryWhite
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    onClick = onToggleStatus,
                    shape = RoundedCornerShape(50),
                    color = statusColor.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, statusColor),
                    modifier = Modifier.testTag("project_status_badge_${project.id}")
                ) {
                    Text(
                        text = project.status,
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Project",
                        tint = StatusAbsentRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = project.objectives,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimaryWhite
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Tools: ${project.requiredTools}",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondarySlate
        )
        if (project.githubUrl.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    tint = CyberCyanPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = project.githubUrl,
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyanPrimary
                )
            }
        }
    }
}

@Composable
private fun LibraryNoteCard(
    note: LibraryNoteEntity,
    onDelete: () -> Unit
) {
    CyberCard(modifier = Modifier.testTag("library_note_card_${note.id}")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${note.category} • Week ${note.associatedWeek ?: 1}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyanPrimary
                )
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimaryWhite
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Note",
                    tint = StatusAbsentRed,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CyberNavyElevated.copy(alpha = 0.5f))
                .padding(10.dp)
        ) {
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal),
                color = TextPrimaryWhite
            )
        }
    }
}
