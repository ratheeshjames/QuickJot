package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY triggerTimeEpochMillis ASC")
    fun getActiveReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE noteId = :noteId LIMIT 1")
    suspend fun getReminderForNote(noteId: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE noteId = :noteId LIMIT 1")
    fun getReminderFlowForNote(noteId: Long): Flow<ReminderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE noteId = :noteId")
    suspend fun deleteReminderForNote(noteId: Long)

    @Query("UPDATE reminders SET isCompleted = 1 WHERE noteId = :noteId")
    suspend fun markCompleted(noteId: Long)

    @Query("UPDATE reminders SET snoozeUntilEpochMillis = :snoozeEpoch WHERE noteId = :noteId")
    suspend fun snoozeReminder(noteId: Long, snoozeEpoch: Long)

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 AND triggerTimeEpochMillis >= :currentEpoch ORDER BY triggerTimeEpochMillis ASC")
    suspend fun getUpcomingActiveReminders(currentEpoch: Long): List<ReminderEntity>
}
