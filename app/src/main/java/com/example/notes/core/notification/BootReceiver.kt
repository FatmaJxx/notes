package com.example.notes.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.notes.core.data.repository.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device rebooted. Rescheduling all pending alarms...")

            val pendingResult = goAsync()
            val reminderRepository = GlobalContext.get().get<ReminderRepository>()
            val alarmScheduler = GlobalContext.get().get<AlarmScheduler>()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val reminders =
                        reminderRepository.getActiveReminders().first()
                    Log.d(TAG, "Found ${reminders.size} pending notifications to reschedule.")

                    for (reminder in reminders) {
                        alarmScheduler.schedule(
                            noteId = reminder.noteId,
                            reminderTimeStamp = reminder.timestamp
                        )
                    }

                    Log.d(TAG, "All pending alarms rescheduled successfully after reboot.")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to reschedule alarms after reboot", e)
                } finally {
                    pendingResult.finish()
                }
            }

        }
    }
}