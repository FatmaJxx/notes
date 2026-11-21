package com.example.notes.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.core.data.repository.SettingRepository
import com.example.notes.core.model.AppColor
import com.example.notes.core.model.AppTheme
import com.example.notes.core.model.SortOrder
import com.example.notes.core.model.ViewMode
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> =
        combine(
            _state,
            settingsRepository.userSettings
        ) { currentState, userSettings ->
            currentState.copy(
                theme = userSettings.theme,
                dynamicColors = userSettings.dynamicColors,
                defaultNoteColor = userSettings.defaultNoteColor,
                sortOrder = userSettings.sortOrder,
                viewMode = userSettings.viewMode
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState()
        )

    private val _uiEvent = Channel<SettingsNavigationEvents>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            SettingsUiEvent.AppearanceChanged -> {
                _uiEvent.trySend(SettingsNavigationEvents.NavigateToAppearanceScreen)
            }
            is SettingsUiEvent.DynamicCheckedChanged -> {
                _state.update { it.copy(dynamicColors = event.checked) }
                setDynamicColors(event.checked)
            }
            SettingsUiEvent.ToggleSortOrderDialog -> {
                _state.update { it.copy(showSortingOrderDialog = !it.showSortingOrderDialog) }
            }
            SettingsUiEvent.ViewModeChanged -> {
                val viewMode =
                    if (_state.value.viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
                _state.update { it.copy(viewMode = viewMode) }
                setViewMode(viewMode)
            }

            is SettingsUiEvent.SortOrderChanged -> {
                _state.update { it.copy(sortOrder = event.order, showSortingOrderDialog = false) }
                setSortOrder(event.order)
            }

            SettingsUiEvent.ToggleAlertDialog -> {
                _state.update { it.copy(showAboutDialog = !it.showAboutDialog) }
            }

            SettingsUiEvent.BackPressed -> {
                _uiEvent.trySend(SettingsNavigationEvents.NavigateBack)
            }

            is SettingsUiEvent.ThemeChanged -> {
                _state.update { it.copy(theme = event.theme) }
                setTheme(event.theme)
            }

            is SettingsUiEvent.NoteDefaultColorChanged -> {
                _state.update { it.copy(defaultNoteColor = event.color) }
                setDefaultNoteColor(event.color)
            }
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
        }
    }

    fun setDynamicColors(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDynamicColors(enabled)
        }
    }

    fun setDefaultNoteColor(color: AppColor) {
        viewModelScope.launch {
            settingsRepository.setDefaultNoteColor(color)
        }
    }

    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch {
            settingsRepository.setSortOrder(order)
        }
    }

    fun setViewMode(mode: ViewMode) {
        viewModelScope.launch {
            settingsRepository.setViewMode(mode)
        }
    }
}
