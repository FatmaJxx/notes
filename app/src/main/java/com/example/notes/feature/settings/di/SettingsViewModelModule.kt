package com.example.notes.feature.settings.di

import com.example.notes.feature.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsViewModelModule = module {
    viewModelOf(::SettingsViewModel)
}