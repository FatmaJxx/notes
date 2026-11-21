package com.example.notes.core.database.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.notes.core.model.AppColor
import com.example.notes.core.model.Note

data class NoteWithDetails(
    @Embedded val note: NoteEntity,
    // 1-to-Many: One Note -> Multiple Checklist Items
    @Relation(
        parentColumn = "id",
        entityColumn = "noteId"
    )
    val checklist: List<ChecklistItemEntity>,

    // Many-to-Many: Notes <-> Tags via NoteTagCrossRef
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NoteTagCrossRef::class,
            parentColumn = "noteId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>,

    // 1-to-1 (Optional): One Note -> One Reminder
    @Relation(
        parentColumn = "id",
        entityColumn = "noteId"
    )
    val reminder: ReminderEntity?,

    // Many-to-1 (Optional): Many Notes -> One Folder
    @Relation(
        parentColumn = "folderId",
        entityColumn = "id"
    )
    val folder: FolderEntity?
)

fun NoteWithDetails.toDomain(): Note {
    return Note(
        id = note.id,
        title = note.title,
        content = note.content,
        color = try {
            AppColor.valueOf(note.color)
        } catch (_: Exception) {
            AppColor.LAVENDER
        },
        isPinned = note.isPinned,
        isFavorite = note.isFavorite,
        isArchived = note.isArchived,
        deletedAt = note.deletedAt,
        folderId = note.folderId,
        createdAt = note.createdAt,
        modifiedAt = note.modifiedAt,
        tags = tags.map { it.toDomain() },
        checklist = checklist.map { it.toDomain() },
        reminder = reminder?.toDomain()
    )
}