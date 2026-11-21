package com.example.notes.feature.folders.tags

import com.example.notes.core.model.Tag

data class TagsUiState(
    val tags: List<Tag> = emptyList(),
    val searchQuery: String = "",
    val newTagName: String = "",
    val showCreateDialog: Boolean = false
){
    val filteredTags: List<Tag>
        get() = if (searchQuery.isBlank()){
            tags
        }else{
            tags.filter {
                it.name.contains(searchQuery.trim(), ignoreCase = true)
            }
        }

}

sealed interface TagsUiEvent {
    data object BackPressed : TagsUiEvent
    data object ShowCreateDialog : TagsUiEvent
    data object DismissCreateDialog : TagsUiEvent
    data class SearchQueryChanged(val query: String) : TagsUiEvent
    data class NewTagNameChanged(val name: String) : TagsUiEvent
    data class DeleteTag(val tag: Tag) : TagsUiEvent
    data object CreateTag : TagsUiEvent
}

sealed interface TagsNavigationEvents {
    data object NavigateBack : TagsNavigationEvents
}