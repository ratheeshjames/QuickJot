package com.example.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.ChecklistItemEntity
import com.example.data.local.entities.FolderEntity
import com.example.data.local.entities.NoteEntity
import com.example.data.local.entities.NoteVersionEntity
import com.example.data.local.entities.ReminderEntity
import com.example.data.local.entities.TagEntity
import com.example.domain.repository.NotesRepository
import com.example.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EditorSnapshot(
    val title: String,
    val body: String,
    val checklistItems: List<ChecklistItemEntity>
)

enum class EditorTextSize(val scaleFactor: Float, val label: String) {
    SMALL(0.85f, "Small"),
    MEDIUM(1.0f, "Medium"),
    LARGE(1.25f, "Large")
}

data class EditorUiState(
    val noteId: Long = 0,
    val title: String = "",
    val body: String = "",
    val colorId: Int = 0,
    val folderId: Long? = null,
    val isPinned: Boolean = false,
    val isLocked: Boolean = false,
    val noteType: String = "text",
    val checklistItems: List<ChecklistItemEntity> = emptyList(),
    val selectedTagIds: Set<Long> = emptySet(),
    val allTags: List<TagEntity> = emptyList(),
    val allFolders: List<FolderEntity> = emptyList(),
    val reminder: ReminderEntity? = null,
    val versions: List<NoteVersionEntity> = emptyList(),
    val characterLimit: Int = 1000,
    val wordCount: Int = 0,
    val charCount: Int = 0,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val modifiedTimestamp: Long = System.currentTimeMillis(),
    val isReadOnly: Boolean = false,
    val textSize: EditorTextSize = EditorTextSize.MEDIUM,
    val showFindReplace: Boolean = false,
    val findQuery: String = "",
    val replaceQuery: String = "",
    val findMatchesCount: Int = 0,
    val showVersionHistory: Boolean = false,
    val isSaving: Boolean = false,
    val saveStatusMessage: String = "",
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
)

