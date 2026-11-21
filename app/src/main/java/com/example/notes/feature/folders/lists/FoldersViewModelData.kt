package com.example.notes.feature.folders.lists

import com.example.notes.core.model.AppColor
import com.example.notes.core.model.Folder
import com.example.notes.feature.folders.details.FolderType

data class FolderWithNotes(
    val folder: Folder,
    val count: Int
)

data class FoldersUiState(
    val folders: List<FolderWithNotes> = emptyList(),
    val archivedCount: Int = 0,
    val trashCount: Int = 0,
    val pinnedCount: Int = 0,
    val favoritesCount: Int = 0,
    val showCreateDialog: Boolean = false,
    val isLoading: Boolean = false
)

sealed interface FoldersUiEvent {
    data class OpenFolder(val folder: FolderType) : FoldersUiEvent
    data object ToggleDialog : FoldersUiEvent
    data class CreateFolder(val folderName: String, val color: AppColor) : FoldersUiEvent

    data class FolderNameChange(val folderName: String) : FoldersUiEvent

}

sealed interface FoldersNavigationEvents {
    data class NavigateToFolderDetails(val folder: FolderType) : FoldersNavigationEvents
}

