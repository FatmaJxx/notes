package com.example.notes.feature.folders.details.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.notes.core.navigation.Navigator
import com.example.notes.feature.editor.navigation.NoteEditorNavKey
import com.example.notes.feature.editor.navigation.navigateToNoteEditor
import com.example.notes.feature.folders.details.FolderDetailScreen
import com.example.notes.feature.folders.details.FolderDetailsViewModel
import com.example.notes.feature.folders.details.FolderType
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
@Serializable
data class FolderDetailsNavKey(val folder: FolderType) : NavKey

fun Navigator.navigateToFolderDetails(folder: FolderType){
    navigate(FolderDetailsNavKey(folder))
}

fun EntryProviderScope<NavKey>.folderDetailsEntry(navigator: Navigator) {
    entry<FolderDetailsNavKey> { (folderType) ->
        val viewModel = koinViewModel<FolderDetailsViewModel> { parametersOf(folderType) }
        FolderDetailScreen(
            viewModel = viewModel,
            onNoteClick = navigator::navigateToNoteEditor,
            onCreateNote = { navigator.navigate(NoteEditorNavKey(0)) },
            onBack = { navigator.goBack() }
        )
    }
}