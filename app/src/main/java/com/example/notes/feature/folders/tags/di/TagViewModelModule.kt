package com.example.notes.feature.folders.tags.di

import com.example.notes.feature.folders.tags.TagsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val tagsViewModelModule = module {
    viewModelOf(::TagsViewModel)
}