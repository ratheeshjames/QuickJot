package com.example.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.models.NoteWithDetails
import com.example.domain.repository.NotesRepository
import com.example.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TrashUiState(
    val trashedNotes: List<NoteWithDetails> = emptyList(),
    val retentionDays: Int = 14,
    val selectedIds: Set<Long> = emptySet(),
    val showEmptyTrashDialog: Boolean = false,
    val isLoading: Boolean = false
)

class TrashViewModel(
    private val notesRepository: NotesRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    private val showEmptyTrashDialog = MutableStateFlow(false)

    val uiState: StateFlow<TrashUiState> = combine(
        notesRepository.trashedNotesFlow,
        userPreferencesRepository.preferencesFlow,
        selectedIds,
        showEmptyTrashDialog
    ) { trashed, prefs, selected, showDialog ->
        TrashUiState(
            trashedNotes = trashed,
            retentionDays = prefs.trashRetentionDays,
            selectedIds = selected,
            showEmptyTrashDialog = showDialog,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TrashUiState(isLoading = true)
    )

    fun toggleSelection(noteId: Long) {
        val current = selectedIds.value
        selectedIds.value = if (current.contains(noteId)) current - noteId else current + noteId
    }

    fun selectAll(notes: List<NoteWithDetails>) {
        selectedIds.value = notes.map { it.note.id }.toSet()
    }

    fun clearSelection() {
        selectedIds.value = emptySet()
    }

    fun restoreNote(noteId: Long) {
        viewModelScope.launch {
            notesRepository.restoreFromTrash(noteId)
        }
    }

    fun deletePermanently(noteId: Long) {
        viewModelScope.launch {
            notesRepository.deletePermanently(noteId)
        }
    }

    fun bulkRestore() {
        val ids = selectedIds.value.toList()
        viewModelScope.launch {
            notesRepository.bulkRestoreFromTrash(ids)
            clearSelection()
        }
    }

    fun bulkDeletePermanently() {
        val ids = selectedIds.value.toList()
        viewModelScope.launch {
            ids.forEach { notesRepository.deletePermanently(it) }
            clearSelection()
        }
    }

    fun showEmptyTrashConfirm(show: Boolean) {
        showEmptyTrashDialog.value = show
    }

    fun emptyTrash() {
        viewModelScope.launch {
            notesRepository.emptyTrash()
            showEmptyTrashDialog.value = false
            clearSelection()
        }
    }
}
