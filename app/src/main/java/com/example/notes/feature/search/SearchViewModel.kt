package com.example.notes.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.core.data.repository.NoteRepository
import com.example.notes.core.data.repository.TagRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class SearchViewModel(
    private val notesRepository: NoteRepository,
    tagsRepository: TagRepository
) : ViewModel() {

    private val _uiEvent = Channel<SearchNavigationEvents>()
    val uiEvent = _uiEvent.receiveAsFlow()
    private val _state = MutableStateFlow(SearchUiState())

    /**
     * In this code, we utilize the combine() function to merge emissions from both flows. Unlike zip(), combine() produces a new value every time either of the flows emits a value. It combines the latest values emitted by each flow. The combined values are printed as pairs.
     *
     * Let’s take a look at the instances where we’d use this the combine() method to combine our Flows:
     *
     * We use combine() when we want to merge two flows and emit a new value every time either of the flows emits a value.
     * It combines the latest values emitted by each flow
     * Suitable for scenarios where we need to react to changes in any of the flows independently
     * */

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<SearchUiState> = combine(
        _state.map { it.query to it.filters }
            .distinctUntilChanged()
            .flatMapLatest { (query, filters) ->
                notesRepository.searchNotes(
                    query = query,
                    onlyPinned = filters.onlyPinned,
                    onlyFavorites = filters.onlyFavorites,
                    selectedTagIds = filters.selectedTagIds
                )
            }, _state,
        tagsRepository.getAllTags()
    ) { notes, currState, tags ->
        currState.copy(
            filteredNotes = notes,
            availableTags = tags,
            isSearching = !currState.query.isNotBlank() && currState.query.isNotEmpty()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState(isSearching = true)
    )

    fun onEvent(event: SearchUiEvent){
        when(event){
            SearchUiEvent.ClearTagFilter -> {
                clearAllFilters()
            }
            SearchUiEvent.DismissFilterSheet -> {
                setShowFilterSheet(false)
            }
            is SearchUiEvent.EditNote -> {
                _uiEvent.trySend(SearchNavigationEvents.NavigateToNoteEditor(event.noteId))
            }
            is SearchUiEvent.QueryChanged -> {
                onQueryChange(event.query)
            }
            SearchUiEvent.ShowFilterSheet -> {
                setShowFilterSheet(true)
            }
            SearchUiEvent.ToggleFavoriteFilter -> {
                toggleFavoriteFilter()
            }
            SearchUiEvent.TogglePinnedFilter -> {
                togglePinnedFilter()
            }
            is SearchUiEvent.ToggleTagFilter -> {
                toggleTagFilter(event.tagId)
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _state.update {
            it.copy(
                query = newQuery
            )
        }
    }

    fun togglePinnedFilter() {
        _state.update {
            it.copy(
                filters = it.filters.copy(
                    onlyPinned = !it.filters.onlyPinned
                )
            )
        }
    }

    fun toggleFavoriteFilter() {
        _state.update { curr ->
            curr.copy(
                filters = curr.filters.copy(onlyFavorites = !curr.filters.onlyFavorites)
            )
        }
    }

    fun toggleTagFilter(tagId: Long) {
        _state.update {
            val updatedTagsId = if (tagId in it.filters.selectedTagIds) {
                it.filters.selectedTagIds - tagId
            } else {
                it.filters.selectedTagIds + tagId
            }
            it.copy(
                filters = it.filters.copy(selectedTagIds = updatedTagsId)
            )
        }
    }

    fun clearAllFilters() {
        _state.update { it.copy(filters = SearchFilterState()) }
    }

    fun setShowFilterSheet(show: Boolean) {
        _state.update { it.copy(showFilterSheet = show) }
    }
}
