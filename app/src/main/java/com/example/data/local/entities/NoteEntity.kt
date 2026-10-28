package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    indices = [
        Index("folderId"),
        Index("isTrash"),
        Index("isArchived"),
        Index("isPinned"),
        Index("modifiedTimestamp")
    ]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val body: String = "",
    val colorId: Int = 0,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isTrash: Boolean = false,
    val trashedTimestamp: Long? = null,
    val isLocked: Boolean = false,
    val folderId: Long? = null,
    val orderIndex: Int = 0,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val modifiedTimestamp: Long = System.currentTimeMillis(),
    val characterLimit: Int = 1000,
    val noteType: String = "text" // "text" or "checklist"
)
