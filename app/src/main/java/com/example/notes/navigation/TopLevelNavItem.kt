package com.example.notes.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.notes.feature.agent.navigation.AgentNavKey
import com.example.notes.feature.folders.lists.navigation.FoldersNavKey
import com.example.notes.feature.notes.navigation.NotesNavKey
import com.example.notes.feature.search.navigation.SearchNavKey
import com.example.notes.feature.settings.navigation.SettingsNavKey

data class TopLevelNavItem(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val title: String,
)

val NOTES = TopLevelNavItem(
    selectedIcon = Icons.AutoMirrored.Filled.StickyNote2,
    unselectedIcon = Icons.AutoMirrored.Outlined.StickyNote2,
    title = "Notes"
)

val FOLDERS = TopLevelNavItem(
    selectedIcon = Icons.Filled.Folder,
    unselectedIcon = Icons.Outlined.Folder,
    title = "Folders"
)

val SETTINGS = TopLevelNavItem(
    selectedIcon = Icons.Filled.Settings,
    unselectedIcon = Icons.Outlined.Settings,
    title = "Settings"
)

val SEARCH = TopLevelNavItem(
    selectedIcon = Icons.Filled.Search,
    unselectedIcon = Icons.Outlined.Search,
    title = "Search"
)
val AGENT = TopLevelNavItem(
    selectedIcon = Icons.Filled.AutoAwesome,
    unselectedIcon = Icons.Outlined.AutoAwesome,
    title = "Search"
)

val TOP_LEVEL_NAV_ITEMS = mapOf(
    NotesNavKey to NOTES,
    SearchNavKey to SEARCH,
    FoldersNavKey to FOLDERS ,
    SettingsNavKey to SETTINGS,
    AgentNavKey to AGENT
)