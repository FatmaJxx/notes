package com.example.notes.feature.editor

import com.example.notes.core.model.AppColor
import com.example.notes.core.model.ChecklistItem
import com.example.notes.core.model.Folder
import com.example.notes.core.model.Reminder
import com.example.notes.core.model.RepeatInterval
import com.example.notes.core.model.Tag

data class NoteEditorUiState(
    val noteId: Long = 0,
    val title: String = "",
    val content: String = "",
    val color: AppColor = AppColor.LAVENDER,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val folderId: Long? = null,
    val folderName: String? = null,
    val tags: List<Tag> = emptyList(),
    val checklist: List<ChecklistItem> = emptyList(),
    val reminder: Reminder? = null,
    val showColorPicker: Boolean = false,
    val showTagPicker: Boolean = false,
    val showFolderPicker: Boolean = false,
    val showReminderDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
)

sealed interface NoteEditorUiEvent {
    data class FolderChange(val folder: Folder): NoteEditorUiEvent
    data object RemoveFolder: NoteEditorUiEvent

    data object BackPressed : NoteEditorUiEvent
    data object ToggleFavorite : NoteEditorUiEvent
    data object SaveNote : NoteEditorUiEvent
    data object AddCheckListItem : NoteEditorUiEvent
    data object ToggleColorPicker: NoteEditorUiEvent
    data object TogglePin: NoteEditorUiEvent
    data object ToggleReminderSheet : NoteEditorUiEvent
    data object ToggleTagsSheet : NoteEditorUiEvent
    data object ToggleFoldersSheet : NoteEditorUiEvent

    data object ToggleArchive: NoteEditorUiEvent
    data object DeleteNote: NoteEditorUiEvent
    data object ShowDeleteDialog: NoteEditorUiEvent

    data class ChooseNoteColor(val color: AppColor): NoteEditorUiEvent
    data class DeleteCheckListItem(val index: Int): NoteEditorUiEvent
    data class UpdateCheckListItem(val index: Int, val text: String,
        val isDone: Boolean): NoteEditorUiEvent
    data class TitleChanged(val text: String): NoteEditorUiEvent
    data class ContentChanged(val text: String): NoteEditorUiEvent

    data class RemoveTag(val tag: Tag): NoteEditorUiEvent
    data class AddTag(val tagName: String): NoteEditorUiEvent
    data class  SetReminder(val timestamp: Long, val interval: RepeatInterval)
        : NoteEditorUiEvent
}

sealed interface NoteEditorNavigationEvents {
    data object NavigateBack : NoteEditorNavigationEvents
    data object NavigateToNotes : NoteEditorNavigationEvents
}