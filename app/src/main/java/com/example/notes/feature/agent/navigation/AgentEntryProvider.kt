package com.example.notes.feature.agent.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.notes.core.navigation.Navigator
import com.example.notes.feature.agent.AgentScreen
import com.example.notes.feature.editor.navigation.navigateToNoteEditor
import kotlinx.serialization.Serializable

@Serializable
data object AgentNavKey : NavKey

fun EntryProviderScope<NavKey>.agentEntry(navigator: Navigator) {
    entry<AgentNavKey> {
        AgentScreen(
            navigatorToEditor = navigator::navigateToNoteEditor
        )
    }
}