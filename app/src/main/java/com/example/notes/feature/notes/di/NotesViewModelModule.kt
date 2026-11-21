package com.example.notes.feature.notes.di

import com.example.notes.feature.notes.NotesViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val notesViewModelModule = module {
    viewModelOf(::NotesViewModel)
}