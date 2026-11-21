package com.example.notes.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.notes.core.database.entity.ChecklistItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistDao {
    @Query("SELECT * FROM checklist_items WHERE noteId = :noteId ORDER BY id ASC")
    fun getItemsForNote(noteId: Long): Flow<List<ChecklistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ChecklistItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ChecklistItemEntity): Long

    @Update
    suspend fun updateItem(item: ChecklistItemEntity)

    @Query("UPDATE checklist_items SET isDone = :isDone WHERE id = :id")
    suspend fun setDoneState(id: Long, isDone: Boolean)

    @Query("DELETE FROM checklist_items WHERE id = :id")
    suspend fun deleteItem(id: Long)

    @Query("DELETE FROM checklist_items WHERE noteId = :noteId")
    suspend fun deleteItemsForNote(noteId: Long)
}
