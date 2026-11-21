package com.example.notes.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.notes.core.datastore.UserSettingsDataSource
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

private val Context.userSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

val dataStoreModule = module {

    // 1. DataStore Instance
    single<DataStore<Preferences>> {
        androidContext().userSettingsDataStore
    }

    // 2. Data Source
    singleOf(::UserSettingsDataSource)
}