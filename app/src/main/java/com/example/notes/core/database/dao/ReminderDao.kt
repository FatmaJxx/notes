package com.example.notes.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.notes.core.database.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE noteId = :noteId")
    fun getReminderForNote(noteId: Long): Flow<ReminderEntity?>

    @Query("SELECT * FROM reminders WHERE isActive = 1")
    fun getActiveReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Query("DELETE FROM reminders WHERE noteId = :noteId")
    suspend fun deleteReminderForNote(noteId: Long)

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("UPDATE reminders SET isActive = :isActive WHERE id = :id")
    suspend fun setActiveState(id: Long, isActive: Boolean)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)
}