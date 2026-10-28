package com.example.ui.archive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.repository.ViewMode
import com.example.ui.components.EmptyStateView
import com.example.ui.home.components.NoteCard
import com.example.ui.navigation.QuickJotRoute
import com.example.ui.util.HapticFeedbackHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    viewModel: ArchiveViewModel,
    haptic: HapticFeedbackHelper,
    onNavigate: (QuickJotRoute) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isInSelectionMode = uiState.selectedIds.isNotEmpty()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (isInSelectionMode) {
                TopAppBar(
                    title = { Text("${uiState.selectedIds.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.selectAll(uiState.archivedNotes) }) {
                            Icon(Icons.Default.SelectAll, contentDescription = "Select All")
                        }
                        IconButton(onClick = {
                            haptic.click()
                            viewModel.bulkUnarchive()
                        }) {
                            Icon(Icons.Default.Unarchive, contentDescription = "Unarchive")
                        }
                        IconButton(onClick = {
                            haptic.heavyClick()
                            viewModel.bulkMoveToTrash()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text(stringResource(R.string.nav_archive), fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.archivedNotes.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Archive,
                    title = stringResource(R.string.empty_archive_title),
                    description = stringResource(R.string.empty_archive_desc),
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.archivedNotes, key = { it.note.id }) { noteWithDetails ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                NoteCard(
                                    noteWithDetails = noteWithDetails,
                                    viewMode = ViewMode.LIST,
                                    isSelected = uiState.selectedIds.contains(noteWithDetails.note.id),
                                    isInSelectionMode = isInSelectionMode,
                                    onClick = {
                                        if (isInSelectionMode) {
                                            viewModel.toggleSelection(noteWithDetails.note.id)
                                        } else {
                                            onNavigate(QuickJotRoute.Editor(noteId = noteWithDetails.note.id))
                                        }
                                    },
                                    onLongClick = {
                                        viewModel.toggleSelection(noteWithDetails.note.id)
                                    }
                                )
                            }

                            if (!isInSelectionMode) {
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(onClick = {
                                    haptic.click()
                                    viewModel.unarchiveNote(noteWithDetails.note.id)
                                }) {
                                    Icon(Icons.Default.Unarchive, contentDescription = "Unarchive", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = {
                                    haptic.heavyClick()
                                    viewModel.moveToTrash(noteWithDetails.note.id)
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Move to Trash", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
