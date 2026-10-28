package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.models.NoteWithDetails
import com.example.domain.repository.NotesRepository
import com.example.domain.repository.SortOption
import com.example.domain.repository.UserPreferences
import com.example.domain.repository.UserPreferencesRepository
import com.example.domain.repository.ViewMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HomeFilter {
    ALL,
    PINNED,
    CHECKLISTS,
    REMINDERS,
    LOCKED
}

data class HomeUiState(
    val pinnedNotes: List<NoteWithDetails> = emptyList(),
    val otherNotes: List<NoteWithDetails> = emptyList(),
    val currentFilter: HomeFilter = HomeFilter.ALL,
    val viewMode: ViewMode = ViewMode.LIST,
    val sortOption: SortOption = SortOption.MODIFIED,
    val isSortAscending: Boolean = false,
    val selectedNoteIds: Set<Long> = emptySet(),
    val isFabExpanded: Boolean = false,
    val showBulkColorDialog: Boolean = false,
    val recentlyDeletedNoteId: Long? = null,
    val isLoading: Boolean = false
)

private data class HomeControls(
    val selectedIds: Set<Long> = emptySet(),
    val isFabExpanded: Boolean = false,
    val showBulkColor: Boolean = false,
    val deletedId: Long? = null
)

class HomeViewModel(
    private val notesRepository: NotesRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val currentFilter = MutableStateFlow(HomeFilter.ALL)
    private val selectedNoteIds = MutableStateFlow<Set<Long>>(emptySet())
    private val isFabExpanded = MutableStateFlow(false)
    private val showBulkColorDialog = MutableStateFlow(false)
    private val recentlyDeletedNoteId = MutableStateFlow<Long?>(null)

    val userPreferences: StateFlow<UserPreferences> = userPreferencesRepository.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    private val controlsFlow = combine(
        selectedNoteIds,
        isFabExpanded,
        showBulkColorDialog,
        recentlyDeletedNoteId
    ) { selectedIds, fabExpanded, bulkColor, deletedId ->
        HomeControls(
            selectedIds = selectedIds,
            isFabExpanded = fabExpanded,
            showBulkColor = bulkColor,
            deletedId = deletedId
        )
    }

    private val filteredSortedNotes = combine(
        notesRepository.activeNotesFlow,
        userPreferencesRepository.preferencesFlow,
        currentFilter
    ) { activeNotes, prefs, filter ->
        val filtered = activeNotes.filter { noteWithDetails ->
            val note = noteWithDetails.note
            when (filter) {
                HomeFilter.ALL -> true
                HomeFilter.PINNED -> note.isPinned
                HomeFilter.CHECKLISTS -> note.noteType == "checklist" || noteWithDetails.checklistItems.isNotEmpty()
                HomeFilter.REMINDERS -> noteWithDetails.reminder != null && !noteWithDetails.reminder.isCompleted
                HomeFilter.LOCKED -> note.isLocked
            }
        }

        val sorted = when (prefs.sortOption) {
            SortOption.MODIFIED -> if (prefs.sortAscending) filtered.sortedBy { it.note.modifiedTimestamp } else filtered.sortedByDescending { it.note.modifiedTimestamp }
            SortOption.CREATED -> if (prefs.sortAscending) filtered.sortedBy { it.note.createdTimestamp } else filtered.sortedByDescending { it.note.createdTimestamp }
            SortOption.TITLE -> if (prefs.sortAscending) filtered.sortedBy { it.note.title.lowercase() } else filtered.sortedByDescending { it.note.title.lowercase() }
            SortOption.COLOR -> if (prefs.sortAscending) filtered.sortedBy { it.note.colorId } else filtered.sortedByDescending { it.note.colorId }
        }

        val pinned = if (filter == HomeFilter.PINNED) sorted else sorted.filter { it.note.isPinned }
        val others = if (filter == HomeFilter.PINNED) emptyList() else sorted.filter { !it.note.isPinned }

        Triple(pinned, others, filter)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        filteredSortedNotes,
        userPreferencesRepository.preferencesFlow,
        controlsFlow
    ) { (pinned, others, filter), prefs, controls ->
        HomeUiState(
            pinnedNotes = pinned,
            otherNotes = others,
            currentFilter = filter,
            viewMode = prefs.viewMode,
            sortOption = prefs.sortOption,
            isSortAscending = prefs.sortAscending,
            selectedNoteIds = controls.selectedIds,
            isFabExpanded = controls.isFabExpanded,
            showBulkColorDialog = controls.showBulkColor,
            recentlyDeletedNoteId = controls.deletedId,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun setFilter(filter: HomeFilter) {
        currentFilter.value = filter
    }

    fun toggleFabMenu() {
        isFabExpanded.value = !isFabExpanded.value
    }

    fun closeFabMenu() {
        isFabExpanded.value = false
    }

    fun toggleViewMode() {
        viewModelScope.launch {
            val current = userPreferences.value.viewMode
            val next = when (current) {
                ViewMode.LIST -> ViewMode.GRID
                ViewMode.GRID -> ViewMode.COMPACT
                ViewMode.COMPACT -> ViewMode.LIST
            }
            userPreferencesRepository.setViewMode(next)
        }
    }

    fun setSort(option: SortOption, ascending: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setSortOption(option, ascending)
        }
    }

    // Selection Mode
    fun toggleSelection(noteId: Long) {
        val current = selectedNoteIds.value
        selectedNoteIds.value = if (current.contains(noteId)) {
            current - noteId
        } else {
            current + noteId
        }
    }

    fun selectAll(allNotes: List<NoteWithDetails>) {
        selectedNoteIds.value = allNotes.map { it.note.id }.toSet()
    }

    fun clearSelection() {
        selectedNoteIds.value = emptySet()
    }

    // Single note actions
    fun moveToTrash(noteId: Long) {
        viewModelScope.launch {
            notesRepository.moveToTrash(noteId)
            recentlyDeletedNoteId.value = noteId
        }
    }

    fun undoDelete() {
        val noteId = recentlyDeletedNoteId.value ?: return
        viewModelScope.launch {
            notesRepository.restoreFromTrash(noteId)
            recentlyDeletedNoteId.value = null
        }
    }

    // Bulk actions
    fun bulkDelete() {
        val ids = selectedNoteIds.value.toList()
        viewModelScope.launch {
            notesRepository.bulkMoveToTrash(ids)
            clearSelection()
        }
    }

    fun bulkArchive() {
        val ids = selectedNoteIds.value.toList()
        viewModelScope.launch {
            notesRepository.bulkSetArchived(ids, true)
            clearSelection()
        }
    }

    fun bulkPin() {
        val ids = selectedNoteIds.value.toList()
        viewModelScope.launch {
            ids.forEach { notesRepository.setPinned(it, true) }
            clearSelection()
        }
    }

    fun showBulkColorPicker(show: Boolean) {
        showBulkColorDialog.value = show
    }

    fun applyBulkColor(colorId: Int) {
        val ids = selectedNoteIds.value.toList()
        viewModelScope.launch {
            notesRepository.bulkSetColor(ids, colorId)
            showBulkColorDialog.value = false
            clearSelection()
        }
    }
}
