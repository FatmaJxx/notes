package com.example.notes.feature.folders.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.core.data.repository.NoteRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrashViewModel(
    private val notesRepository: NoteRepository
) : ViewModel() {

    private val _uiEvent = Channel< TrashNavigationEvents>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _state = MutableStateFlow(TrashUiState())
    val state: StateFlow<TrashUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            notesRepository.getTrashNotes().collect{ notes ->
                _state.update { it.copy(trashNotes = notes) }

            }
        }
    }

    fun onEvent(event: TrashUiEvent){
        when(event){
            TrashUiEvent.BackPressed -> {
                _uiEvent.trySend(TrashNavigationEvents.NavigateBack)
            }

            TrashUiEvent.ConfirmEmptyTrash -> {
                emptyTrash()
            }
            is TrashUiEvent.RestoreNote -> {
                restoreNoteById(event.noteId)
            }
            is TrashUiEvent.ConfirmDeleteNote -> deleteNoteById(event.noteId)
            TrashUiEvent.DismissDeleteNoteDialog -> {
                _state.update { it.copy(noteIdToBeDeletedPermanently = null) }
            }
            TrashUiEvent.DismissEmptyTrashDialog -> {
                _state.update { it.copy(showEmptyTrashDialog = false) }
            }
            is TrashUiEvent.ShowDeleteNoteDialog -> {
                _state.update { it.copy(noteIdToBeDeletedPermanently = event.noteId) }
            }
            TrashUiEvent.ShowEmptyTrashDialog -> {
                _state.update { it.copy(showEmptyTrashDialog = true) }
            }
        }
    }

    private fun deleteNoteById(noteId: Long){
        viewModelScope.launch {
            notesRepository.permanentlyDelete(noteId)
            _state.update {
                it.copy(noteIdToBeDeletedPermanently = null)
            }
        }
    }

    private fun restoreNoteById(noteId: Long){
        viewModelScope.launch {
            notesRepository.restoreFromTrash(noteId)
        }
    }

    private fun emptyTrash(){
        viewModelScope.launch {
            notesRepository.emptyTrash()
            _state.update { it.copy(showEmptyTrashDialog = false) }
        }
    }
}