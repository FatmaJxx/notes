package com.example.notes.feature.folders.trash.di

import com.example.notes.feature.folders.trash.TrashViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val trashViewModelModule = module {
    viewModelOf(::TrashViewModel)
}