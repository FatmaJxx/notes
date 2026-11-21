package com.example.notes.feature.folders.lists.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.notes.core.navigation.Navigator
import com.example.notes.feature.folders.details.FolderType
import com.example.notes.feature.folders.details.navigation.navigateToFolderDetails
import com.example.notes.feature.folders.lists.FoldersScreen
import com.example.notes.feature.folders.tags.navigation.TagsNavKey
import com.example.notes.feature.folders.trash.navigation.TrashNavKey
import kotlinx.serialization.Serializable

@Serializable
object FoldersNavKey : NavKey

fun EntryProviderScope<NavKey>.foldersEntry(navigator: Navigator) {
    entry<FoldersNavKey> {
        FoldersScreen(
            onFolderClick = { folderType ->
                when (folderType) {
                    is FolderType.Trash -> navigator.navigate(TrashNavKey)
                    is FolderType.Tags -> navigator.navigate(TagsNavKey)
                    else -> navigator.navigateToFolderDetails(folderType)
                }
            }

        )
    }
}