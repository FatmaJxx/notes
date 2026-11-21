package com.example.notes.feature.reminders

import com.example.notes.core.model.RepeatInterval
import java.util.Calendar

data class ReminderUiState(
    val selectedDateMillis: Long = Calendar.getInstance().timeInMillis,
    val selectedHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
    val selectedMinute: Int = Calendar.getInstance().get(Calendar.MINUTE),
    val selectedInterval: RepeatInterval = RepeatInterval.ONCE,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false
)

sealed interface ReminderUiEvent {
    data class DateMillisChanged(val dateInMillis: Long) : ReminderUiEvent
    data object ToggleDatePicker: ReminderUiEvent
    data object ToggleTimePicker: ReminderUiEvent
    data class SaveTimeData(val hour: Int, val min: Int): ReminderUiEvent
    data class IntervalsChanged(val interval: RepeatInterval) : ReminderUiEvent
}