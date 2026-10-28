package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entities.NoteTagCrossRef
import com.example.data.local.entities.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun searchTags(query: String): List<TagEntity>

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): TagEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    @Update
    suspend fun updateTag(tag: TagEntity)

    @Delete
    suspend fun deleteTag(tag: TagEntity)

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteTagById(id: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteTagCrossRef(crossRef: NoteTagCrossRef)

    @Delete
    suspend fun deleteNoteTagCrossRef(crossRef: NoteTagCrossRef)

    @Query("DELETE FROM note_tag_cross_ref WHERE noteId = :noteId AND tagId = :tagId")
    suspend fun removeTagFromNote(noteId: Long, tagId: Long)

    @Query("DELETE FROM note_tag_cross_ref WHERE noteId = :noteId")
    suspend fun clearTagsForNote(noteId: Long)

    @Transaction
    suspend fun mergeTags(sourceTagId: Long, targetTagId: Long) {
        // Query notes with source tag and reassign to target tag
        insertCrossRefsForMerge(sourceTagId, targetTagId)
        deleteCrossRefsForTag(sourceTagId)
        deleteTagById(sourceTagId)
    }

    @Query("""
        INSERT OR IGNORE INTO note_tag_cross_ref (noteId, tagId)
        SELECT noteId, :targetTagId FROM note_tag_cross_ref WHERE tagId = :sourceTagId
    """)
    suspend fun insertCrossRefsForMerge(sourceTagId: Long, targetTagId: Long)

    @Query("DELETE FROM note_tag_cross_ref WHERE tagId = :tagId")
    suspend fun deleteCrossRefsForTag(tagId: Long)

    @Query("SELECT COUNT(*) FROM tags")
    fun getTagsCount(): Flow<Int>
}
