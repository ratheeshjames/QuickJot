package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entities.NoteEntity
import com.example.data.local.models.NoteWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Transaction
    @Query("SELECT * FROM notes WHERE isTrash = 0 AND isArchived = 0 ORDER BY isPinned DESC, orderIndex ASC, modifiedTimestamp DESC")
    fun getActiveNotesWithDetails(): Flow<List<NoteWithDetails>>

    @Transaction
    @Query("SELECT * FROM notes WHERE isTrash = 0 AND isArchived = 1 ORDER BY modifiedTimestamp DESC")
    fun getArchivedNotesWithDetails(): Flow<List<NoteWithDetails>>

    @Transaction
    @Query("SELECT * FROM notes WHERE isTrash = 1 ORDER BY trashedTimestamp DESC")
    fun getTrashedNotesWithDetails(): Flow<List<NoteWithDetails>>

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteWithDetailsById(id: Long): NoteWithDetails?

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun getNoteWithDetailsFlowById(id: Long): Flow<NoteWithDetails?>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    @Query("DELETE FROM notes WHERE isTrash = 1")
    suspend fun emptyTrash()

    @Query("DELETE FROM notes WHERE isTrash = 1 AND trashedTimestamp <= :thresholdTimestamp")
    suspend fun purgeOldTrashedNotes(thresholdTimestamp: Long): Int

    @Query("UPDATE notes SET isTrash = 1, trashedTimestamp = :timestamp, isPinned = 0 WHERE id = :id")
    suspend fun moveToTrash(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET isTrash = 1, trashedTimestamp = :timestamp, isPinned = 0 WHERE id IN (:ids)")
    suspend fun bulkMoveToTrash(ids: List<Long>, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET isTrash = 0, trashedTimestamp = NULL WHERE id = :id")
    suspend fun restoreFromTrash(id: Long)

    @Query("UPDATE notes SET isTrash = 0, trashedTimestamp = NULL WHERE id IN (:ids)")
    suspend fun bulkRestoreFromTrash(ids: List<Long>)

    @Query("UPDATE notes SET isArchived = :isArchived, isPinned = 0 WHERE id = :id")
    suspend fun setArchived(id: Long, isArchived: Boolean)

    @Query("UPDATE notes SET isArchived = :isArchived, isPinned = 0 WHERE id IN (:ids)")
    suspend fun bulkSetArchived(ids: List<Long>, isArchived: Boolean)

    @Query("UPDATE notes SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPinned(id: Long, isPinned: Boolean)

    @Query("UPDATE notes SET colorId = :colorId WHERE id = :id")
    suspend fun setColor(id: Long, colorId: Int)

    @Query("UPDATE notes SET colorId = :colorId WHERE id IN (:ids)")
    suspend fun bulkSetColor(ids: List<Long>, colorId: Int)

    @Query("UPDATE notes SET folderId = :folderId WHERE id = :id")
    suspend fun setFolder(id: Long, folderId: Long?)

    @Query("UPDATE notes SET folderId = :folderId WHERE id IN (:ids)")
    suspend fun bulkSetFolder(ids: List<Long>, folderId: Long?)

    @Query("UPDATE notes SET isLocked = :isLocked WHERE id = :id")
    suspend fun setLocked(id: Long, isLocked: Boolean)

    @Query("UPDATE notes SET orderIndex = :orderIndex WHERE id = :id")
    suspend fun updateOrderIndex(id: Long, orderIndex: Int)

    // Full-Text Search via FTS virtual table
    @Transaction
    @Query("""
        SELECT n.* FROM notes n 
        JOIN notes_fts fts ON n.id = fts.rowid 
        WHERE notes_fts MATCH :query 
        AND n.isTrash = 0 AND n.isArchived = 0
        ORDER BY n.modifiedTimestamp DESC
    """)
    fun searchNotesFts(query: String): Flow<List<NoteWithDetails>>

    // Search in Trash
    @Transaction
    @Query("""
        SELECT n.* FROM notes n 
        JOIN notes_fts fts ON n.id = fts.rowid 
        WHERE notes_fts MATCH :query 
        AND n.isTrash = 1
        ORDER BY n.trashedTimestamp DESC
    """)
    fun searchTrashFts(query: String): Flow<List<NoteWithDetails>>

    // Search in Archive
    @Transaction
    @Query("""
        SELECT n.* FROM notes n 
        JOIN notes_fts fts ON n.id = fts.rowid 
        WHERE notes_fts MATCH :query 
        AND n.isArchived = 1 AND n.isTrash = 0
        ORDER BY n.modifiedTimestamp DESC
    """)
    fun searchArchiveFts(query: String): Flow<List<NoteWithDetails>>

    // Quick Stats
    @Query("SELECT COUNT(*) FROM notes WHERE isTrash = 0 AND isArchived = 0")
    fun getActiveNotesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notes WHERE isTrash = 1")
    fun getTrashNotesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notes WHERE isArchived = 1 AND isTrash = 0")
    fun getArchiveNotesCount(): Flow<Int>

    @Query("SELECT * FROM notes WHERE isTrash = 0 AND isArchived = 0")
    suspend fun getAllActiveNotesForBackup(): List<NoteEntity>
}
