package com.example.notes

import android.app.Application
import com.example.notes.core.agent.di.agentModule
import com.example.notes.core.data.di.repositoryModule
import com.example.notes.core.database.di.databaseModule
import com.example.notes.core.datastore.di.dataStoreModule
import com.example.notes.core.notification.di.notificationModule
import com.example.notes.feature.agent.di.agentViewModelModule
import com.example.notes.feature.editor.di.noteEditorViewModelModule
import com.example.notes.feature.folders.details.di.folderDetailsViewModelModule
import com.example.notes.feature.folders.lists.di.foldersViewModelModule
import com.example.notes.feature.folders.tags.di.tagsViewModelModule
import com.example.notes.feature.folders.trash.di.trashViewModelModule
import com.example.notes.feature.notes.di.notesViewModelModule
import com.example.notes.feature.reminders.di.reminderModelModule
import com.example.notes.feature.search.di.searchViewModelModule
import com.example.notes.feature.settings.di.settingsViewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class NotesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@NotesApplication)
            modules(
                databaseModule,
                repositoryModule,
                noteEditorViewModelModule,
                notesViewModelModule,
                dataStoreModule,
                foldersViewModelModule,
                folderDetailsViewModelModule,
                trashViewModelModule,
                tagsViewModelModule,
                searchViewModelModule,
                reminderModelModule,
                settingsViewModelModule,
                notificationModule,
                agentModule,
                agentViewModelModule
            )
        }
    }
}