package com.example.notes.feature.folders.lists

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.core.data.repository.FolderRepository
import com.example.notes.core.data.repository.NoteRepository
import com.example.notes.core.model.AppColor
import com.example.notes.core.model.Folder
import com.example.notes.feature.folders.lists.FoldersNavigationEvents.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FoldersViewModel(
    private val foldersRepository: FolderRepository,
    notesRepository: NoteRepository
) : ViewModel() {

    fun onEvent(event: FoldersUiEvent) {
        when (event) {
            is FoldersUiEvent.OpenFolder -> {
                _uiEvent.trySend(NavigateToFolderDetails(event.folder))
            }

            FoldersUiEvent.ToggleDialog -> {
                toggleCreateDialog()
            }

            is FoldersUiEvent.CreateFolder -> {
                createFolder(event.folderName, event.color)
            }

            is FoldersUiEvent.FolderNameChange -> {
                onFolderNameChange(event.folderName)
            }
        }
    }

    private val _uiEvent = Channel<FoldersNavigationEvents>()
    val uiEvent = _uiEvent.receiveAsFlow()
    private val _showCreateDialog = MutableStateFlow(false)

    var newFolderName by mutableStateOf("")
        private set



    val folderState: StateFlow<FoldersUiState> = combine(
        foldersRepository.getAllFolders(),
        notesRepository.getActiveNotes(),
        notesRepository.getArchivedNotes(),
        notesRepository.getTrashNotes(),
        _showCreateDialog
    ) { folders, allNotes, archivedNotes, trashNotes, showCreateDialog ->
        val folderList = folders.map { folder ->
            val count = allNotes.count { it.folderId == folder.id }
            FolderWithNotes(folder, count)
        }
        FoldersUiState(
            folders = folderList,
            archivedCount = archivedNotes.size,
            trashCount = trashNotes.size,
            pinnedCount = allNotes.count { it.isPinned },
            favoritesCount = allNotes.count { it.isFavorite },
            showCreateDialog = showCreateDialog,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FoldersUiState(isLoading = true)
    )

    fun onFolderNameChange(name: String) {
        newFolderName = name
    }

    fun toggleCreateDialog() {
        _showCreateDialog.value = !_showCreateDialog.value
    }

    fun createFolder(name: String, color: AppColor) {
        viewModelScope.launch {
            foldersRepository.createFolder(Folder(name = name, color = color))
            newFolderName = ""
            toggleCreateDialog()
        }
    }
}