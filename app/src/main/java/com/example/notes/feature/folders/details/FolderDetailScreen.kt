package com.example.notes.feature.folders.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notes.core.designsystem.components.EmptyState
import com.example.notes.core.designsystem.components.Fab
import com.example.notes.core.designsystem.components.NoteCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDetailScreen(
    viewModel: FolderDetailsViewModel,
    onNoteClick: (Long) -> Unit,
    onCreateNote: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                FolderDetailsNavigationEvents.NavigateBack -> onBack()
                FolderDetailsNavigationEvents.NavigateToCreateNote -> onCreateNote()
                is FolderDetailsNavigationEvents.NavigateToEditNote -> onNoteClick(event.noteId)
            }
        }
    }

    val state by viewModel
        .state.collectAsStateWithLifecycle()

    FolderDetailScreen(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDetailScreen(
    state: FolderDetailsUiState,
    onEvent: (FolderDetailsUiEvent) -> Unit,
    modifier: Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.folderName,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onEvent(FolderDetailsUiEvent.BackPressed)
                    }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            Fab(onClick = {
                onEvent(FolderDetailsUiEvent.CreateNote)
            })
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (state.notes.isEmpty()) {
            EmptyState(
                title = "No notes in ${state.folderName}",
                subtitle = "Tap + to create a note in this folder",
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(state.notes, key = { it.id }) { note ->
                    NoteCard(
                        note = note,
                        onClick = { onEvent(FolderDetailsUiEvent.EditNote(note.id)) }
                    )
                }
            }
        }
    }
}
