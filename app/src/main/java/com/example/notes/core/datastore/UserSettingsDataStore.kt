package com.example.notes.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.notes.core.model.AppColor
import com.example.notes.core.model.AppTheme
import com.example.notes.core.model.SortOrder
import com.example.notes.core.model.UserSettings
import com.example.notes.core.model.ViewMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserSettingsDataSource(
    private val dataStore: DataStore<Preferences>
) {
    val userSettings: Flow<UserSettings> =
        dataStore.data.map { preferences ->
            UserSettings(
                theme = preferences[THEME_KEY]
                    ?.let(AppTheme::valueOf)
                    ?: AppTheme.SYSTEM,

                dynamicColors = preferences[DYNAMIC_COLORS_KEY]
                    ?: true,

                sortOrder = preferences[SORT_ORDER_KEY]
                    ?.let(SortOrder::valueOf)
                    ?: SortOrder.MODIFIED_DESC,

                viewMode = preferences[VIEW_MODE_KEY]
                    ?.let(ViewMode::valueOf)
                    ?: ViewMode.LIST,

                defaultNoteColor = preferences[DEFAULT_NOTE_COLOR_KEY]
                    ?.let(AppColor::valueOf)
                    ?: AppColor.LAVENDER
            )
        }

    suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }

    suspend fun setDynamicColors(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DYNAMIC_COLORS_KEY] = enabled
        }
    }

    suspend fun setSortOrder(order: SortOrder) {
        dataStore.edit { preferences ->
            preferences[SORT_ORDER_KEY] = order.name
        }
    }

    suspend fun setViewMode(mode: ViewMode) {
        dataStore.edit { preferences ->
            preferences[VIEW_MODE_KEY] = mode.name
        }
    }

    suspend fun setDefaultNoteColor(color: AppColor) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_NOTE_COLOR_KEY] = color.name
        }
    }

    private companion object {
        val THEME_KEY = stringPreferencesKey("theme")
        val DYNAMIC_COLORS_KEY = booleanPreferencesKey("dynamic_colors")
        val SORT_ORDER_KEY = stringPreferencesKey("sort_order")
        val VIEW_MODE_KEY = stringPreferencesKey("view_mode")
        val DEFAULT_NOTE_COLOR_KEY = stringPreferencesKey("default_note_color")
    }
}