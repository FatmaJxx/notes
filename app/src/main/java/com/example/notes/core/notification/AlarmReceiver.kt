package com.example.notes.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.notes.core.data.repository.NoteRepository
import com.example.notes.core.data.repository.ReminderRepository
import com.example.notes.core.model.RepeatInterval
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import kotlinx.coroutines.flow.first
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "AlarmReciever"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val noteId = intent?.getLongExtra("EXTRA_NOTE_ID", 0L)

        val pendingResult = goAsync()
        val notificationService = GlobalContext.get().get<ReminderNotificationService>()
        val notesRepository = GlobalContext.get().get<NoteRepository>()
        val remindersRepository = GlobalContext.get().get<ReminderRepository>()
        val alarmScheduler = GlobalContext.get().get<AlarmScheduler>()


        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (noteId != null) {
                    val note = notesRepository.getNoteById(noteId).first() ?: return@launch
                    var reminder = note.reminder ?: return@launch

                    if (!reminder.isActive) return@launch

                    notificationService.showNotification(
                        noteId = noteId,
                        noteTitle = note.title,
                        noteBody = note.content
                    )

                    val reminderId = reminder.id
                    val reminderRepeatInterval = reminder.repeatInterval
                    val reminderTimestamp = reminder.timestamp

                    if (reminderRepeatInterval != RepeatInterval.ONCE) {
                        val nextReminder =
                            computeNextOccurrence(reminderTimestamp, reminderRepeatInterval)

                        reminder = reminder.copy(timestamp = nextReminder)
                        remindersRepository.updateReminder(reminder)

                        alarmScheduler.cancel(noteId)

                        alarmScheduler.schedule(noteId, nextReminder)

                    } else {
                        remindersRepository.setActiveState(reminderId, false)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reschedule alarms after reboot", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}

fun computeNextOccurrence(timeStamp: Long, repeatInterval: RepeatInterval): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timeStamp
    }
    val now = System.currentTimeMillis()

    while (calendar.timeInMillis <= now) {
        when (repeatInterval) {
            RepeatInterval.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            RepeatInterval.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            RepeatInterval.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            else -> return timeStamp
        }
    }

    return calendar.timeInMillis
}