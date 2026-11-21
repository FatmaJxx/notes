package com.example.notes.feature.reminders

import androidx.lifecycle.ViewModel
import com.example.notes.core.model.Reminder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar

class ReminderViewModel(
    val reminder: Reminder?,
) : ViewModel() {
    private val _state = MutableStateFlow(ReminderUiState())
    val state = _state.asStateFlow()

    init {
        if(reminder != null){
            val calendar = Calendar.getInstance().apply {
                timeInMillis = reminder.timestamp
            }
            _state.update {
                it.copy(
                    selectedDateMillis = reminder.timestamp,
                    selectedHour = calendar.get(Calendar.HOUR_OF_DAY),
                    selectedMinute = calendar.get(Calendar.MINUTE),
                    selectedInterval = reminder.repeatInterval
                )
            }
        }
    }
    fun onEvent(event: ReminderUiEvent) {
        when (event) {
            is ReminderUiEvent.DateMillisChanged -> {
                _state.update { it.copy(selectedDateMillis = event.dateInMillis) }
            }

            is ReminderUiEvent.SaveTimeData -> {
                _state.update {
                    it.copy(
                        selectedHour = event.hour,
                        selectedMinute = event.min,
                        showTimePicker = false
                    )
                }
            }

            ReminderUiEvent.ToggleDatePicker -> {
                _state.update { it.copy(showDatePicker = !it.showDatePicker) }
            }

            ReminderUiEvent.ToggleTimePicker -> {
                _state.update { it.copy(showTimePicker = !it.showTimePicker) }
            }

            is ReminderUiEvent.IntervalsChanged -> {
                _state.update { it.copy(selectedInterval = event.interval) }
            }
        }
    }
}