package com.example.notes.feature.folders.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.core.data.repository.TagRepository
import com.example.notes.core.model.Tag
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TagsViewModel(
    private val tagsRepository: TagRepository
) : ViewModel() {

    private val _uiEvent = Channel<TagsNavigationEvents>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _state = MutableStateFlow(TagsUiState())
    val state: StateFlow<TagsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            tagsRepository.getAllTags().collect{tags ->
                _state.update { it.copy(tags = tags) }
            }
        }
    }

    fun onEvent(event: TagsUiEvent){
        when(event){
            TagsUiEvent.BackPressed -> {
                _uiEvent.trySend(TagsNavigationEvents.NavigateBack)
            }
            TagsUiEvent.CreateTag -> createTag()
            is TagsUiEvent.DeleteTag -> deleteTag(event.tag)
            TagsUiEvent.DismissCreateDialog -> {
                _state.update { it.copy(showCreateDialog = true) }
            }
            is TagsUiEvent.NewTagNameChanged -> {
                _state.update { it.copy(newTagName = event.name) }
            }
            is TagsUiEvent.SearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
            }
            TagsUiEvent.ShowCreateDialog -> {
                _state.update { it.copy(showCreateDialog = true) }
            }
        }
    }

    private fun deleteTag(tag: Tag){
        viewModelScope.launch {
            tagsRepository.deleteTag(tag)
        }
    }

    private fun createTag(){
        val name = state.value.newTagName.trim()
        if (name.isBlank()) return

        viewModelScope.launch {
            tagsRepository.createTag(Tag(name = name))
            _state.update {
                it.copy(showCreateDialog = false, newTagName = "")
            }
        }
    }
}
