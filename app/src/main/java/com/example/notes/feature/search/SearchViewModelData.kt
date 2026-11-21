package com.example.notes.feature.search

import com.example.notes.core.model.Note
import com.example.notes.core.model.Tag


data class SearchFilterState(
    val onlyPinned: Boolean = false,
    val onlyFavorites: Boolean = false,
    val selectedTagIds: List<Long> = emptyList(),
)

data class SearchUiState(
    val query: String = "",
    val filters: SearchFilterState = SearchFilterState(),
    val filteredNotes: List<Note> = emptyList(),
    val availableTags: List<Tag> = emptyList(),
    val showFilterSheet: Boolean = false,
    val isSearching: Boolean = false
)

sealed interface SearchUiEvent{
    data class QueryChanged(val query: String): SearchUiEvent
    data object TogglePinnedFilter: SearchUiEvent
    data object ToggleFavoriteFilter: SearchUiEvent
    data object ShowFilterSheet: SearchUiEvent
    data object DismissFilterSheet: SearchUiEvent
    data class EditNote(val noteId: Long): SearchUiEvent
    data class ToggleTagFilter(val tagId: Long): SearchUiEvent
    data object ClearTagFilter: SearchUiEvent
}

sealed interface SearchNavigationEvents {
    data class NavigateToNoteEditor(val noteId: Long) : SearchNavigationEvents
}