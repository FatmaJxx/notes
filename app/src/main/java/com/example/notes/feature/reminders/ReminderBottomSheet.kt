package com.example.notes.feature.reminders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notes.core.designsystem.components.Chip
import com.example.notes.core.designsystem.theme.notes.BottomSheetShape
import com.example.notes.core.designsystem.theme.notes.PillShape
import com.example.notes.core.model.Reminder
import com.example.notes.core.model.RepeatInterval
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderBottomSheet(
    modifier: Modifier = Modifier,
    reminder: Reminder? = null,
    viewModel: ReminderViewModel = koinViewModel {
        parametersOf(reminder)
    },
    onDismiss: () -> Unit,
    onSave: (timestamp: Long, interval: RepeatInterval) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.selectedDateMillis
    )
    val timePickerState = rememberTimePickerState(
        initialHour = state.selectedHour,
        initialMinute = state.selectedMinute,
        is24Hour = false
    )

    ReminderBottomSheet(
        modifier = modifier,
        state = state,
        onEvent = viewModel::onEvent,
        sheetState = sheetState,
        datePickerState = datePickerState,
        timePickerState = timePickerState,
        onDismiss = onDismiss,
        onSave = onSave
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderBottomSheet(
    modifier: Modifier,
    state: ReminderUiState,
    onEvent: (ReminderUiEvent) -> Unit,
    sheetState: SheetState,
    datePickerState: DatePickerState,
    timePickerState: TimePickerState,
    onDismiss: () -> Unit,
    onSave: (timestamp: Long, interval: RepeatInterval) -> Unit,
) {

    if (state.showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { onEvent(ReminderUiEvent.ToggleDatePicker) },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onEvent(ReminderUiEvent.DateMillisChanged(it))
                        }
                        onEvent(ReminderUiEvent.ToggleDatePicker)
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(ReminderUiEvent.ToggleDatePicker) }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (state.showTimePicker) {
        TimePickerDialog(
            onDismiss = { onEvent(ReminderUiEvent.ToggleTimePicker) },
            onConfirm = {
                onEvent(ReminderUiEvent.SaveTimeData(timePickerState.hour, timePickerState.minute))
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = BottomSheetShape,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            val formattedDate = remember(state.selectedDateMillis) {
                SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(
                    Date(
                        state.selectedDateMillis
                    )
                )
            }

            Surface(
                shape = PillShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEvent(ReminderUiEvent.ToggleDatePicker) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Date",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val formattedTime = remember(state.selectedHour, state.selectedMinute) {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, state.selectedHour)
                    set(Calendar.MINUTE, state.selectedMinute)
                }
                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
            }

            Surface(
                shape = PillShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEvent(ReminderUiEvent.ToggleTimePicker) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Time",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "REPEAT",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RepeatInterval.entries.forEach { interval ->
                    Chip(
                        text = interval.label,
                        isSelected = state.selectedInterval == interval,
                        onClick = { onEvent(ReminderUiEvent.IntervalsChanged(interval)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text(text = "Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = {
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = state.selectedDateMillis
                            set(Calendar.HOUR_OF_DAY, state.selectedHour)
                            set(Calendar.MINUTE, state.selectedMinute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        onSave(cal.timeInMillis, state.selectedInterval)
                    },
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(text = "Save reminder", color = Color.White)
                }
            }
        }
    }
}

/**
 * Reusable helper wrapper for Material 3 TimePicker Dialog layout
 */
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    TextButton(onClick = onConfirm) {
                        Text("OK")
                    }
                }
            }
        }
    }
}