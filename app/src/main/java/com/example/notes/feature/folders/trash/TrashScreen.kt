package com.example.notes.feature.folders.trash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notes.core.designsystem.components.EmptyState
import com.example.notes.core.designsystem.components.NoteCard
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import com.example.notes.R
import com.example.notes.core.designsystem.components.ConfirmDialog
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    modifier: Modifier = Modifier,
    viewModel: TrashViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                TrashNavigationEvents.NavigateBack -> onBack()
            }
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    TrashScreen(
        modifier = modifier,
        state = state,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    modifier: Modifier,
    state: TrashUiState,
    onEvent: (TrashUiEvent) -> Unit
) {


    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.feature_trash_title),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(TrashUiEvent.BackPressed) }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.trashNotes.isNotEmpty()) {
                        TextButton(onClick = { onEvent(TrashUiEvent.ShowEmptyTrashDialog) }) {
                            Text(stringResource(R.string.feature_trash_btn_action_empty_trash), color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFBE8EE),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = Color(0xFFE07A9E),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Items in trash are deleted after 30 days",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF38232E)
                    )
                }
            }

            if (state.trashNotes.isEmpty()) {
                EmptyState(
                    title = "Trash is empty",
                    subtitle = "Notes you delete will show up here",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.trashNotes, key = { it.id }) { note ->
                        Column {
                            NoteCard(
                                note = note,
                                onClick = {}
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                TextButton(
                                    onClick = {
                                        onEvent(TrashUiEvent.RestoreNote(note.id))
                                    }
                                ) {
                                    Text(
                                        text = "Restore",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        onEvent(TrashUiEvent.ShowDeleteNoteDialog(note.id))
                                    }
                                ) {
                                    Text(
                                        text = "Delete permanently",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete single permanently dialog
    ConfirmDialog(
        show = state.noteIdToBeDeletedPermanently != null,
        title = "Delete forever?",
        message = "This note will be deleted permanently and cannot be recovered.",
        confirmText = "Delete forever",
        onConfirm = {
            state.noteIdToBeDeletedPermanently?.let { noteId ->
                onEvent(TrashUiEvent.ConfirmDeleteNote(noteId))
            }
        },
        onDismiss = {
            onEvent(TrashUiEvent.DismissDeleteNoteDialog)
        }
    )

    // Empty entire trash dialog
    ConfirmDialog(
        show = state.showEmptyTrashDialog,
        title = "Empty trash?",
        message = "All notes in the trash will be permanently deleted.",
        confirmText = "Empty trash",
        onConfirm = {
            onEvent(TrashUiEvent.ConfirmEmptyTrash)
        },
        onDismiss = { onEvent(TrashUiEvent.DismissEmptyTrashDialog) }
    )
}
