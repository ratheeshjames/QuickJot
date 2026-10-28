package com.example.ui.trash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.ui.util.HapticFeedbackHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    viewModel: TrashViewModel,
    haptic: HapticFeedbackHelper,
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
                        IconButton(onClick = { viewModel.selectAll(uiState.trashedNotes) }) {
                            Icon(Icons.Default.SelectAll, contentDescription = "Select All")
                        }
                        IconButton(onClick = {
                            haptic.click()
                            viewModel.bulkRestore()
                        }) {
                            Icon(Icons.Default.Restore, contentDescription = "Restore")
                        }
                        IconButton(onClick = {
                            haptic.heavyClick()
                            viewModel.bulkDeletePermanently()
                        }) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "Delete Forever", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text(stringResource(R.string.nav_trash), fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                        }
                    },
                    actions = {
                        if (uiState.trashedNotes.isNotEmpty()) {
                            TextButton(onClick = { viewModel.showEmptyTrashConfirm(true) }) {
                                Text(stringResource(R.string.action_empty_trash), color = MaterialTheme.colorScheme.error)
                            }
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
            if (uiState.trashedNotes.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Delete,
                    title = stringResource(R.string.empty_trash_title),
                    description = stringResource(R.string.empty_trash_desc),
                    modifier = Modifier.weight(1f)
                )
            } else {
                // Retention info banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Notes in Trash are automatically deleted after ${uiState.retentionDays} days.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.trashedNotes, key = { it.note.id }) { noteWithDetails ->
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
                                    viewModel.restoreNote(noteWithDetails.note.id)
                                }) {
                                    Icon(Icons.Default.Restore, contentDescription = "Restore note", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = {
                                    haptic.heavyClick()
                                    viewModel.deletePermanently(noteWithDetails.note.id)
                                }) {
                                    Icon(Icons.Default.DeleteForever, contentDescription = "Delete forever", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Empty Trash Confirmation Dialog
        if (uiState.showEmptyTrashDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.showEmptyTrashConfirm(false) },
                title = { Text(stringResource(R.string.confirm_empty_trash_title)) },
                text = { Text(stringResource(R.string.confirm_empty_trash_msg)) },
                confirmButton = {
                    Button(
                        onClick = {
                            haptic.heavyClick()
                            viewModel.emptyTrash()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.action_empty_trash))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showEmptyTrashConfirm(false) }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}
