package com.example.notes.feature.folders.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.core.data.repository.NoteRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

class FolderDetailsViewModel(
    private val folder: FolderType,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiEvent = Channel<FolderDetailsNavigationEvents>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onEvent(event: FolderDetailsUiEvent) {
        when (event) {
            FolderDetailsUiEvent.BackPressed -> {
                _uiEvent.trySend(FolderDetailsNavigationEvents.NavigateBack)
            }

            FolderDetailsUiEvent.CreateNote -> {
                _uiEvent.trySend(FolderDetailsNavigationEvents.NavigateToCreateNote)
            }

            is FolderDetailsUiEvent.EditNote -> {
                _uiEvent.trySend(FolderDetailsNavigationEvents.NavigateToEditNote(event.noteId))
            }
        }
    }

    val state: StateFlow<FolderDetailsUiState> =
        loadNotesStream(folder)
            .map { notes ->

                val title = when (folder) {
                    FolderType.Archive -> "Archive"
                    FolderType.Favorites -> "Favorites"
                    is FolderType.Folder -> folder.name
                    FolderType.Pinned -> "Pinned Notes"
                    FolderType.Trash -> "Trash"
                    FolderType.Tags -> "Tags"
                }
                FolderDetailsUiState(folderName = title, notes = notes)

            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = FolderDetailsUiState()
            )

    private fun loadNotesStream(folder: FolderType) = when (folder) {
        FolderType.Archive -> noteRepository.getArchivedNotes()
        FolderType.Favorites -> noteRepository.getFavoriteNotes()
        is FolderType.Folder -> noteRepository.getNotesByFolderId(folder.id)
        FolderType.Pinned -> noteRepository.getPinnedNotes()
        FolderType.Trash -> noteRepository.getTrashNotes()
        FolderType.Tags -> noteRepository.getActiveNotes()
    }
}

sealed interface FolderType {
    data class Folder(val id: Long, val name: String) : FolderType
    data object Pinned : FolderType
    data object Favorites : FolderType
    data object Archive : FolderType
    data object Tags : FolderType
    data object Trash : FolderType
}

