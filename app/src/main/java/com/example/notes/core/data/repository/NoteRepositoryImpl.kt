package com.example.notes.core.data.repository

import androidx.room.withTransaction
import com.example.notes.core.database.NoteDatabase
import com.example.notes.core.database.dao.ChecklistDao
import com.example.notes.core.database.dao.NoteDao
import com.example.notes.core.database.dao.ReminderDao
import com.example.notes.core.database.dao.TagDao
import com.example.notes.core.database.entity.NoteTagCrossRef
import com.example.notes.core.database.entity.toDomain
import com.example.notes.core.database.entity.toEntity
import com.example.notes.core.model.ChecklistItem
import com.example.notes.core.model.Note
import com.example.notes.core.model.Reminder
import com.example.notes.core.model.SortOrder
import com.example.notes.core.model.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.collections.map

class NoteRepositoryImpl(
    private val database: NoteDatabase,
    private val noteDao: NoteDao,
    private val tagDao: TagDao,
    private val checklistDao: ChecklistDao,
    private val reminderDao: ReminderDao
) : NoteRepository {

    override fun getActiveNotes(sortOrder: SortOrder): Flow<List<Note>> {
        return noteDao.getActiveNotes(sortOrder.name).map { notes ->
            notes.map { it.toDomain() }
        }
    }

    override fun getPinnedNotes(): Flow<List<Note>> {
        return noteDao.getPinnedNotes().map { list -> list.map { it.toDomain() } }
    }

    override fun getFavoriteNotes(): Flow<List<Note>> {
        return noteDao.getFavoriteNotes().map { list -> list.map { it.toDomain() } }
    }

    override fun getArchivedNotes(): Flow<List<Note>> {
        return noteDao.getArchivedNotes().map { list -> list.map { it.toDomain() } }
    }

    override fun getTrashNotes(): Flow<List<Note>> {
        return noteDao.getTrashNotes().map { list -> list.map { it.toDomain() } }
    }

    override fun getNotesByFolderId(folderId: Long): Flow<List<Note>> {
        return noteDao.getNotesByFolderId(folderId).map { list -> list.map { it.toDomain() } }
    }

    override fun getNotesByFolderName(folderName: String): Flow<List<Note>> {
        return noteDao.getNotesByFolderName(folderName).map { list -> list.map { it.toDomain() } }
    }

    override fun getNoteById(id: Long): Flow<Note?> {
        return noteDao.getNoteById(id).map { it?.toDomain() }
    }

    override suspend fun getNoteByIdOnce(id: Long): Note? {
        return noteDao.getNoteByIdOnce(id)?.toDomain()
    }

    override fun searchNotes(
        query: String,
        onlyPinned: Boolean,
        onlyFavorites: Boolean,
        selectedTagIds: List<Long>
    ): Flow<List<Note>> {
        return noteDao.searchNotes(
            query = query,
            onlyPinned = onlyPinned,
            onlyFavorites = onlyFavorites,
            filterByTags = selectedTagIds.isNotEmpty(),
            selectedTagIds = selectedTagIds
        ).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun saveNote(
        note: Note,
        tags: List<Tag>,
        checklist: List<ChecklistItem>,
        reminder: Reminder?
    ): Long = database.withTransaction {

        val existing = if (note.id != 0L) noteDao.getNoteByIdOnce(note.id)?.note else null

        val noteEntity = note.toEntity()
            .copy(
                modifiedAt = System.currentTimeMillis(),
                createdAt = existing?.createdAt ?: note.createdAt
            )

        val insertedId = noteDao.upsertNote(noteEntity)

        val noteId = if (insertedId != -1L) {
            insertedId
        } else {
            note.id
        }

        val tagIds = tags
            .map { it.name.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .map { resolveTagId(it) }
        noteDao.removeAllTagsForNote(noteId)
        noteDao.insertNoteTagCrossRefs(tagIds.map { NoteTagCrossRef(noteId, it) })

        checklistDao.deleteItemsForNote(noteId)
        val items = checklist
            .filter { it.text.isNotBlank() }
            .map { it.toEntity().copy(noteId = noteId) }
        if (items.isNotEmpty()) checklistDao.insertItems(items)

        reminderDao.deleteReminderForNote(noteId)
        reminder?.let {
            reminderDao.insertReminder(it.toEntity().copy(id = 0, noteId = noteId))
        }

        noteId
    }

    /** Finds a tag by name or creates it. Never returns -1. */
    private suspend fun resolveTagId(name: String): Long =
        tagDao.getTagByName(name)?.id
            ?: tagDao.insertTag(Tag(name = name).toEntity()).takeIf { it != -1L }
            ?: checkNotNull(tagDao.getTagByName(name)) { "Tag '$name' could not be created" }.id

    override suspend fun moveToTrash(noteId: Long) {
        noteDao.moveToTrash(noteId, deletedAt = System.currentTimeMillis())
    }

    override suspend fun moveToTrashByTitle(title: String){
        return noteDao.moveToTrashByTitle(title, deletedAt = System.currentTimeMillis())
    }

    override suspend fun restoreFromTrash(noteId: Long) {
        noteDao.restoreFromTrash(noteId)
    }

    override suspend fun permanentlyDelete(noteId: Long) {
        noteDao.permanentlyDelete(noteId)
    }

    override suspend fun emptyTrash() {
        noteDao.emptyTrash()
    }

    override suspend fun setArchived(noteId: Long, isArchived: Boolean) {
        noteDao.setArchived(noteId, isArchived)
    }

    override suspend fun setPinned(noteId: Long, isPinned: Boolean) {
        noteDao.setPinned(noteId, isPinned)
    }

    override suspend fun setFavorite(noteId: Long, isFavorite: Boolean) {
        noteDao.setFavorite(noteId, isFavorite)
    }

    override suspend fun setChecklist(
        noteId: Long,
        checklist: List<ChecklistItem>
    ) {
        checklistDao.deleteItemsForNote(noteId)
        val items = checklist
            .filter { it.text.isNotBlank() }
            .map { it.toEntity().copy(noteId = noteId) }
        if (items.isNotEmpty()) checklistDao.insertItems(items)
    }
}
