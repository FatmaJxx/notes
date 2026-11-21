package com.example.notes.core.data.repository

import com.example.notes.core.database.dao.ReminderDao
import com.example.notes.core.database.entity.toDomain
import com.example.notes.core.database.entity.toEntity
import com.example.notes.core.model.Reminder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReminderRepositoryImpl(
    private val reminderDao: ReminderDao
) : ReminderRepository {
    override fun getActiveReminders(): Flow<List<Reminder>> {
        return reminderDao.getActiveReminders().map {reminders ->  reminders.map { it.toDomain() } }
    }

    override suspend fun setActiveState(id: Long, isActive: Boolean) {
        reminderDao.setActiveState(id, isActive)
    }

    override suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder.toEntity())
    }
}