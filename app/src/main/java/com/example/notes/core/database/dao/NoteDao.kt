package com.example.notes.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.example.notes.core.database.entity.NoteEntity
import com.example.notes.core.database.entity.NoteTagCrossRef
import com.example.notes.core.database.entity.NoteWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Transaction
    @Query(
        """
    SELECT * FROM notes
    WHERE deletedAt IS NULL
      AND isArchived = 0
    ORDER BY
        isPinned DESC,
        CASE WHEN :sortOrder = 'MODIFIED_DESC' THEN modifiedAt END DESC,
        CASE WHEN :sortOrder = 'CREATED_DESC' THEN createdAt END DESC,
        CASE WHEN :sortOrder = 'TITLE_ASC' THEN title END ASC
"""
    )
    fun getActiveNotes(sortOrder: String): Flow<List<NoteWithDetails>>

    @Transaction
    @Query("SELECT * FROM notes WHERE deletedAt IS NULL AND isArchived = 0 AND isPinned = 1 ORDER BY modifiedAt DESC")
    fun getPinnedNotes(): Flow<List<NoteWithDetails>>

    @Transaction
    @Query("SELECT * FROM notes WHERE deletedAt IS NULL AND isArchived = 0 AND isFavorite = 1 ORDER BY modifiedAt DESC")
    fun getFavoriteNotes(): Flow<List<NoteWithDetails>>

    @Transaction
    @Query("SELECT * FROM notes WHERE deletedAt IS NULL AND isArchived = 1 ORDER BY modifiedAt DESC")
    fun getArchivedNotes(): Flow<List<NoteWithDetails>>

    @Transaction
    @Query("SELECT * FROM notes WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun getTrashNotes(): Flow<List<NoteWithDetails>>

    @Transaction
    @Query("SELECT * FROM notes WHERE deletedAt IS NULL AND isArchived = 0 AND folderId = :folderId ORDER BY isPinned DESC, modifiedAt DESC")
    fun getNotesByFolderId(folderId: Long): Flow<List<NoteWithDetails>>

    @Transaction
    @Query("SELECT * FROM notes WHERE deletedAt IS NULL AND isArchived = 0 AND folderName = :folderName ORDER BY isPinned DESC, modifiedAt DESC")
    fun getNotesByFolderName(folderName: String): Flow<List<NoteWithDetails>>

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: Long): Flow<NoteWithDetails?>

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteByIdOnce(id: Long): NoteWithDetails?

    @Transaction
    @Query(
        """
        SELECT DISTINCT n.* 
        FROM notes n
        WHERE n.deletedAt IS NULL 
             AND n.isArchived = 0
        -- Search title, content, or tag name
        And (
           :query = ''
           OR n.title LIKE '%' || :query || '%'
           OR n.content LIKE '%' || :query || '%'
           OR EXISTS (
              SELECT 1
              FROM note_tag_cross_ref as searchRef
              JOIN tags as searchTag
                   ON searchTag.id = searchRef.tagId
              WHERE searchRef.noteId = n.id
                    AND searchTag.name LIKE '%' || :query || '%'
              
           )
         )
         
         -- Apply the pinned filter only when enabled.
         AND (
            :onlyPinned = 0 OR n.isPinned
         )
         
         -- Apply the favorites filter only when enabled.
         AND (
            :onlyFavorites = 0 OR n.isFavorite
         )
         
         -- Match any of the selected tags.
         AND (
            :filterByTags = 0
            OR EXISTS (
                SELECT 1
                FROM note_tag_cross_ref as filterRef
                WHERE filterRef.noteId = n.id
                     AND filterRef.tagId in (:selectedTagIds)
            )
         )
        
      
        ORDER BY n.modifiedAt DESC
    """
    )
    fun searchNotes(
        query: String,
        onlyPinned: Boolean,
        onlyFavorites: Boolean,
        filterByTags: Boolean,
        selectedTagIds: List<Long>
    ): Flow<List<NoteWithDetails>>

    @Upsert
    suspend fun upsertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("UPDATE notes SET deletedAt = :deletedAt WHERE id = :noteId")
    suspend fun moveToTrash(noteId: Long, deletedAt: Long)

    @Query("UPDATE notes SET deletedAt = NULL WHERE id = :noteId")
    suspend fun restoreFromTrash(noteId: Long)

    @Query("UPDATE notes SET isArchived = :isArchived WHERE id = :noteId")
    suspend fun setArchived(noteId: Long, isArchived: Boolean)

    @Query("UPDATE notes SET isPinned = :isPinned WHERE id = :noteId")
    suspend fun setPinned(noteId: Long, isPinned: Boolean)

    @Query("UPDATE notes SET isFavorite = :isFavorite WHERE id = :noteId")
    suspend fun setFavorite(noteId: Long, isFavorite: Boolean)

    @Query("UPDATE notes SET deletedAt = :deletedAt WHERE LOWER(title) LIKE '%' || LOWER(:title) || '%'")
    suspend fun moveToTrashByTitle(title: String, deletedAt: Long)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun permanentlyDelete(noteId: Long)

    @Query("DELETE FROM notes WHERE deletedAt IS NOT NULL")
    suspend fun emptyTrash()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteTagCrossRef(crossRef: NoteTagCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteTagCrossRefs(crossRefs: List<NoteTagCrossRef>)

    @Query("DELETE FROM note_tag_cross_ref WHERE noteId = :noteId")
    suspend fun removeAllTagsForNote(noteId: Long)

    @Query("DELETE FROM note_tag_cross_ref WHERE noteId = :noteId AND tagId = :tagId")
    suspend fun removeTagFromNote(noteId: Long, tagId: Long)
}






