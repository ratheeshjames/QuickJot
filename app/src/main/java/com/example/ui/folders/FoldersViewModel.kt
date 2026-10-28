package com.example.ui.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.FolderEntity
import com.example.domain.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FolderWithNoteCount(
    val folder: FolderEntity,
    val noteCount: Int,
    val subfolders: List<FolderWithNoteCount> = emptyList()
)

data class FoldersUiState(
    val folders: List<FolderWithNoteCount> = emptyList(),
    val rawFolders: List<FolderEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

class FoldersViewModel(
    private val notesRepository: NotesRepository
) : ViewModel() {

    private val searchQueryFlow = MutableStateFlow("")

    val uiState: StateFlow<FoldersUiState> = combine(
        notesRepository.allFoldersFlow,
        notesRepository.activeNotesFlow,
        searchQueryFlow
    ) { folders, notes, query ->
        val counts = notes.groupBy { it.note.folderId }
            .mapValues { it.value.size }

        // Build hierarchy: root folders (parentId == null) and subfolders
        val folderMap = folders.associateBy { it.id }
        val childrenMap = folders.filter { it.parentId != null }
            .groupBy { it.parentId!! }

        fun buildTree(folder: FolderEntity): FolderWithNoteCount {
            val children = childrenMap[folder.id]?.map { buildTree(it) } ?: emptyList()
            val totalNotes = (counts[folder.id] ?: 0) + children.sumOf { it.noteCount }
            return FolderWithNoteCount(folder, totalNotes, children)
        }

        val rootFolders = folders.filter { it.parentId == null || !folderMap.containsKey(it.parentId) }
            .map { buildTree(it) }

        val filtered = if (query.isBlank()) {
            rootFolders
        } else {
            folders.filter { it.name.contains(query, ignoreCase = true) }
                .map { FolderWithNoteCount(it, counts[it.id] ?: 0) }
        }

        FoldersUiState(
            folders = filtered,
            rawFolders = folders,
            searchQuery = query,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FoldersUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        searchQueryFlow.value = query
    }

    fun createFolder(name: String, parentId: Long?, colorId: Int) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                notesRepository.createFolder(name.trim(), parentId, colorId)
            }
        }
    }

    fun updateFolder(folder: FolderEntity) {
        viewModelScope.launch {
            notesRepository.updateFolder(folder)
        }
    }

    fun deleteFolder(id: Long) {
        viewModelScope.launch {
            notesRepository.deleteFolder(id)
        }
    }
}
