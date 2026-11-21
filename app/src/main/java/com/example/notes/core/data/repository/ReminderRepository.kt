package com.example.notes.core.data.repository

import com.example.notes.core.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getActiveReminders(): Flow<List<Reminder>>
    suspend fun setActiveState(id: Long, isActive: Boolean)
    suspend fun updateReminder(reminder: Reminder)
}