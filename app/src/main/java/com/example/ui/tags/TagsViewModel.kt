package com.example.ui.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.TagEntity
import com.example.domain.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TagWithNoteCount(
    val tag: TagEntity,
    val noteCount: Int
)

data class TagsUiState(
    val tags: List<TagWithNoteCount> = emptyList(),
    val rawTags: List<TagEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

class TagsViewModel(
    private val notesRepository: NotesRepository
) : ViewModel() {

    private val searchQueryFlow = MutableStateFlow("")

    val uiState: StateFlow<TagsUiState> = combine(
        notesRepository.allTagsFlow,
        notesRepository.activeNotesFlow,
        searchQueryFlow
    ) { tags, notes, query ->
        // Count how many notes have each tag
        val tagCounts = mutableMapOf<Long, Int>()
        notes.forEach { noteWithDetails ->
            noteWithDetails.tags.forEach { tag ->
                tagCounts[tag.id] = (tagCounts[tag.id] ?: 0) + 1
            }
        }

        val items = tags.map { tag ->
            TagWithNoteCount(
                tag = tag,
                noteCount = tagCounts[tag.id] ?: 0
            )
        }

        val filtered = if (query.isBlank()) {
            items
        } else {
            items.filter { it.tag.name.contains(query, ignoreCase = true) }
        }

        TagsUiState(
            tags = filtered,
            rawTags = tags,
            searchQuery = query,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TagsUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        searchQueryFlow.value = query
    }

    fun createTag(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                notesRepository.createTag(name)
            }
        }
    }

    fun renameTag(id: Long, newName: String) {
        viewModelScope.launch {
            if (newName.isNotBlank()) {
                notesRepository.renameTag(id, newName)
            }
        }
    }

    fun deleteTag(id: Long) {
        viewModelScope.launch {
            notesRepository.deleteTag(id)
        }
    }

    fun mergeTags(sourceTagId: Long, targetTagId: Long) {
        viewModelScope.launch {
            if (sourceTagId != targetTagId) {
                notesRepository.mergeTags(sourceTagId, targetTagId)
            }
        }
    }
}
