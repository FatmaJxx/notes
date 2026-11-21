package com.example.notes.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.notes.core.model.Note

@Entity(
    tableName = "notes",
    indices = [
        Index("folderId"),
        Index("folderName"),
        Index("isArchived"),
        Index("isPinned"),
        Index("isFavorite")
    ]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val color: String, // NoteColor name
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val deletedAt: Long? = null,
    val folderId: Long? = null,
    val folderName: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis()
)

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        color = color.name,
        isPinned = isPinned,
        isFavorite = isFavorite,
        isArchived = isArchived,
        deletedAt = deletedAt,
        folderId = folderId,
        createdAt = createdAt,
        modifiedAt = modifiedAt
    )
}











