package com.example.notes.feature.notes


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.GridView
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notes.core.designsystem.components.EmptyState
import com.example.notes.core.designsystem.components.NoteCard
import com.example.notes.core.designsystem.components.PulseAnimation
import com.example.notes.core.designsystem.components.StripeNoteCard
import com.example.notes.core.model.ViewMode
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = koinViewModel(),
    onNoteEdit: (Long) -> Unit,
    onCreateNote: () -> Unit,
) {

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                NotesNavigationEvents.NavigateToCreateNote -> onCreateNote()
                is NotesNavigationEvents.NavigateToEditNote -> onNoteEdit(event.noteId)
            }
        }
    }


    val state by viewModel.state.collectAsStateWithLifecycle()

    NotesScreen(
        modifier = modifier,
        onEvent = viewModel::onEvent,
        state = state
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    modifier: Modifier,
    onEvent: (NotesUiEvent) -> Unit,
    state: NotesUiState
) {

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "Notes Brand",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Notes",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onEvent(NotesUiEvent.ToggleViewMode)
                    }) {
                        Icon(
                            imageVector = if (state.viewMode == ViewMode.GRID) {
                                Icons.AutoMirrored.Outlined.ViewList
                            } else {
                                Icons.Outlined.GridView
                            },
                            contentDescription = "Toggle View",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { onEvent(NotesUiEvent.CreateNote) }) {
                        Icon(
                            imageVector = Icons.Filled.AddCircle,
                            contentDescription = "Search notes",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(35.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isLoading) {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    PulseAnimation(circleColor = MaterialTheme.colorScheme.primary, circleSize = 16.dp)
                }
            } else if (state.notes.isEmpty()) {
                EmptyState(
                    title = "No notes yet",
                    subtitle = "Tap + to create your first note",
                    icon = Icons.Filled.AutoAwesome,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                when (state.viewMode) {
                    ViewMode.GRID -> {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = 88.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalItemSpacing = 12.dp,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.notes, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    onClick = {
                                        onEvent(NotesUiEvent.EditNote(note.id))
                                    },
                                    onPinClick = {
                                        onEvent(NotesUiEvent.TogglePinned(note))
                                    }
                                )
                            }
                        }
                    }

                    ViewMode.LIST -> {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = 88.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.notes, key = { it.id }) { note ->
                                StripeNoteCard(
                                    note = note,
                                    onClick = {
                                        onEvent(NotesUiEvent.EditNote(note.id))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
