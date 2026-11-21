package com.example.notes.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.notes.core.model.Reminder
import com.example.notes.core.model.RepeatInterval

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
    indices = [Index("noteId")]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val noteId: Long,
    val timestamp: Long,
    val repeatInterval: String = "ONCE",
    val isActive: Boolean = true
)

fun ReminderEntity.toDomain(): Reminder = Reminder(
    id = id,
    noteId = noteId,
    timestamp = timestamp,
    repeatInterval = try {
        RepeatInterval.valueOf(repeatInterval)
    } catch (_: Exception) {
        RepeatInterval.ONCE
    },
    isActive = isActive
)

fun Reminder.toEntity(): ReminderEntity = ReminderEntity(
    id = id,
    noteId = noteId,
    timestamp = timestamp,
    repeatInterval = repeatInterval.name,
    isActive = isActive
)