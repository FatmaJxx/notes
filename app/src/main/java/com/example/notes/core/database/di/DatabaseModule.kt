package com.example.notes.core.database.di

import androidx.room.Room
import com.example.notes.core.database.NoteDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    // 1. Provide the Database instance
    single {
        Room.databaseBuilder(
            androidContext(),
            NoteDatabase::class.java,
            "note-database"
        ).build()
    }

    // 2. Provide each DAO from the database
    single { get<NoteDatabase>().noteDao() }
    single { get<NoteDatabase>().checklistDao() }
    single { get<NoteDatabase>().tagDao() }
    single { get<NoteDatabase>().folderDao() }
    single { get<NoteDatabase>().reminderDao() }
}