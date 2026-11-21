package com.example.notes.feature.folders.lists.di

import com.example.notes.feature.folders.lists.FoldersViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val foldersViewModelModule = module {
    viewModelOf(::FoldersViewModel)
}