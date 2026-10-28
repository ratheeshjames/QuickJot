package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["noteId"], unique = true), Index("triggerTimeEpochMillis")]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noteId: Long,
    val triggerTimeEpochMillis: Long,
    val repeatInterval: String = "NONE", // "NONE", "DAILY", "WEEKLY", "MONTHLY", "CUSTOM"
    val customIntervalDays: Int = 1,
    val isCompleted: Boolean = false,
    val snoozeUntilEpochMillis: Long? = null
)
