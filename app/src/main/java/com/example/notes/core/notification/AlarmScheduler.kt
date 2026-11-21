package com.example.notes.core.notification

interface AlarmScheduler {
    fun schedule(noteId: Long, reminderTimeStamp: Long)
    fun cancel(noteId: Long)
}