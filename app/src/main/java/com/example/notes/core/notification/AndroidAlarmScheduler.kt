package com.example.notes.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent

class AndroidAlarmScheduler(
    private val context: Context
) : AlarmScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(noteId: Long, reminderTimeStamp: Long) {


        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_NOTE_ID", noteId)
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminderTimeStamp,
            PendingIntent.getBroadcast(
                context,
                noteId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
            )
        )
    }

    override fun cancel(noteId: Long) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                noteId.hashCode(),
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or FLAG_IMMUTABLE
            )
        )
    }
}