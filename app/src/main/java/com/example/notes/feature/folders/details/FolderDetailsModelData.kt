package com.example.notes.feature.folders.details

import com.example.notes.core.model.Note


data class FolderDetailsUiState(
    val folderName: String = "",
    val notes: List<Note> = emptyList(),
)

sealed interface FolderDetailsUiEvent {
    data object BackPressed : FolderDetailsUiEvent
    data object CreateNote : FolderDetailsUiEvent
    data class EditNote(val noteId: Long) : FolderDetailsUiEvent
}

sealed interface FolderDetailsNavigationEvents {
    data class NavigateToEditNote(val noteId: Long) : FolderDetailsNavigationEvents
    data object NavigateBack : FolderDetailsNavigationEvents
    data object NavigateToCreateNote : FolderDetailsNavigationEvents
}