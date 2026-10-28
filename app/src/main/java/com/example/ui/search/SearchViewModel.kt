package com.example.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.FolderEntity
import com.example.data.local.entities.RecentSearchEntity
import com.example.data.local.entities.TagEntity
import com.example.data.local.models.NoteWithDetails
import com.example.domain.repository.NotesRepository
import com.example.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SearchScope {
    ACTIVE,
    ARCHIVE,
    TRASH
}

data class SearchFilters(
    val scope: SearchScope = SearchScope.ACTIVE,
    val tagId: Long? = null,
    val colorId: Int? = null,
    val folderId: Long? = null,
    val isPinnedOnly: Boolean = false,
    val isChecklistOnly: Boolean = false,
    val isLockedOnly: Boolean = false,
    val hasReminderOnly: Boolean = false,
    val isCaseSensitive: Boolean = false,
    val isWholeWord: Boolean = false
)

data class SearchUiState(
    val query: String = "",
    val results: List<NoteWithDetails> = emptyList(),
    val recentSearches: List<RecentSearchEntity> = emptyList(),
    val allTags: List<TagEntity> = emptyList(),
    val allFolders: List<FolderEntity> = emptyList(),
    val filters: SearchFilters = SearchFilters(),
    val showFilterSheet: Boolean = false,
    val isLoading: Boolean = false
)

class SearchViewModel(
    private val notesRepository: NotesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    initialTag: String?,
    initialFolderId: Long?
) : ViewModel() {

    private val queryFlow = MutableStateFlow("")
    private val filtersFlow = MutableStateFlow(
        SearchFilters(
            folderId = initialFolderId
        )
    )

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadMetadata(initialTag)
        observeSearchResults()
    }

    private fun loadMetadata(initialTag: String?) {
        viewModelScope.launch {
            launch {
                notesRepository.allTagsFlow.collect { tags ->
                    _uiState.value = _uiState.value.copy(allTags = tags)
                    if (initialTag != null) {
                        val matchingTag = tags.firstOrNull { it.name.equals(initialTag, ignoreCase = true) }
                        if (matchingTag != null) {
                            setTagFilter(matchingTag.id)
                        }
                    }
                }
            }
            launch {
                notesRepository.allFoldersFlow.collect { folders ->
                    _uiState.value = _uiState.value.copy(allFolders = folders)
                }
            }
            launch {
                notesRepository.recentSearchesFlow.collect { recents ->
                    _uiState.value = _uiState.value.copy(recentSearches = recents)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeSearchResults() {
        viewModelScope.launch {
            combine(queryFlow, filtersFlow) { query, filters ->
                Pair(query, filters)
            }.flatMapLatest { (query, filters) ->
                val baseFlow = when (filters.scope) {
                    SearchScope.ACTIVE -> notesRepository.searchNotes(query)
                    SearchScope.ARCHIVE -> notesRepository.searchArchive(query)
                    SearchScope.TRASH -> notesRepository.searchTrash(query)
                }
                baseFlow
            }.collect { rawResults ->
                val filters = filtersFlow.value
                val query = queryFlow.value

                val filtered = rawResults.filter { noteWithDetails ->
                    val note = noteWithDetails.note

                    // Tag filter
                    if (filters.tagId != null && noteWithDetails.tags.none { it.id == filters.tagId }) return@filter false

                    // Color filter
                    if (filters.colorId != null && note.colorId != filters.colorId) return@filter false

                    // Folder filter
                    if (filters.folderId != null && note.folderId != filters.folderId) return@filter false

                    // Pinned filter
                    if (filters.isPinnedOnly && !note.isPinned) return@filter false

                    // Checklist filter
                    if (filters.isChecklistOnly && (note.noteType != "checklist" && noteWithDetails.checklistItems.isEmpty())) return@filter false

                    // Locked filter
                    if (filters.isLockedOnly && !note.isLocked) return@filter false

                    // Reminder filter
                    if (filters.hasReminderOnly && (noteWithDetails.reminder == null || noteWithDetails.reminder.isCompleted)) return@filter false

                    // Case-sensitive / whole word match options
                    if (query.isNotBlank()) {
                        val title = note.title
                        val body = note.body
                        if (filters.isCaseSensitive) {
                            if (!title.contains(query) && !body.contains(query)) return@filter false
                        }
                        if (filters.isWholeWord) {
                            val pattern = "\\b${Regex.escape(query)}\\b".toRegex(if (filters.isCaseSensitive) emptySet() else setOf(RegexOption.IGNORE_CASE))
                            if (!pattern.containsMatchIn(title) && !pattern.containsMatchIn(body)) return@filter false
                        }
                    }

                    true
                }

                _uiState.value = _uiState.value.copy(
                    query = query,
                    filters = filters,
                    results = filtered,
                    isLoading = false
                )
            }
        }
    }

    fun setQuery(query: String) {
        queryFlow.value = query
        if (query.isNotBlank()) {
            viewModelScope.launch {
                notesRepository.recordRecentSearch(query)
            }
        }
    }

    fun setScope(scope: SearchScope) {
        filtersFlow.value = filtersFlow.value.copy(scope = scope)
    }

    fun setTagFilter(tagId: Long?) {
        filtersFlow.value = filtersFlow.value.copy(tagId = if (filtersFlow.value.tagId == tagId) null else tagId)
    }

    fun setColorFilter(colorId: Int?) {
        filtersFlow.value = filtersFlow.value.copy(colorId = if (filtersFlow.value.colorId == colorId) null else colorId)
    }

    fun setFolderFilter(folderId: Long?) {
        filtersFlow.value = filtersFlow.value.copy(folderId = if (filtersFlow.value.folderId == folderId) null else folderId)
    }

    fun togglePinnedOnly() {
        filtersFlow.value = filtersFlow.value.copy(isPinnedOnly = !filtersFlow.value.isPinnedOnly)
    }

    fun toggleChecklistOnly() {
        filtersFlow.value = filtersFlow.value.copy(isChecklistOnly = !filtersFlow.value.isChecklistOnly)
    }

    fun toggleLockedOnly() {
        filtersFlow.value = filtersFlow.value.copy(isLockedOnly = !filtersFlow.value.isLockedOnly)
    }

    fun toggleReminderOnly() {
        filtersFlow.value = filtersFlow.value.copy(hasReminderOnly = !filtersFlow.value.hasReminderOnly)
    }

    fun toggleCaseSensitive() {
        filtersFlow.value = filtersFlow.value.copy(isCaseSensitive = !filtersFlow.value.isCaseSensitive)
    }

    fun toggleWholeWord() {
        filtersFlow.value = filtersFlow.value.copy(isWholeWord = !filtersFlow.value.isWholeWord)
    }

    fun clearRecentSearch(id: Long) {
        viewModelScope.launch {
            notesRepository.deleteRecentSearch(id)
        }
    }

    fun clearAllRecentSearches() {
        viewModelScope.launch {
            notesRepository.clearRecentSearches()
        }
    }

    fun resetFilters() {
        filtersFlow.value = SearchFilters(scope = filtersFlow.value.scope)
    }
}
