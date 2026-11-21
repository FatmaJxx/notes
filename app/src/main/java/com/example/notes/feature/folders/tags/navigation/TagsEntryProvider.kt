package com.example.notes.feature.folders.tags.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.notes.core.navigation.Navigator
import com.example.notes.feature.folders.tags.TagsScreen
import kotlinx.serialization.Serializable

@Serializable
data object TagsNavKey : NavKey

fun EntryProviderScope<NavKey>.tagsEntry(navigator: Navigator) {
    entry<TagsNavKey> {
        TagsScreen(
            onBack = { navigator.goBack() }
        )
    }
}