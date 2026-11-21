package com.example.notes.feature.editor.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.notes.core.navigation.Navigator
import com.example.notes.feature.editor.NoteEditorScreen
import com.example.notes.feature.editor.NoteEditorViewModel
import com.example.notes.feature.notes.navigation.NotesNavKey
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
data class NoteEditorNavKey(val noteId: Long) : NavKey

fun Navigator.navigateToNoteEditor(noteId: Long){
    navigate(NoteEditorNavKey(noteId))
}
fun EntryProviderScope<NavKey>.noteEditorEntry(navigator: Navigator){
    entry< NoteEditorNavKey>{key ->
        val viewModel = koinViewModel<NoteEditorViewModel> { parametersOf(key.noteId) }
        NoteEditorScreen(
            viewModel = viewModel,
            onBack = { navigator.goBack() },
            navigateToNote = {navigator.navigate(NotesNavKey)}
        )
    }
}