package com.example.notes.core.data.repository

import com.example.notes.core.model.ChecklistItem
import com.example.notes.core.model.Note
import com.example.notes.core.model.Reminder
import com.example.notes.core.model.SortOrder
import com.example.notes.core.model.Tag
import kotlinx.coroutines.flow.Flow

interface NoteRepository {

    fun getActiveNotes(sortOrder: SortOrder = SortOrder.MODIFIED_DESC): Flow<List<Note>>
    fun getPinnedNotes(): Flow<List<Note>>
    fun getFavoriteNotes(): Flow<List<Note>>
    fun getArchivedNotes(): Flow<List<Note>>
    fun getTrashNotes(): Flow<List<Note>>
    fun getNotesByFolderId(folderId: Long): Flow<List<Note>>
    fun getNotesByFolderName(folderName: String): Flow<List<Note>>

    fun getNoteById(id: Long): Flow<Note?>
    suspend fun getNoteByIdOnce(id: Long): Note?
    fun searchNotes(query: String,
                    onlyPinned: Boolean = false,
                    onlyFavorites: Boolean = false,
                    selectedTagIds: List<Long> = emptyList()): Flow<List<Note>>
    suspend fun saveNote(
        note: Note,
        tags: List<Tag> = emptyList(),
        checklist: List<ChecklistItem> = emptyList(),
        reminder: Reminder? = null
    ): Long
    suspend fun moveToTrash(noteId: Long)
    suspend fun moveToTrashByTitle(title: String)
    suspend fun restoreFromTrash(noteId: Long)
    suspend fun permanentlyDelete(noteId: Long)
    suspend fun emptyTrash()
    suspend fun setArchived(noteId: Long, isArchived: Boolean)
    suspend fun setPinned(noteId: Long, isPinned: Boolean)
    suspend fun setFavorite(noteId: Long, isFavorite: Boolean)
    suspend fun setChecklist(noteId: Long, checklist: List<ChecklistItem>)
}