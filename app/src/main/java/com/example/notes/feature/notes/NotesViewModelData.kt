package com.example.notes.feature.notes

import com.example.notes.core.model.Note
import com.example.notes.core.model.SortOrder
import com.example.notes.core.model.ViewMode

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val viewMode: ViewMode = ViewMode.GRID,
    val sortOrder: SortOrder = SortOrder.MODIFIED_DESC,
    val isLoading: Boolean = false
)

sealed interface NotesUiEvent {
    data object CreateNote : NotesUiEvent
    data object ToggleViewMode : NotesUiEvent
    data class TogglePinned(val note: Note) : NotesUiEvent
    data class EditNote(val noteId: Long) : NotesUiEvent
}

sealed interface NotesNavigationEvents {
    data object NavigateToCreateNote : NotesNavigationEvents

    data class NavigateToEditNote(val noteId: Long) : NotesNavigationEvents
}