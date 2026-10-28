package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.QuickJotDatabase
import com.example.data.local.entities.ChecklistItemEntity
import com.example.data.local.entities.FolderEntity
import com.example.data.local.entities.NoteEntity
import com.example.data.local.entities.NoteTagCrossRef
import com.example.data.local.entities.NoteVersionEntity
import com.example.data.local.entities.RecentSearchEntity
import com.example.data.local.entities.ReminderEntity
import com.example.data.local.entities.TagEntity
import com.example.data.local.models.NoteWithDetails
import com.example.domain.repository.NotesRepository
import com.example.domain.repository.QuickJotStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class OfflineNotesRepository(
    private val database: QuickJotDatabase
) : NotesRepository {

    private val noteDao = database.noteDao()
    private val checklistDao = database.checklistDao()
    private val noteVersionDao = database.noteVersionDao()
    private val folderDao = database.folderDao()
    private val tagDao = database.tagDao()
    private val reminderDao = database.reminderDao()
    private val recentSearchDao = database.recentSearchDao()

    override val activeNotesFlow: Flow<List<NoteWithDetails>> = noteDao.getActiveNotesWithDetails()
    override val archivedNotesFlow: Flow<List<NoteWithDetails>> = noteDao.getArchivedNotesWithDetails()
    override val trashedNotesFlow: Flow<List<NoteWithDetails>> = noteDao.getTrashedNotesWithDetails()

    override fun getNoteWithDetailsFlow(id: Long): Flow<NoteWithDetails?> =
        noteDao.getNoteWithDetailsFlowById(id)

    override suspend fun getNoteWithDetails(id: Long): NoteWithDetails? =
        noteDao.getNoteWithDetailsById(id)

    override suspend fun getNote(id: Long): NoteEntity? =
        noteDao.getNoteById(id)

    override suspend fun saveNote(
        note: NoteEntity,
        checklistItems: List<ChecklistItemEntity>,
        tagIds: List<Long>,
        createVersionSnapshot: Boolean
    ): Long {
        return database.withTransaction {
            val noteId = if (note.id == 0L) {
                noteDao.insertNote(note)
            } else {
                noteDao.updateNote(note)
                note.id
            }

            // Version snapshot
            if (createVersionSnapshot && (note.title.isNotBlank() || note.body.isNotBlank())) {
                noteVersionDao.insertVersion(
                    NoteVersionEntity(
                        noteId = noteId,
                        title = note.title,
                        body = note.body,
                        timestamp = System.currentTimeMillis()
                    )
                )
                noteVersionDao.trimVersionsForNote(noteId, keepCount = 30)
            }

            // Checklist items
            if (checklistItems.isNotEmpty()) {
                checklistDao.deleteItemsForNote(noteId)
                val itemsWithNoteId = checklistItems.mapIndexed { index, item ->
                    item.copy(id = 0, noteId = noteId, orderIndex = index)
                }
                checklistDao.insertItems(itemsWithNoteId)
            }

            // Tags
            if (tagIds.isNotEmpty()) {
                tagDao.clearTagsForNote(noteId)
                tagIds.forEach { tagId ->
                    tagDao.insertNoteTagCrossRef(NoteTagCrossRef(noteId = noteId, tagId = tagId))
                }
            }

            noteId
        }
    }

    override suspend fun duplicateNote(noteId: Long): Long {
        return database.withTransaction {
            val original = noteDao.getNoteWithDetailsById(noteId) ?: return@withTransaction 0L
            val copyNote = original.note.copy(
                id = 0,
                title = if (original.note.title.isNotBlank()) "${original.note.title} (Copy)" else "",
                createdTimestamp = System.currentTimeMillis(),
                modifiedTimestamp = System.currentTimeMillis(),
                isPinned = false
            )
            val newNoteId = noteDao.insertNote(copyNote)

            // Copy checklist items
            if (original.checklistItems.isNotEmpty()) {
                val copiedItems = original.checklistItems.map { it.copy(id = 0, noteId = newNoteId) }
                checklistDao.insertItems(copiedItems)
            }

            // Copy tags
            original.tags.forEach { tag ->
                tagDao.insertNoteTagCrossRef(NoteTagCrossRef(noteId = newNoteId, tagId = tag.id))
            }

            newNoteId
        }
    }

    override suspend fun updateNote(note: NoteEntity) {
        noteDao.updateNote(note)
    }

    override suspend fun updateOrderIndex(id: Long, orderIndex: Int) {
        noteDao.updateOrderIndex(id, orderIndex)
    }

    override suspend fun moveToTrash(id: Long) {
        noteDao.moveToTrash(id)
    }

    override suspend fun bulkMoveToTrash(ids: List<Long>) {
        noteDao.bulkMoveToTrash(ids)
    }

    override suspend fun restoreFromTrash(id: Long) {
        noteDao.restoreFromTrash(id)
    }

    override suspend fun bulkRestoreFromTrash(ids: List<Long>) {
        noteDao.bulkRestoreFromTrash(ids)
    }

    override suspend fun deletePermanently(id: Long) {
        noteDao.deleteNoteById(id)
    }

    override suspend fun emptyTrash() {
        noteDao.emptyTrash()
    }

    override suspend fun purgeOldTrash(retentionDays: Int): Int {
        val retentionMillis = retentionDays.toLong() * 24L * 60L * 60L * 1000L
        val threshold = System.currentTimeMillis() - retentionMillis
        return noteDao.purgeOldTrashedNotes(threshold)
    }

    override suspend fun setPinned(id: Long, isPinned: Boolean) {
        noteDao.setPinned(id, isPinned)
    }

    override suspend fun setArchived(id: Long, isArchived: Boolean) {
        noteDao.setArchived(id, isArchived)
    }

    override suspend fun bulkSetArchived(ids: List<Long>, isArchived: Boolean) {
        noteDao.bulkSetArchived(ids, isArchived)
    }

    override suspend fun setColor(id: Long, colorId: Int) {
        noteDao.setColor(id, colorId)
    }

    override suspend fun bulkSetColor(ids: List<Long>, colorId: Int) {
        noteDao.bulkSetColor(ids, colorId)
    }

    override suspend fun setFolder(id: Long, folderId: Long?) {
        noteDao.setFolder(id, folderId)
    }

    override suspend fun bulkSetFolder(ids: List<Long>, folderId: Long?) {
        noteDao.bulkSetFolder(ids, folderId)
    }

    override suspend fun setLocked(id: Long, isLocked: Boolean) {
        noteDao.setLocked(id, isLocked)
    }

    override fun getChecklistItems(noteId: Long): Flow<List<ChecklistItemEntity>> =
        checklistDao.getItemsForNote(noteId)

    override suspend fun saveChecklistItems(noteId: Long, items: List<ChecklistItemEntity>) {
        database.withTransaction {
            checklistDao.deleteItemsForNote(noteId)
            val mapped = items.mapIndexed { idx, it -> it.copy(id = 0, noteId = noteId, orderIndex = idx) }
            checklistDao.insertItems(mapped)
        }
    }

    override suspend fun toggleChecklistItem(itemId: Long, isDone: Boolean) {
        checklistDao.setItemDone(itemId, isDone)
    }

    override fun getNoteVersions(noteId: Long): Flow<List<NoteVersionEntity>> =
        noteVersionDao.getVersionsForNote(noteId)

    override suspend fun restoreVersion(noteId: Long, version: NoteVersionEntity) {
        val current = noteDao.getNoteById(noteId) ?: return
        val updated = current.copy(
            title = version.title,
            body = version.body,
            modifiedTimestamp = System.currentTimeMillis()
        )
        noteDao.updateNote(updated)
    }

    override val allFoldersFlow: Flow<List<FolderEntity>> = folderDao.getAllFolders()

    override fun getSubfolders(parentId: Long): Flow<List<FolderEntity>> =
        folderDao.getSubfolders(parentId)

    override suspend fun getFolder(id: Long): FolderEntity? =
        folderDao.getFolderById(id)

    override suspend fun createFolder(name: String, parentId: Long?, colorId: Int): Long =
        folderDao.insertFolder(FolderEntity(name = name, parentId = parentId, colorId = colorId))

    override suspend fun updateFolder(folder: FolderEntity) {
        folderDao.updateFolder(folder)
    }

    override suspend fun deleteFolder(id: Long) {
        database.withTransaction {
            // Unset folder for any notes assigned to this folder
            database.openHelper.writableDatabase.execSQL("UPDATE notes SET folderId = NULL WHERE folderId = $id")
            folderDao.deleteFolderById(id)
        }
    }

    override val allTagsFlow: Flow<List<TagEntity>> = tagDao.getAllTags()

    override suspend fun searchTags(query: String): List<TagEntity> =
        tagDao.searchTags(query)

    override suspend fun createTag(name: String): Long {
        val trimmed = name.trim().removePrefix("#")
        val existing = tagDao.getTagByName(trimmed)
        return existing?.id ?: tagDao.insertTag(TagEntity(name = trimmed))
    }

    override suspend fun renameTag(id: Long, newName: String) {
        tagDao.updateTag(TagEntity(id = id, name = newName.trim().removePrefix("#")))
    }

    override suspend fun deleteTag(id: Long) {
        tagDao.deleteTagById(id)
    }

    override suspend fun mergeTags(sourceTagId: Long, targetTagId: Long) {
        tagDao.mergeTags(sourceTagId, targetTagId)
    }

    override suspend fun addTagToNote(noteId: Long, tagId: Long) {
        tagDao.insertNoteTagCrossRef(NoteTagCrossRef(noteId = noteId, tagId = tagId))
    }

    override suspend fun removeTagFromNote(noteId: Long, tagId: Long) {
        tagDao.removeTagFromNote(noteId, tagId)
    }

    private fun formatFtsQuery(query: String): String {
        // Sanitize and append '*' to each word for prefix matching in SQLite FTS
        val terms = query.trim().split("\\s+".toRegex())
            .filter { it.isNotBlank() }
            .map { it.replace("\"", "").replace("'", "") + "*" }
        return if (terms.isEmpty()) "" else terms.joinToString(" ")
    }

    override fun searchNotes(query: String): Flow<List<NoteWithDetails>> {
        val fts = formatFtsQuery(query)
        return if (fts.isBlank()) activeNotesFlow else noteDao.searchNotesFts(fts)
    }

    override fun searchArchive(query: String): Flow<List<NoteWithDetails>> {
        val fts = formatFtsQuery(query)
        return if (fts.isBlank()) archivedNotesFlow else noteDao.searchArchiveFts(fts)
    }

    override fun searchTrash(query: String): Flow<List<NoteWithDetails>> {
        val fts = formatFtsQuery(query)
        return if (fts.isBlank()) trashedNotesFlow else noteDao.searchTrashFts(fts)
    }

    override val recentSearchesFlow: Flow<List<RecentSearchEntity>> =
        recentSearchDao.getRecentSearches()

    override suspend fun recordRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isNotBlank()) {
            recentSearchDao.insertSearch(RecentSearchEntity(query = trimmed))
        }
    }

    override suspend fun deleteRecentSearch(id: Long) {
        recentSearchDao.deleteSearchById(id)
    }

    override suspend fun clearRecentSearches() {
        recentSearchDao.clearAll()
    }

    override val activeRemindersFlow: Flow<List<ReminderEntity>> =
        reminderDao.getActiveReminders()

    override fun getReminderForNoteFlow(noteId: Long): Flow<ReminderEntity?> =
        reminderDao.getReminderFlowForNote(noteId)

    override suspend fun setReminder(reminder: ReminderEntity): Long =
        reminderDao.insertReminder(reminder)

    override suspend fun deleteReminder(noteId: Long) {
        reminderDao.deleteReminderForNote(noteId)
    }

    override suspend fun markReminderCompleted(noteId: Long) {
        reminderDao.markCompleted(noteId)
    }

    override suspend fun snoozeReminder(noteId: Long, snoozeUntilEpoch: Long) {
        reminderDao.snoozeReminder(noteId, snoozeUntilEpoch)
    }

    override val statsFlow: Flow<QuickJotStats> = combine(
        activeNotesFlow,
        archivedNotesFlow,
        trashedNotesFlow,
        allTagsFlow,
        allFoldersFlow
    ) { active, archived, trashed, tags, folders ->
        var totalWords = 0
        var totalChars = 0
        var activeChecklists = 0
        var completedChecklistItems = 0
        var totalChecklistItems = 0

        active.forEach { noteWithDetails ->
            val note = noteWithDetails.note
            val textToCount = "${note.title} ${note.body}"
            val words = textToCount.trim().split("\\s+".toRegex()).count { it.isNotBlank() }
            totalWords += words
            totalChars += note.title.length + note.body.length

            if (note.noteType == "checklist" || noteWithDetails.checklistItems.isNotEmpty()) {
                activeChecklists++
                totalChecklistItems += noteWithDetails.checklistItems.size
                completedChecklistItems += noteWithDetails.checklistItems.count { it.isDone }
            }
        }

        QuickJotStats(
            totalNotes = active.size,
            totalWords = totalWords,
            totalCharacters = totalChars,
            activeChecklists = activeChecklists,
            completedChecklistItems = completedChecklistItems,
            totalChecklistItems = totalChecklistItems,
            totalTags = tags.size,
            totalFolders = folders.size,
            archivedNotes = archived.size,
            trashedNotes = trashed.size
        )
    }

    override suspend fun getAllActiveNotesForBackup(): List<NoteWithDetails> {
        return activeNotesFlow.first()
    }
}
