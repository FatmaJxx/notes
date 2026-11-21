package com.example.notes.feature.editor.di

import com.example.notes.feature.editor.FoldersSheetViewModel
import com.example.notes.feature.editor.NoteEditorViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val noteEditorViewModelModule = module {
    viewModelOf(::NoteEditorViewModel)
    viewModelOf(::FoldersSheetViewModel)
}