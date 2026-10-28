package com.example.domain.repository

import com.example.data.local.entities.ChecklistItemEntity
import com.example.data.local.entities.FolderEntity
import com.example.data.local.entities.NoteEntity
import com.example.data.local.entities.NoteVersionEntity
import com.example.data.local.entities.RecentSearchEntity
import com.example.data.local.entities.ReminderEntity
import com.example.data.local.entities.TagEntity
import com.example.data.local.models.NoteWithDetails
import kotlinx.coroutines.flow.Flow

data class QuickJotStats(
    val totalNotes: Int = 0,
    val totalWords: Int = 0,
    val totalCharacters: Int = 0,
    val activeChecklists: Int = 0,
    val completedChecklistItems: Int = 0,
    val totalChecklistItems: Int = 0,
    val totalTags: Int = 0,
    val totalFolders: Int = 0,
    val archivedNotes: Int = 0,
    val trashedNotes: Int = 0
)

/**
 * Domain interface for Notes data operations.
 */
interface NotesRepository {
    // Notes streams
    val activeNotesFlow: Flow<List<NoteWithDetails>>
    val archivedNotesFlow: Flow<List<NoteWithDetails>>
    val trashedNotesFlow: Flow<List<NoteWithDetails>>

    fun getNoteWithDetailsFlow(id: Long): Flow<NoteWithDetails?>
    suspend fun getNoteWithDetails(id: Long): NoteWithDetails?
    suspend fun getNote(id: Long): NoteEntity?

    // Create & Update
    suspend fun saveNote(
        note: NoteEntity,
        checklistItems: List<ChecklistItemEntity> = emptyList(),
        tagIds: List<Long> = emptyList(),
        createVersionSnapshot: Boolean = false
    ): Long

    suspend fun duplicateNote(noteId: Long): Long
    suspend fun updateNote(note: NoteEntity)
    suspend fun updateOrderIndex(id: Long, orderIndex: Int)

    // Delete & Trash
    suspend fun moveToTrash(id: Long)
    suspend fun bulkMoveToTrash(ids: List<Long>)
    suspend fun restoreFromTrash(id: Long)
    suspend fun bulkRestoreFromTrash(ids: List<Long>)
    suspend fun deletePermanently(id: Long)
    suspend fun emptyTrash()
    suspend fun purgeOldTrash(retentionDays: Int): Int

    // Organization & Status
    suspend fun setPinned(id: Long, isPinned: Boolean)
    suspend fun setArchived(id: Long, isArchived: Boolean)
    suspend fun bulkSetArchived(ids: List<Long>, isArchived: Boolean)
    suspend fun setColor(id: Long, colorId: Int)
    suspend fun bulkSetColor(ids: List<Long>, colorId: Int)
    suspend fun setFolder(id: Long, folderId: Long?)
    suspend fun bulkSetFolder(ids: List<Long>, folderId: Long?)
    suspend fun setLocked(id: Long, isLocked: Boolean)

    // Checklists
    fun getChecklistItems(noteId: Long): Flow<List<ChecklistItemEntity>>
    suspend fun saveChecklistItems(noteId: Long, items: List<ChecklistItemEntity>)
    suspend fun toggleChecklistItem(itemId: Long, isDone: Boolean)

    // Version History
    fun getNoteVersions(noteId: Long): Flow<List<NoteVersionEntity>>
    suspend fun restoreVersion(noteId: Long, version: NoteVersionEntity)

    // Folders
    val allFoldersFlow: Flow<List<FolderEntity>>
    fun getSubfolders(parentId: Long): Flow<List<FolderEntity>>
    suspend fun getFolder(id: Long): FolderEntity?
    suspend fun createFolder(name: String, parentId: Long? = null, colorId: Int = 0): Long
    suspend fun updateFolder(folder: FolderEntity)
    suspend fun deleteFolder(id: Long)

    // Tags
    val allTagsFlow: Flow<List<TagEntity>>
    suspend fun searchTags(query: String): List<TagEntity>
    suspend fun createTag(name: String): Long
    suspend fun renameTag(id: Long, newName: String)
    suspend fun deleteTag(id: Long)
    suspend fun mergeTags(sourceTagId: Long, targetTagId: Long)
    suspend fun addTagToNote(noteId: Long, tagId: Long)
    suspend fun removeTagFromNote(noteId: Long, tagId: Long)

    // Search
    fun searchNotes(query: String): Flow<List<NoteWithDetails>>
    fun searchArchive(query: String): Flow<List<NoteWithDetails>>
    fun searchTrash(query: String): Flow<List<NoteWithDetails>>
    val recentSearchesFlow: Flow<List<RecentSearchEntity>>
    suspend fun recordRecentSearch(query: String)
    suspend fun deleteRecentSearch(id: Long)
    suspend fun clearRecentSearches()

    // Reminders
    val activeRemindersFlow: Flow<List<ReminderEntity>>
    fun getReminderForNoteFlow(noteId: Long): Flow<ReminderEntity?>
    suspend fun setReminder(reminder: ReminderEntity): Long
    suspend fun deleteReminder(noteId: Long)
    suspend fun markReminderCompleted(noteId: Long)
    suspend fun snoozeReminder(noteId: Long, snoozeUntilEpoch: Long)

    // Statistics
    val statsFlow: Flow<QuickJotStats>

    // Backup & Export helpers
    suspend fun getAllActiveNotesForBackup(): List<NoteWithDetails>
}
