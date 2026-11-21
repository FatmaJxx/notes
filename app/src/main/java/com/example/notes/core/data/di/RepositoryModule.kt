package com.example.notes.core.data.di

import com.example.notes.core.data.repository.FolderRepository
import com.example.notes.core.data.repository.FolderRepositoryImpl
import com.example.notes.core.data.repository.NoteRepository
import com.example.notes.core.data.repository.NoteRepositoryImpl
import com.example.notes.core.data.repository.ReminderRepository
import com.example.notes.core.data.repository.ReminderRepositoryImpl
import com.example.notes.core.data.repository.SettingRepository
import com.example.notes.core.data.repository.SettingRepositoryImpl
import com.example.notes.core.data.repository.TagRepository
import com.example.notes.core.data.repository.TagRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single<NoteRepository> { NoteRepositoryImpl(get(), get(), get(), get(), get()) }
    single<FolderRepository> { FolderRepositoryImpl(get()) }
    single<TagRepository> { TagRepositoryImpl(get()) }
    single<SettingRepository> { SettingRepositoryImpl(get()) }
    single<ReminderRepository> { ReminderRepositoryImpl(get()) }
}