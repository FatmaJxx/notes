package com.example.notes.feature.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.core.data.repository.FolderRepository
import com.example.notes.core.data.repository.NoteRepository
import com.example.notes.core.data.repository.SettingRepository
import com.example.notes.core.model.ChecklistItem
import com.example.notes.core.model.Note
import com.example.notes.core.model.Reminder
import com.example.notes.core.model.RepeatInterval
import com.example.notes.core.model.Tag
import com.example.notes.core.notification.AlarmScheduler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NoteEditorViewModel(
    val noteId: Long,
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository,
    private val settingRepository: SettingRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _state = MutableStateFlow(NoteEditorUiState())
    val state: StateFlow<NoteEditorUiState> = _state.asStateFlow()

    private val _uiEvent = Channel<NoteEditorNavigationEvents>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        if (noteId != 0L) {
            loadExistingNote(id = noteId)
        } else {

            viewModelScope.launch {
                settingRepository.userSettings.collect { userSettings ->
                    _state.update { it.copy(color = userSettings.defaultNoteColor) }

                }
            }
        }
    }

    fun onEvent(event: NoteEditorUiEvent) {
        when (event) {
            NoteEditorUiEvent.BackPressed -> {
                _uiEvent.trySend(NoteEditorNavigationEvents.NavigateBack)
            }

            is NoteEditorUiEvent.SaveNote -> {
                saveNote()
            }

            NoteEditorUiEvent.ToggleFavorite -> {
                toggleFavorite()
            }

            NoteEditorUiEvent.AddCheckListItem -> {
                addChecklistItem()
            }

            NoteEditorUiEvent.DeleteNote -> {
                deleteNote()
            }

            NoteEditorUiEvent.ToggleArchive -> {
                toggleArchive()
            }

            NoteEditorUiEvent.ToggleColorPicker -> {
                _state.update { it.copy(showColorPicker = !it.showColorPicker) }
            }

            NoteEditorUiEvent.TogglePin -> {
                togglePin()
            }

            is NoteEditorUiEvent.ChooseNoteColor -> {
                _state.update { it.copy(color = event.color, showColorPicker = false) }
            }

            is NoteEditorUiEvent.DeleteCheckListItem -> {
                removeChecklistItem(index = event.index)
            }

            is NoteEditorUiEvent.UpdateCheckListItem -> {
                updateChecklistItem(
                    index = event.index,
                    text = event.text, isDone = event.isDone
                )
            }

            is NoteEditorUiEvent.RemoveTag -> {
                removeTag(tag = event.tag)
            }

            is NoteEditorUiEvent.AddTag -> {
                addTag(event.tagName)
            }

            is NoteEditorUiEvent.SetReminder -> {
                setReminder(event.timestamp, event.interval)
            }

            NoteEditorUiEvent.ShowDeleteDialog -> {
                setShowDeleteDialog()
            }

            is NoteEditorUiEvent.ContentChanged -> {
                onContentChange(event.text)
            }

            is NoteEditorUiEvent.TitleChanged -> {
                onTitleChange(event.text)
            }

            NoteEditorUiEvent.ToggleFoldersSheet -> {
                _state.update { it.copy(showFolderPicker = !it.showFolderPicker) }
            }

            NoteEditorUiEvent.ToggleReminderSheet -> {
                _state.update { it.copy(showReminderDialog = !it.showReminderDialog) }
            }

            NoteEditorUiEvent.ToggleTagsSheet -> {
                _state.update { it.copy(showTagPicker = !it.showTagPicker) }

            }

            is NoteEditorUiEvent.FolderChange -> {
                _state.update {
                    it.copy(
                        folderName = event.folder.name,
                        folderId = event.folder.id
                    )
                }
            }

            NoteEditorUiEvent.RemoveFolder -> {
                _state.update { it.copy(folderName = null, folderId = null) }

            }
        }
    }

    private fun loadExistingNote(id: Long) {
        viewModelScope.launch {
            val note = noteRepository.getNoteByIdOnce(id)
            if (note != null) {
                val folderName = note.folderId?.let { id ->
                    folderRepository.getFolderNameById(id)
                }
                _state.update {
                    it.copy(
                        noteId = noteId,
                        title = note.title,
                        content = note.content,
                        color = note.color,
                        isPinned = note.isPinned,
                        isFavorite = note.isFavorite,
                        isArchived = note.isArchived,
                        folderId = note.folderId,
                        folderName = folderName,
                        tags = note.tags,
                        checklist = note.checklist,
                        reminder = note.reminder,
                    )
                }
            }
        }
    }

    fun onTitleChange(title: String) {
        _state.update { it.copy(title = title) }
    }

    fun onContentChange(content: String) {
        _state.update {
            it.copy(
                content = content,
            )
        }
    }

    fun togglePin() {
        val newPinned = !_state.value.isPinned
        _state.update { it.copy(isPinned = newPinned) }
    }

    fun toggleFavorite() {
        val newFav = !_state.value.isFavorite
        _state.update { it.copy(isFavorite = newFav) }
    }

    fun toggleArchive() {
        val newArchived = !_state.value.isArchived
        _state.update { it.copy(isArchived = newArchived) }
    }

    fun addTag(text: String) {
        val current = _state.value.tags
        val newItem = Tag(
            name = text,
        )
        if (!current.contains(newItem))
            _state.update { it.copy(tags = current + newItem) }
    }

    fun removeTag(tag: Tag) {
        val current = _state.value.tags.toMutableList()
        current.remove(tag)
        _state.update { it.copy(tags = current) }
    }

    fun addChecklistItem(text: String = "") {
        val current = _state.value.checklist
        val newItem = ChecklistItem(
            noteId = _state.value.noteId,
            text = text,
            isDone = false,
        )
        _state.update { it.copy(checklist = current + newItem) }
    }

    fun updateChecklistItem(index: Int, text: String, isDone: Boolean) {
        val current = _state.value.checklist.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(text = text, isDone = isDone)
            _state.update { it.copy(checklist = current) }
        }
    }

    fun removeChecklistItem(index: Int) {
        val current = _state.value.checklist.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _state.update { it.copy(checklist = current) }
        }
    }

    fun setReminder(timestamp: Long, interval: RepeatInterval) {
        val reminder = Reminder(
            noteId = _state.value.noteId,
            timestamp = timestamp,
            repeatInterval = interval,
            isActive = true
        )
        _state.update {
            it.copy(
                reminder = reminder,
                showReminderDialog = false
            )
        }
    }

    fun setShowDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = !it.showDeleteDialog) }
    }


    fun deleteNote() {
        viewModelScope.launch {
            if (_state.value.noteId > 0) {
                noteRepository.moveToTrash(_state.value.noteId)
            }
            _uiEvent.send(NoteEditorNavigationEvents.NavigateBack)
        }
    }

    private fun saveNote() {
        val state = _state.value

        val note = Note(
            id = state.noteId,
            title = state.title,
            content = state.content,
            color = state.color,
            isPinned = state.isPinned,
            isFavorite = state.isFavorite,
            isArchived = state.isArchived,
            folderId = state.folderId,
            modifiedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                if (note.title.isNotEmpty() && !(note.content.isEmpty() && state.checklist.isEmpty())) {
                    val savedId = noteRepository.saveNote(
                        note = note,
                        tags = state.tags,
                        checklist = state.checklist,
                        reminder = state.reminder
                    )

                    _state.update {
                        it.copy(noteId = savedId)
                    }


                    if (state.reminder != null) {
                        alarmScheduler.schedule(
                            noteId = savedId,
                            reminderTimeStamp = state.reminder.timestamp,

                            )
                    }
                }

                _uiEvent.send(
                    NoteEditorNavigationEvents.NavigateToNotes
                )

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
