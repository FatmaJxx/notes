package com.example.notes.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.notes.core.model.ChecklistItem

@Entity(
    tableName = "checklist_items",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("noteId")]
)
data class ChecklistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val noteId: Long,
    val text: String,
    val isDone: Boolean = false,
)

fun ChecklistItemEntity.toDomain(): ChecklistItem = ChecklistItem(
    id = id,
    noteId = noteId,
    text = text,
    isDone = isDone,
)

fun ChecklistItem.toEntity(): ChecklistItemEntity = ChecklistItemEntity(
    id = id,
    noteId = noteId,
    text = text,
    isDone = isDone,
)
