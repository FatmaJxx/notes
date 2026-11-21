package com.example.notes.feature.search.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.notes.core.navigation.Navigator
import com.example.notes.feature.editor.navigation.navigateToNoteEditor
import com.example.notes.feature.search.SearchScreen
import kotlinx.serialization.Serializable

@Serializable
data object SearchNavKey : NavKey


fun EntryProviderScope<NavKey>.searchEntry(navigator: Navigator){
    entry<SearchNavKey> {
        SearchScreen(
            onNoteClick = navigator::navigateToNoteEditor
        )
    }
}