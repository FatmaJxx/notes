package com.example.notes.core.notification.di

import com.example.notes.core.notification.AlarmScheduler
import com.example.notes.core.notification.AndroidAlarmScheduler
import com.example.notes.core.notification.ReminderNotificationService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val notificationModule = module {
    single<AlarmScheduler> {
        AndroidAlarmScheduler(androidContext())
    }

    single {
        ReminderNotificationService(androidContext())
    }
}