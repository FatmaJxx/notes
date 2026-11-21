package com.example.notes.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notes.core.designsystem.components.Chip
import com.example.notes.core.designsystem.components.EmptyState
import com.example.notes.core.designsystem.components.PulseAnimation
import com.example.notes.core.designsystem.components.StripeNoteCard
import com.example.notes.core.designsystem.theme.notes.PillShape
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = koinViewModel(),
    onNoteClick: (Long) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SearchNavigationEvents.NavigateToNoteEditor -> onNoteClick(event.noteId)
            }
        }
    }

    SearchScreen(
        modifier = modifier,
        state = state,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    modifier: Modifier,
    state: SearchUiState,
    onEvent: (SearchUiEvent) -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Surface(
                shape = PillShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = state.query,
                        onValueChange = { onEvent(SearchUiEvent.QueryChanged(it)) },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            if (state.query.isEmpty()) {
                                Text(
                                    text = "Search notes",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.5f
                                        ),
                                        fontSize = 16.sp
                                    )
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    )

                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { onEvent(SearchUiEvent.QueryChanged("")) }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Chip(
                    text = "Pinned",
                    isSelected = state.filters.onlyPinned,
                    onClick = { onEvent(SearchUiEvent.TogglePinnedFilter) }
                )
                Chip(
                    text = "Favorites",
                    isSelected = state.filters.onlyFavorites,
                    onClick = { onEvent(SearchUiEvent.ToggleFavoriteFilter) }
                )
                Chip(
                    text = if (state.filters.selectedTagIds.isEmpty()) "Tags"
                    else "Tags (${state.filters.selectedTagIds.size})",
                    isSelected = state.filters.selectedTagIds.isNotEmpty(),
                    onClick = { onEvent(SearchUiEvent.ShowFilterSheet) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${state.filteredNotes.size} RESULTS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )

            Spacer(modifier = Modifier.height(12.dp))
            if (state.isSearching) {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    PulseAnimation(
                        circleColor = MaterialTheme.colorScheme.primary, circleSize = 16.dp
                    )
                }
            } else if (state.filteredNotes.isEmpty()) {
                EmptyState(
                    title = "No results found",
                    subtitle = "Try a different word or clear your filters",
                    icon = Icons.Outlined.Edit,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.filteredNotes, key = { it.id }) { note ->
                        StripeNoteCard(
                            note = note,
                            onClick = { onEvent(SearchUiEvent.EditNote(note.id)) }
                        )
                    }
                }
            }
        }
    }
    if (state.showFilterSheet) {
        FilterBottomSheet(
            filterState = state.filters,
            availableTags = state.availableTags,
            matchingCount = state.filteredNotes.size,
            onTogglePinned = { onEvent(SearchUiEvent.TogglePinnedFilter) },
            onToggleFavorite = { onEvent(SearchUiEvent.ToggleFavoriteFilter) },
            onToggleTag = { onEvent(SearchUiEvent.ToggleTagFilter(it)) },
            onClearAll = { onEvent(SearchUiEvent.ClearTagFilter) },
            onDismiss = { onEvent(SearchUiEvent.DismissFilterSheet) }
        )
    }
}



