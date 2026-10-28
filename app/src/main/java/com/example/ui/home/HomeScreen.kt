package com.example.ui.home

import android.content.ClipDescription
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.models.NoteWithDetails
import com.example.domain.repository.ViewMode
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ExpressiveColorPicker
import com.example.ui.home.components.ExpressiveFabMenu
import com.example.ui.home.components.HomeNavigationDrawer
import com.example.ui.home.components.HomeTopBar
import com.example.ui.home.components.NoteCard
import com.example.ui.navigation.QuickJotRoute
import com.example.ui.util.HapticFeedbackHelper
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    haptic: HapticFeedbackHelper,
    onNavigate: (QuickJotRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    val allNotes = remember(uiState.pinnedNotes, uiState.otherNotes) {
        uiState.pinnedNotes + uiState.otherNotes
    }

    val isInSelectionMode = uiState.selectedNoteIds.isNotEmpty()

    // Handle recently deleted note snackbar with undo
    LaunchedEffect(uiState.recentlyDeletedNoteId) {
        val deletedId = uiState.recentlyDeletedNoteId
        if (deletedId != null) {
            val result = snackbarHostState.showSnackbar(
                message = "Moved note to Trash",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                haptic.click()
                viewModel.undoDelete()
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeNavigationDrawer(
                currentRoute = QuickJotRoute.Home,
                onNavigate = { destination ->
                    scope.launch { drawerState.close() }
                    onNavigate(destination)
                }
            )
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                HomeTopBar(
                    selectedCount = uiState.selectedNoteIds.size,
                    isInSelectionMode = isInSelectionMode,
                    currentFilter = uiState.currentFilter,
                    currentViewMode = uiState.viewMode,
                    currentSortOption = uiState.sortOption,
                    isSortAscending = uiState.isSortAscending,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onSearchClick = { onNavigate(QuickJotRoute.Search()) },
                    onToggleViewMode = { viewModel.toggleViewMode() },
                    onSortSelected = { option, ascending -> viewModel.setSort(option, ascending) },
                    onFilterSelected = { viewModel.setFilter(it) },
                    onSelectAll = { viewModel.selectAll(allNotes) },
                    onClearSelection = { viewModel.clearSelection() },
                    onBulkDelete = {
                        haptic.heavyClick()
                        viewModel.bulkDelete()
                    },
                    onBulkArchive = {
                        haptic.click()
                        viewModel.bulkArchive()
                    },
                    onBulkPin = {
                        haptic.click()
                        viewModel.bulkPin()
                    },
                    onBulkColorClick = { viewModel.showBulkColorPicker(true) },
                    haptic = haptic
                )
            },
            floatingActionButton = {
                if (!isInSelectionMode) {
                    ExpressiveFabMenu(
                        isExpanded = uiState.isFabExpanded,
                        onToggleExpand = { viewModel.toggleFabMenu() },
                        onNewNote = {
                            viewModel.closeFabMenu()
                            onNavigate(QuickJotRoute.Editor(initialType = "text"))
                        },
                        onNewChecklist = {
                            viewModel.closeFabMenu()
                            onNavigate(QuickJotRoute.Editor(initialType = "checklist"))
                        },
                        onVoiceNote = {
                            viewModel.closeFabMenu()
                            onNavigate(QuickJotRoute.Editor(initialType = "voice"))
                        },
                        onClipboardNote = {
                            viewModel.closeFabMenu()
                            val clipboardText = clipboardManager.getText()?.text
                            onNavigate(QuickJotRoute.Editor(sharedText = clipboardText))
                        },
                        haptic = haptic
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (allNotes.isEmpty()) {
                    // Guided Empty State
                    EmptyStateView(
                        icon = Icons.Default.EditNote,
                        title = if (uiState.currentFilter == HomeFilter.ALL) stringResource(R.string.empty_home_title) else "No notes found",
                        description = if (uiState.currentFilter == HomeFilter.ALL) stringResource(R.string.empty_home_desc) else "No notes match the current filter.",
                        actionLabel = if (uiState.currentFilter == HomeFilter.ALL) stringResource(R.string.action_new_note) else null,
                        onActionClick = {
                            onNavigate(QuickJotRoute.Editor(initialType = "text"))
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    when (uiState.viewMode) {
                        ViewMode.GRID -> {
                            NotesGridView(
                                pinnedNotes = uiState.pinnedNotes,
                                otherNotes = uiState.otherNotes,
                                selectedIds = uiState.selectedNoteIds,
                                isInSelectionMode = isInSelectionMode,
                                onNoteClick = { note ->
                                    if (isInSelectionMode) {
                                        haptic.tick()
                                        viewModel.toggleSelection(note.id)
                                    } else {
                                        if (note.isLocked) {
                                            onNavigate(QuickJotRoute.LockScreen(reason = "note_lock:${note.id}"))
                                        } else {
                                            onNavigate(QuickJotRoute.Editor(noteId = note.id))
                                        }
                                    }
                                },
                                onNoteLongClick = { note ->
                                    haptic.click()
                                    viewModel.toggleSelection(note.id)
                                }
                            )
                        }
                        ViewMode.LIST, ViewMode.COMPACT -> {
                            NotesListView(
                                viewMode = uiState.viewMode,
                                pinnedNotes = uiState.pinnedNotes,
                                otherNotes = uiState.otherNotes,
                                selectedIds = uiState.selectedNoteIds,
                                isInSelectionMode = isInSelectionMode,
                                onNoteClick = { note ->
                                    if (isInSelectionMode) {
                                        haptic.tick()
                                        viewModel.toggleSelection(note.id)
                                    } else {
                                        if (note.isLocked) {
                                            onNavigate(QuickJotRoute.LockScreen(reason = "note_lock:${note.id}"))
                                        } else {
                                            onNavigate(QuickJotRoute.Editor(noteId = note.id))
                                        }
                                    }
                                },
                                onNoteLongClick = { note ->
                                    haptic.click()
                                    viewModel.toggleSelection(note.id)
                                },
                                onSwipeDelete = { noteId ->
                                    haptic.heavyClick()
                                    viewModel.moveToTrash(noteId)
                                }
                            )
                        }
                    }
                }

                // Bulk Color Picker Dialog
                if (uiState.showBulkColorDialog) {
                    AlertDialog(
                        onDismissRequest = { viewModel.showBulkColorPicker(false) },
                        title = { Text("Choose Color for Selected Notes") },
                        text = {
                            ExpressiveColorPicker(
                                selectedColorId = 0,
                                onColorSelected = { colorId ->
                                    haptic.click()
                                    viewModel.applyBulkColor(colorId)
                                }
                            )
                        },
                        confirmButton = {},
                        dismissButton = {
                            TextButton(onClick = { viewModel.showBulkColorPicker(false) }) {
                                Text(stringResource(R.string.cancel))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotesListView(
    viewMode: ViewMode,
    pinnedNotes: List<NoteWithDetails>,
    otherNotes: List<NoteWithDetails>,
    selectedIds: Set<Long>,
    isInSelectionMode: Boolean,
    onNoteClick: (com.example.data.local.entities.NoteEntity) -> Unit,
    onNoteLongClick: (com.example.data.local.entities.NoteEntity) -> Unit,
    onSwipeDelete: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (pinnedNotes.isNotEmpty()) {
            item {
                SectionHeader(stringResource(R.string.section_pinned))
            }
            items(pinnedNotes, key = { it.note.id }) { noteWithDetails ->
                SwipeableNoteCard(
                    noteWithDetails = noteWithDetails,
                    viewMode = viewMode,
                    isSelected = selectedIds.contains(noteWithDetails.note.id),
                    isInSelectionMode = isInSelectionMode,
                    onClick = { onNoteClick(noteWithDetails.note) },
                    onLongClick = { onNoteLongClick(noteWithDetails.note) },
                    onDelete = { onSwipeDelete(noteWithDetails.note.id) }
                )
            }
        }

        if (otherNotes.isNotEmpty()) {
            if (pinnedNotes.isNotEmpty()) {
                item {
                    SectionHeader(stringResource(R.string.section_others))
                }
            }
            items(otherNotes, key = { it.note.id }) { noteWithDetails ->
                SwipeableNoteCard(
                    noteWithDetails = noteWithDetails,
                    viewMode = viewMode,
                    isSelected = selectedIds.contains(noteWithDetails.note.id),
                    isInSelectionMode = isInSelectionMode,
                    onClick = { onNoteClick(noteWithDetails.note) },
                    onLongClick = { onNoteLongClick(noteWithDetails.note) },
                    onDelete = { onSwipeDelete(noteWithDetails.note.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun NotesGridView(
    pinnedNotes: List<NoteWithDetails>,
    otherNotes: List<NoteWithDetails>,
    selectedIds: Set<Long>,
    isInSelectionMode: Boolean,
    onNoteClick: (com.example.data.local.entities.NoteEntity) -> Unit,
    onNoteLongClick: (com.example.data.local.entities.NoteEntity) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (pinnedNotes.isNotEmpty()) {
            item(span = { GridItemSpan(2) }) {
                SectionHeader(stringResource(R.string.section_pinned))
            }
            items(pinnedNotes, key = { it.note.id }) { noteWithDetails ->
                NoteCard(
                    noteWithDetails = noteWithDetails,
                    viewMode = ViewMode.GRID,
                    isSelected = selectedIds.contains(noteWithDetails.note.id),
                    isInSelectionMode = isInSelectionMode,
                    onClick = { onNoteClick(noteWithDetails.note) },
                    onLongClick = { onNoteLongClick(noteWithDetails.note) }
                )
            }
        }

        if (otherNotes.isNotEmpty()) {
            if (pinnedNotes.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    SectionHeader(stringResource(R.string.section_others))
                }
            }
            items(otherNotes, key = { it.note.id }) { noteWithDetails ->
                NoteCard(
                    noteWithDetails = noteWithDetails,
                    viewMode = ViewMode.GRID,
                    isSelected = selectedIds.contains(noteWithDetails.note.id),
                    isInSelectionMode = isInSelectionMode,
                    onClick = { onNoteClick(noteWithDetails.note) },
                    onLongClick = { onNoteLongClick(noteWithDetails.note) }
                )
            }
        }

        item(span = { GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun SwipeableNoteCard(
    noteWithDetails: NoteWithDetails,
    viewMode: ViewMode,
    isSelected: Boolean,
    isInSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit
) {
    if (isInSelectionMode) {
        NoteCard(
            noteWithDetails = noteWithDetails,
            viewMode = viewMode,
            isSelected = isSelected,
            isInSelectionMode = true,
            onClick = onClick,
            onLongClick = onLongClick
        )
    } else {
        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                if (value == SwipeToDismissBoxValue.EndToStart || value == SwipeToDismissBoxValue.StartToEnd) {
                    onDelete()
                    true
                } else false
            }
        )

        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                val alignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                    Alignment.CenterStart
                } else {
                    Alignment.CenterEnd
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 24.dp),
                    contentAlignment = alignment
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        ) {
            NoteCard(
                noteWithDetails = noteWithDetails,
                viewMode = viewMode,
                isSelected = isSelected,
                isInSelectionMode = false,
                onClick = onClick,
                onLongClick = onLongClick
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
    )
}
