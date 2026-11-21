package com.example.notes.core.model

import java.util.Collections.emptyList

data class Note(
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val color: AppColor = AppColor.LAVENDER,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val deletedAt: Long? = null,
    val folderId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val tags: List<Tag> = emptyList(),
    val checklist: List<ChecklistItem> = emptyList(),
    val reminder: Reminder? = null
)
