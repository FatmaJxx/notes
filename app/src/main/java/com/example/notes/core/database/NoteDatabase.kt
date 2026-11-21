package com.example.notes.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.notes.core.database.dao.ChecklistDao
import com.example.notes.core.database.dao.FolderDao
import com.example.notes.core.database.dao.NoteDao
import com.example.notes.core.database.dao.ReminderDao
import com.example.notes.core.database.dao.TagDao
import com.example.notes.core.database.entity.NoteEntity
import com.example.notes.core.database.entity.ChecklistItemEntity
import com.example.notes.core.database.entity.TagEntity
import com.example.notes.core.database.entity.NoteTagCrossRef
import com.example.notes.core.database.entity.FolderEntity
import com.example.notes.core.database.entity.ReminderEntity

@Database(
    entities = [
        NoteEntity::class,
        ChecklistItemEntity::class,
        TagEntity::class,
        NoteTagCrossRef::class,
        FolderEntity::class,
        ReminderEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun checklistDao(): ChecklistDao
    abstract fun tagDao(): TagDao
    abstract fun folderDao(): FolderDao
    abstract fun reminderDao(): ReminderDao
}