package com.example.notes.core.model

data class ChecklistItem(
    val id: Long = 0,
    val noteId: Long = 0,
    val text: String = "",
    val isDone: Boolean = false,
)