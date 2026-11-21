package com.example.notes.feature.notes.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.notes.core.navigation.Navigator
import com.example.notes.feature.editor.navigation.NoteEditorNavKey
import com.example.notes.feature.editor.navigation.navigateToNoteEditor
import com.example.notes.feature.notes.NotesScreen
import kotlinx.serialization.Serializable

@Serializable
object NotesNavKey : NavKey

fun EntryProviderScope<NavKey>.notesEntry(navigator: Navigator) {
    entry<NotesNavKey> {
        NotesScreen(
            onNoteEdit = navigator::navigateToNoteEditor,
            onCreateNote = { navigator.navigate(NoteEditorNavKey(0)) },
        )
    }
}