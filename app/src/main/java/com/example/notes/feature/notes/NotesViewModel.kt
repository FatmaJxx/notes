package com.example.notes.feature.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.core.data.repository.NoteRepository
import com.example.notes.core.data.repository.SettingRepository
import com.example.notes.core.model.Note
import com.example.notes.core.model.ViewMode
import com.example.notes.feature.notes.NotesNavigationEvents.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
class NotesViewModel(
    private val noteRepository: NoteRepository,
    private val settingRepository: SettingRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<NotesUiState> =
        settingRepository.userSettings
            .flatMapLatest { settings ->
                noteRepository.getActiveNotes(settings.sortOrder)
                    .map { notes ->
                        NotesUiState(
                            notes = notes,
                            viewMode = settings.viewMode,
                            sortOrder = settings.sortOrder,
                            isLoading = false
                        )
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = NotesUiState(isLoading = true)
            )

    private val _uiEvent = Channel<NotesNavigationEvents>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onEvent(event: NotesUiEvent){
        when(event){
            NotesUiEvent.CreateNote -> {
                _uiEvent.trySend(NavigateToCreateNote)
            }
            NotesUiEvent.ToggleViewMode -> { toggleViewMode() }
            is NotesUiEvent.TogglePinned -> { togglePin(note = event.note) }
            is NotesUiEvent.EditNote -> {
                _uiEvent.trySend(NavigateToEditNote(event.noteId))
            }
        }
    }

    fun toggleViewMode() {
        viewModelScope.launch {
            val currentMode = state.value.viewMode
            val newMode = if (currentMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
            settingRepository.setViewMode(newMode)
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            noteRepository.setPinned(note.id, !note.isPinned)
        }
    }
}
