package com.example.notes.ui

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.notes.core.designsystem.components.NotesNavigationSuiteScaffold
import com.example.notes.core.navigation.Navigator
import com.example.notes.core.navigation.rememberNavigationState
import com.example.notes.core.navigation.toEntries
import com.example.notes.feature.agent.navigation.agentEntry
import com.example.notes.feature.editor.navigation.NoteEditorNavKey
import com.example.notes.feature.editor.navigation.noteEditorEntry
import com.example.notes.feature.folders.details.navigation.folderDetailsEntry
import com.example.notes.feature.folders.lists.navigation.foldersEntry
import com.example.notes.feature.folders.tags.navigation.tagsEntry
import com.example.notes.feature.folders.trash.navigation.trashEntry
import com.example.notes.feature.notes.navigation.NotesNavKey
import com.example.notes.feature.notes.navigation.notesEntry
import com.example.notes.feature.search.navigation.searchEntry
import com.example.notes.feature.settings.navigation.appearanceEntry
import com.example.notes.feature.settings.navigation.settingsEntry
import com.example.notes.navigation.TOP_LEVEL_NAV_ITEMS

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotesApp(
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfoV2(),
    noteId: Long?
) {

    val navigationState = rememberNavigationState(
        startKey = NotesNavKey,
        topLevelKeys = TOP_LEVEL_NAV_ITEMS.keys,
    )

    val navigator = remember(navigationState) { Navigator(navigationState) }

    LaunchedEffect(noteId) {
        if (noteId != null) {
            navigator.navigate(NoteEditorNavKey(noteId))
        }
    }
    val entryProvider = remember(navigator) {
        entryProvider {
            noteEditorEntry(navigator)
            notesEntry(navigator)
            foldersEntry(navigator)
            folderDetailsEntry(navigator)
            trashEntry(navigator)
            tagsEntry(navigator)
            searchEntry(navigator)
            settingsEntry(navigator)
            appearanceEntry(navigator)
            agentEntry(navigator)
        }
    }



    val imeVisible = WindowInsets.isImeVisible
    val showNavigationSuite = navigationState.currentKey in navigationState.topLevelKeys && !imeVisible

    NotesNavigationSuiteScaffold(
        navigationSuiteItems = {
            TOP_LEVEL_NAV_ITEMS.forEach { (key, item) ->
                val selected = key == navigationState.currentTopLevelKey
                item(
                    selected = selected,
                    onClick = { navigator.navigate(key) },
                    icon = {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title
                        )
                    },
                )
            }
        },
        layoutType = if (showNavigationSuite) {
            NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(windowAdaptiveInfo)
        } else {
            NavigationSuiteType.None
        }
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
        ) { innerPadding ->
            NavDisplay(
                entries = navigationState.toEntries(entryProvider),
                onBack = { navigator.goBack() },
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )
        }
    }
}