package com.example.notes.feature.folders.details.di

import com.example.notes.feature.folders.details.FolderDetailsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val folderDetailsViewModelModule = module {
    viewModelOf(::FolderDetailsViewModel)
}