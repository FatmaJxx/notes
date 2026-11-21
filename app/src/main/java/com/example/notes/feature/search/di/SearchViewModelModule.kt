package com.example.notes.feature.search.di

import com.example.notes.feature.search.SearchViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val searchViewModelModule = module {
    viewModelOf(::SearchViewModel)
}