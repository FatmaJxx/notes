package com.example.notes.feature.reminders.di

import com.example.notes.core.model.Reminder
import com.example.notes.feature.reminders.ReminderViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val reminderModelModule= module {
    viewModel { (reminder: Reminder?) ->
        ReminderViewModel(reminder)
    }
}