class EditorViewModel(
    private val notesRepository: NotesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    val initialNoteId: Long?,
    val initialFolderId: Long?,
    val initialType: String?,
    val initialSharedText: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    // Undo / Redo stacks
    private val undoStack = mutableListOf<EditorSnapshot>()
    private val redoStack = mutableListOf<EditorSnapshot>()
    private var isPerformingUndoRedo = false

    // Auto-save debounce job
    private var autoSaveJob: Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val userPrefs = userPreferencesRepository.preferencesFlow.first()
            val limit = userPrefs.characterLimit

            val tags = notesRepository.allTagsFlow.first()
            val folders = notesRepository.allFoldersFlow.first()

            if (initialNoteId != null && initialNoteId > 0) {
                // Existing Note
                val noteWithDetails = notesRepository.getNoteWithDetails(initialNoteId)
                if (noteWithDetails != null) {
                    val note = noteWithDetails.note
                    val words = countWords("${note.title} ${note.body}")
                    val chars = note.title.length + note.body.length

                    _uiState.value = _uiState.value.copy(
                        noteId = note.id,
                        title = note.title,
                        body = note.body,
                        colorId = note.colorId,
                        folderId = note.folderId,
                        isPinned = note.isPinned,
                        isLocked = note.isLocked,
                        noteType = note.noteType,
                        checklistItems = noteWithDetails.checklistItems,
                        selectedTagIds = noteWithDetails.tags.map { it.id }.toSet(),
                        allTags = tags,
                        allFolders = folders,
                        reminder = noteWithDetails.reminder,
                        characterLimit = limit,
                        wordCount = words,
                        charCount = chars,
                        createdTimestamp = note.createdTimestamp,
                        modifiedTimestamp = note.modifiedTimestamp
                    )

                    // Load version history
                    launch {
                        notesRepository.getNoteVersions(note.id).collect { versions ->
                            _uiState.value = _uiState.value.copy(versions = versions)
                        }
                    }

                    pushUndoSnapshot()
                    return@launch
                }
            }

            // New Note
            val initialBody = initialSharedText ?: ""
            val determinedType = if (initialType == "checklist") "checklist" else "text"
            val initialChecklist = if (determinedType == "checklist") {
                listOf(ChecklistItemEntity(noteId = 0, text = "", isDone = false, orderIndex = 0))
            } else emptyList()

            _uiState.value = _uiState.value.copy(
                noteId = 0,
                title = "",
                body = initialBody,
                folderId = initialFolderId,
                noteType = determinedType,
                checklistItems = initialChecklist,
                allTags = tags,
                allFolders = folders,
                characterLimit = limit,
                wordCount = countWords(initialBody),
                charCount = initialBody.length,
                createdTimestamp = System.currentTimeMillis(),
                modifiedTimestamp = System.currentTimeMillis()
            )
            pushUndoSnapshot()

            if (initialSharedText != null) {
                scheduleAutoSave(forceImmediate = true)
            }
        }
    }

    private fun countWords(text: String): Int {
        return text.trim().split("\\s+".toRegex()).count { it.isNotBlank() }
    }

    private fun pushUndoSnapshot() {
        if (isPerformingUndoRedo) return
        val current = EditorSnapshot(
            title = _uiState.value.title,
            body = _uiState.value.body,
            checklistItems = _uiState.value.checklistItems.map { it.copy() }
        )
        // Avoid duplicate identical snapshots
        if (undoStack.lastOrNull() != current) {
            undoStack.add(current)
            if (undoStack.size > 50) undoStack.removeAt(0)
            redoStack.clear()
            updateUndoRedoAvailability()
        }
    }

    private fun updateUndoRedoAvailability() {
        _uiState.value = _uiState.value.copy(
            canUndo = undoStack.size > 1,
            canRedo = redoStack.isNotEmpty()
        )
    }

    fun undo() {
        if (undoStack.size > 1) {
            isPerformingUndoRedo = true
            val current = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(current)

            val previous = undoStack.last()
            _uiState.value = _uiState.value.copy(
                title = previous.title,
                body = previous.body,
                checklistItems = previous.checklistItems.map { it.copy() },
                wordCount = countWords("${previous.title} ${previous.body}"),
                charCount = previous.title.length + previous.body.length
            )
            isPerformingUndoRedo = false
            updateUndoRedoAvailability()
            scheduleAutoSave()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            isPerformingUndoRedo = true
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(next)

            _uiState.value = _uiState.value.copy(
                title = next.title,
                body = next.body,
                checklistItems = next.checklistItems.map { it.copy() },
                wordCount = countWords("${next.title} ${next.body}"),
                charCount = next.title.length + next.body.length
            )
            isPerformingUndoRedo = false
            updateUndoRedoAvailability()
            scheduleAutoSave()
        }
    }

    fun updateTitle(newTitle: String) {
        if (_uiState.value.isReadOnly) return
        _uiState.value = _uiState.value.copy(
            title = newTitle,
            wordCount = countWords("$newTitle ${_uiState.value.body}"),
            charCount = newTitle.length + _uiState.value.body.length
        )
        pushUndoSnapshot()
        scheduleAutoSave()
    }

    fun updateBody(newBody: String) {
        if (_uiState.value.isReadOnly) return
        // Enforce configurable character limit
        val truncated = if (newBody.length > _uiState.value.characterLimit) {
            newBody.take(_uiState.value.characterLimit)
        } else {
            newBody
        }

        _uiState.value = _uiState.value.copy(
            body = truncated,
            wordCount = countWords("${_uiState.value.title} $truncated"),
            charCount = _uiState.value.title.length + truncated.length
        )
        pushUndoSnapshot()
        scheduleAutoSave()
    }

    // Rich Text Formatting helper
    fun applyFormatting(tag: String) {
        if (_uiState.value.isReadOnly) return
        val currentBody = _uiState.value.body
        val formatted = when (tag) {
            "BOLD" -> "$currentBody **bold**"
            "ITALIC" -> "$currentBody *italic*"
            "UNDERLINE" -> "$currentBody <u>underline</u>"
            "STRIKE" -> "$currentBody ~~strikethrough~~"
            "BULLET" -> if (currentBody.isBlank()) "- " else "$currentBody\n- "
            "NUMBER" -> if (currentBody.isBlank()) "1. " else "$currentBody\n1. "
            "CHECK" -> if (currentBody.isBlank()) "[ ] " else "$currentBody\n[ ] "
            else -> currentBody
        }
        updateBody(formatted)
    }

    // Checklists operations
    fun updateChecklistItem(index: Int, text: String) {
        if (_uiState.value.isReadOnly) return
        val items = _uiState.value.checklistItems.toMutableList()
        if (index in items.indices) {
            items[index] = items[index].copy(text = text)
            _uiState.value = _uiState.value.copy(checklistItems = items)
            pushUndoSnapshot()
            scheduleAutoSave()
        }
    }

    fun toggleChecklistItem(index: Int, isDone: Boolean) {
        val items = _uiState.value.checklistItems.toMutableList()
        if (index in items.indices) {
            items[index] = items[index].copy(isDone = isDone)
            _uiState.value = _uiState.value.copy(checklistItems = items)
            scheduleAutoSave()
        }
    }

    fun addChecklistItem(afterIndex: Int? = null) {
        if (_uiState.value.isReadOnly) return
        val items = _uiState.value.checklistItems.toMutableList()
        val insertAt = if (afterIndex != null && afterIndex in items.indices) afterIndex + 1 else items.size
        items.add(insertAt, ChecklistItemEntity(noteId = _uiState.value.noteId, text = "", isDone = false, orderIndex = insertAt))
        _uiState.value = _uiState.value.copy(checklistItems = items)
        pushUndoSnapshot()
        scheduleAutoSave()
    }

    fun removeChecklistItem(index: Int) {
        if (_uiState.value.isReadOnly) return
        val items = _uiState.value.checklistItems.toMutableList()
        if (index in items.indices) {
            items.removeAt(index)
            _uiState.value = _uiState.value.copy(checklistItems = items)
            pushUndoSnapshot()
            scheduleAutoSave()
        }
    }

    fun reorderChecklistItems(fromIndex: Int, toIndex: Int) {
        if (_uiState.value.isReadOnly) return
        val items = _uiState.value.checklistItems.toMutableList()
        if (fromIndex in items.indices && toIndex in items.indices) {
            val moved = items.removeAt(fromIndex)
            items.add(toIndex, moved)
            _uiState.value = _uiState.value.copy(checklistItems = items)
            scheduleAutoSave()
        }
    }

    fun setColor(colorId: Int) {
        _uiState.value = _uiState.value.copy(colorId = colorId)
        scheduleAutoSave(forceImmediate = true)
    }

    fun setFolder(folderId: Long?) {
        _uiState.value = _uiState.value.copy(folderId = folderId)
        scheduleAutoSave(forceImmediate = true)
    }

    fun togglePin() {
        val next = !_uiState.value.isPinned
        _uiState.value = _uiState.value.copy(isPinned = next)
        scheduleAutoSave(forceImmediate = true)
    }

    fun toggleLock() {
        val next = !_uiState.value.isLocked
        _uiState.value = _uiState.value.copy(isLocked = next)
        scheduleAutoSave(forceImmediate = true)
    }

    fun toggleReadOnly() {
        _uiState.value = _uiState.value.copy(isReadOnly = !_uiState.value.isReadOnly)
    }

    fun setTextSize(size: EditorTextSize) {
        _uiState.value = _uiState.value.copy(textSize = size)
    }

    fun toggleTag(tagId: Long) {
        val current = _uiState.value.selectedTagIds
        val next = if (current.contains(tagId)) current - tagId else current + tagId
        _uiState.value = _uiState.value.copy(selectedTagIds = next)
        scheduleAutoSave(forceImmediate = true)
    }

    fun createAndAddTag(name: String) {
        viewModelScope.launch {
            val newId = notesRepository.createTag(name)
            val updatedTags = notesRepository.allTagsFlow.first()
            _uiState.value = _uiState.value.copy(
                allTags = updatedTags,
                selectedTagIds = _uiState.value.selectedTagIds + newId
            )
            scheduleAutoSave(forceImmediate = true)
        }
    }

    // Find and Replace
    fun toggleFindReplace(show: Boolean? = null) {
        val next = show ?: !_uiState.value.showFindReplace
        _uiState.value = _uiState.value.copy(showFindReplace = next)
        if (!next) {
            _uiState.value = _uiState.value.copy(findQuery = "", replaceQuery = "", findMatchesCount = 0)
        }
    }

    fun updateFindQuery(query: String) {
        val count = if (query.isNotBlank()) {
            val regex = Regex.escape(query).toRegex(RegexOption.IGNORE_CASE)
            regex.findAll(_uiState.value.body).count()
        } else 0
        _uiState.value = _uiState.value.copy(findQuery = query, findMatchesCount = count)
    }

    fun updateReplaceQuery(query: String) {
        _uiState.value = _uiState.value.copy(replaceQuery = query)
    }

    fun replaceOne() {
        val fq = _uiState.value.findQuery
        val rq = _uiState.value.replaceQuery
        if (fq.isNotBlank()) {
            val updated = _uiState.value.body.replaceFirst(fq, rq, ignoreCase = true)
            updateBody(updated)
            updateFindQuery(fq)
        }
    }

    fun replaceAll() {
        val fq = _uiState.value.findQuery
        val rq = _uiState.value.replaceQuery
        if (fq.isNotBlank()) {
            val updated = _uiState.value.body.replace(fq, rq, ignoreCase = true)
            updateBody(updated)
            updateFindQuery(fq)
        }
    }

    // Version History
    fun showVersionHistory(show: Boolean) {
        _uiState.value = _uiState.value.copy(showVersionHistory = show)
    }

    fun restoreVersion(version: NoteVersionEntity) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                title = version.title,
                body = version.body,
                showVersionHistory = false
            )
            pushUndoSnapshot()
            scheduleAutoSave(forceImmediate = true)
        }
    }

    // Reminders
    fun setReminder(epochMillis: Long, repeat: String = "NONE", customDays: Int = 1) {
        viewModelScope.launch {
            val rem = ReminderEntity(
                id = _uiState.value.reminder?.id ?: 0,
                noteId = _uiState.value.noteId,
                triggerTimeEpochMillis = epochMillis,
                repeatInterval = repeat,
                customIntervalDays = customDays,
                isCompleted = false
            )
            _uiState.value = _uiState.value.copy(reminder = rem)
            scheduleAutoSave(forceImmediate = true)
        }
    }

    fun removeReminder() {
        viewModelScope.launch {
            if (_uiState.value.noteId > 0) {
                notesRepository.deleteReminder(_uiState.value.noteId)
            }
            _uiState.value = _uiState.value.copy(reminder = null)
        }
    }

    // Duplicate note
    fun duplicateCurrentNote(onDuplicated: (Long) -> Unit) {
        viewModelScope.launch {
            val noteId = _uiState.value.noteId
            if (noteId > 0) {
                val copyId = notesRepository.duplicateNote(noteId)
                onDuplicated(copyId)
            }
        }
    }

    // Delete note
    fun deleteNote(onDeleted: () -> Unit) {
        viewModelScope.launch {
            val noteId = _uiState.value.noteId
            if (noteId > 0) {
                notesRepository.moveToTrash(noteId)
            }
            onDeleted()
        }
    }

    // In-Place Auto-Save
    private fun scheduleAutoSave(forceImmediate: Boolean = false) {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            if (!forceImmediate) {
                delay(400) // Debounce rapid keystrokes
            }
            performAutoSave()
        }
    }

    private suspend fun performAutoSave() {
        val state = _uiState.value
        val hasContent = state.title.isNotBlank() ||
                state.body.isNotBlank() ||
                state.checklistItems.any { it.text.isNotBlank() }

        if (!hasContent) {
            // Auto-remove empty notes if already in database
            if (state.noteId > 0) {
                notesRepository.deletePermanently(state.noteId)
            }
            return
        }

        _uiState.value = _uiState.value.copy(isSaving = true)

        val noteEntity = NoteEntity(
            id = state.noteId,
            title = state.title.trim(),
            body = state.body,
            colorId = state.colorId,
            folderId = state.folderId,
            isPinned = state.isPinned,
            isLocked = state.isLocked,
            characterLimit = state.characterLimit,
            noteType = state.noteType,
            createdTimestamp = state.createdTimestamp,
            modifiedTimestamp = System.currentTimeMillis()
        )

        val savedNoteId = notesRepository.saveNote(
            note = noteEntity,
            checklistItems = state.checklistItems,
            tagIds = state.selectedTagIds.toList(),
            createVersionSnapshot = true
        )

        // Save reminder if present
        state.reminder?.let { rem ->
            notesRepository.setReminder(rem.copy(noteId = savedNoteId))
        }

        _uiState.value = _uiState.value.copy(
            noteId = savedNoteId,
            isSaving = false,
            saveStatusMessage = "Saved",
            modifiedTimestamp = noteEntity.modifiedTimestamp
        )
    }

    fun onExit(onComplete: () -> Unit) {
        viewModelScope.launch {
            autoSaveJob?.cancel()
            performAutoSave()
            onComplete()
        }
    }
}
