package com.example.notes.feature.settings.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.notes.core.navigation.Navigator
import com.example.notes.feature.settings.AppearanceScreen
import com.example.notes.feature.settings.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable
data object SettingsNavKey : NavKey


fun EntryProviderScope<NavKey>.settingsEntry(navigator: Navigator) {
    entry<SettingsNavKey> {
        SettingsScreen(
            onNavigateToAppearance = { navigator.navigate(AppearanceNavKey) }
        )
    }
}

@Serializable
data object AppearanceNavKey : NavKey


fun EntryProviderScope<NavKey>.appearanceEntry(navigator: Navigator) {
    entry<AppearanceNavKey> {
        AppearanceScreen(onBack = { navigator.goBack() })
    }
}