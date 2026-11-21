package com.example.notes.core.data.repository

import com.example.notes.core.datastore.UserSettingsDataSource
import com.example.notes.core.model.AppColor
import com.example.notes.core.model.AppTheme
import com.example.notes.core.model.SortOrder
import com.example.notes.core.model.UserSettings
import com.example.notes.core.model.ViewMode
import kotlinx.coroutines.flow.Flow

class SettingRepositoryImpl(
    private val dataSource: UserSettingsDataSource
) : SettingRepository {

    override val userSettings: Flow<UserSettings> =
        dataSource.userSettings

    override suspend fun setTheme(theme: AppTheme) {
        dataSource.setTheme(theme)
    }

    override suspend fun setDynamicColors(enabled: Boolean) {
        dataSource.setDynamicColors(enabled)
    }

    override suspend fun setDefaultNoteColor(color: AppColor) {
        dataSource.setDefaultNoteColor(color)
    }

    override suspend fun setSortOrder(order: SortOrder) {
        dataSource.setSortOrder(order)
    }

    override suspend fun setViewMode(mode: ViewMode) {
        dataSource.setViewMode(mode)
    }
}