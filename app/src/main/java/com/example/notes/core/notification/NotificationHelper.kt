package com.example.notes.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.notes.MainActivity
import com.example.notes.R
import kotlin.random.Random
import androidx.core.net.toUri

class ReminderNotificationService(
    private val context: Context
) {
    companion object {
        private const val CHANNEL_ID = "reminders_channel_id"
        private const val CHANNEL_NAME = "Event Reminders"
    }

    fun showNotification(noteId: Long, noteTitle: String, noteBody: String) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for event and task reminders"
        }
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "mynotes://noteEditor/$noteId".toUri()
            Log.d("HelloFromNote", noteId.toString())

            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            noteId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Note Reminder")
            .setContentText(noteTitle)
            .setStyle(
                NotificationCompat.BigTextStyle().setBigContentTitle("Note Reminder")
                    .bigText("$noteTitle\n$noteBody")
            )
            .setSmallIcon(R.drawable.ic_star_filled)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(Random.nextInt(), notification)
    }
}