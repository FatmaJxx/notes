package com.example.notes.feature.settings

import com.example.notes.core.model.AppColor
import com.example.notes.core.model.AppTheme
import com.example.notes.core.model.SortOrder
import com.example.notes.core.model.ViewMode

data class SettingsUiState(
    val theme: AppTheme = AppTheme.LIGHT,
    val dynamicColors: Boolean = false,
    val defaultNoteColor: AppColor = AppColor.LAVENDER,
    val sortOrder: SortOrder = SortOrder.MODIFIED_DESC,
    val viewMode: ViewMode = ViewMode.GRID,
    val showSortingOrderDialog: Boolean = false,
    val showAboutDialog: Boolean = false
)

sealed interface SettingsUiEvent{
    data object AppearanceChanged: SettingsUiEvent
    data class DynamicCheckedChanged(val checked: Boolean) : SettingsUiEvent
    data object ToggleSortOrderDialog: SettingsUiEvent
    data object ViewModeChanged : SettingsUiEvent
    data object ToggleAlertDialog: SettingsUiEvent
    data object BackPressed: SettingsUiEvent
    data class SortOrderChanged(val order: SortOrder) : SettingsUiEvent
    data class ThemeChanged(val theme: AppTheme) : SettingsUiEvent
    data class NoteDefaultColorChanged(val color: AppColor) : SettingsUiEvent
}

sealed interface SettingsNavigationEvents {
    data object NavigateToAppearanceScreen : SettingsNavigationEvents
    data object NavigateBack: SettingsNavigationEvents
}