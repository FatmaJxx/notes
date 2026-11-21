package com.example.notes.feature.folders.trash

import com.example.notes.core.model.Note

data class TrashUiState(
    val trashNotes: List<Note> = emptyList(),
    val noteIdToBeDeletedPermanently: Long? = null,
    val showEmptyTrashDialog: Boolean = false
)

sealed interface TrashUiEvent {
    data object BackPressed : TrashUiEvent

    data object ShowEmptyTrashDialog : TrashUiEvent
    data object DismissEmptyTrashDialog : TrashUiEvent
    data object ConfirmEmptyTrash : TrashUiEvent

    data class ShowDeleteNoteDialog(val noteId: Long) : TrashUiEvent
    data object DismissDeleteNoteDialog : TrashUiEvent
    data class ConfirmDeleteNote(val noteId: Long) : TrashUiEvent

    data class RestoreNote(val noteId: Long) : TrashUiEvent
}

sealed interface TrashNavigationEvents {
    data object NavigateBack : TrashNavigationEvents
}