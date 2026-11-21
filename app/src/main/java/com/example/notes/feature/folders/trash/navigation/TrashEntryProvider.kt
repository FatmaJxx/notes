package com.example.notes.feature.folders.trash.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.notes.core.navigation.Navigator
import com.example.notes.feature.folders.trash.TrashScreen
import kotlinx.serialization.Serializable

@Serializable
data object TrashNavKey : NavKey

fun EntryProviderScope<NavKey>.trashEntry(navigator: Navigator) {
    entry<TrashNavKey> {
        TrashScreen(
            onBack = { navigator.goBack() }
        )
    }
}