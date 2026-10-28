package com.example.data.local.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.data.local.entities.ChecklistItemEntity
import com.example.data.local.entities.FolderEntity
import com.example.data.local.entities.NoteEntity
import com.example.data.local.entities.NoteTagCrossRef
import com.example.data.local.entities.ReminderEntity
import com.example.data.local.entities.TagEntity

data class NoteWithDetails(
    @Embedded
    val note: NoteEntity,

    @Relation(
        parentColumn = "folderId",
        entityColumn = "id"
    )
    val folder: FolderEntity? = null,

    @Relation(
        parentColumn = "id",
        entityColumn = "noteId"
    )
    val checklistItems: List<ChecklistItemEntity> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NoteTagCrossRef::class,
            parentColumn = "noteId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "noteId"
    )
    val reminder: ReminderEntity? = null
)
