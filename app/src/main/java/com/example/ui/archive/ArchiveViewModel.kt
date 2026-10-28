package com.example.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.models.NoteWithDetails
import com.example.domain.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ArchiveUiState(
    val archivedNotes: List<NoteWithDetails> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
    val isLoading: Boolean = false
)

class ArchiveViewModel(
    private val notesRepository: NotesRepository
) : ViewModel() {

    private val selectedIds = MutableStateFlow<Set<Long>>(emptySet())

    val uiState: StateFlow<ArchiveUiState> = combine(
        notesRepository.archivedNotesFlow,
        selectedIds
    ) { archived, selected ->
        ArchiveUiState(
            archivedNotes = archived,
            selectedIds = selected,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ArchiveUiState(isLoading = true)
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

    fun unarchiveNote(noteId: Long) {
        viewModelScope.launch {
            notesRepository.setArchived(noteId, false)
        }
    }

    fun moveToTrash(noteId: Long) {
        viewModelScope.launch {
            notesRepository.moveToTrash(noteId)
        }
    }

    fun bulkUnarchive() {
        val ids = selectedIds.value.toList()
        viewModelScope.launch {
            notesRepository.bulkSetArchived(ids, false)
            clearSelection()
        }
    }

    fun bulkMoveToTrash() {
        val ids = selectedIds.value.toList()
        viewModelScope.launch {
            notesRepository.bulkMoveToTrash(ids)
            clearSelection()
        }
    }
}
