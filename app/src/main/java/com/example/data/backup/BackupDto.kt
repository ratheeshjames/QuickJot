package com.example.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class QuickJotBackupDto(
    val version: Int = 1,
    val createdAtEpoch: Long = System.currentTimeMillis(),
    val notes: List<BackupNoteItemDto> = emptyList(),
    val folders: List<BackupFolderItemDto> = emptyList(),
    val tags: List<BackupTagItemDto> = emptyList()
)

@Serializable
data class BackupNoteItemDto(
    val id: Long,
    val title: String,
    val body: String,
    val colorId: Int,
    val isPinned: Boolean,
    val isArchived: Boolean,
    val isLocked: Boolean,
    val folderId: Long?,
    val noteType: String,
    val createdTimestamp: Long,
    val modifiedTimestamp: Long,
    val checklistItems: List<BackupChecklistItemDto> = emptyList(),
    val tagNames: List<String> = emptyList()
)

@Serializable
data class BackupChecklistItemDto(
    val text: String,
    val isDone: Boolean,
    val orderIndex: Int
)

@Serializable
data class BackupFolderItemDto(
    val id: Long,
    val name: String,
    val parentId: Long?,
    val colorId: Int
)

@Serializable
data class BackupTagItemDto(
    val id: Long,
    val name: String
)
