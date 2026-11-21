package com.example.notes.core.data.repository

import com.example.notes.core.model.AppColor
import com.example.notes.core.model.AppTheme
import com.example.notes.core.model.SortOrder
import com.example.notes.core.model.UserSettings
import com.example.notes.core.model.ViewMode
import kotlinx.coroutines.flow.Flow

interface SettingRepository {
    val userSettings: Flow<UserSettings>

    suspend fun setTheme(theme: AppTheme)
    suspend fun setDynamicColors(enabled: Boolean)
    suspend fun setDefaultNoteColor(color: AppColor)
    suspend fun setSortOrder(order: SortOrder)
    suspend fun setViewMode(mode: ViewMode)
}