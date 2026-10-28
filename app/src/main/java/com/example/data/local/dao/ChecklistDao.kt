package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.ChecklistItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistDao {

    @Query("SELECT * FROM checklist_items WHERE noteId = :noteId ORDER BY orderIndex ASC")
    fun getItemsForNote(noteId: Long): Flow<List<ChecklistItemEntity>>

    @Query("SELECT * FROM checklist_items WHERE noteId = :noteId ORDER BY orderIndex ASC")
    suspend fun getItemsForNoteSync(noteId: Long): List<ChecklistItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ChecklistItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ChecklistItemEntity>)

    @Update
    suspend fun updateItem(item: ChecklistItemEntity)

    @Delete
    suspend fun deleteItem(item: ChecklistItemEntity)

    @Query("DELETE FROM checklist_items WHERE noteId = :noteId")
    suspend fun deleteItemsForNote(noteId: Long)

    @Query("UPDATE checklist_items SET isDone = :isDone WHERE id = :id")
    suspend fun setItemDone(id: Long, isDone: Boolean)

    @Query("UPDATE checklist_items SET orderIndex = :orderIndex WHERE id = :id")
    suspend fun updateOrderIndex(id: Long, orderIndex: Int)
}
