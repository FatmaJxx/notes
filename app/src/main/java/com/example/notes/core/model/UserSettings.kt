package com.example.notes.core.model

data class UserSettings(
    val theme: AppTheme,
    val dynamicColors: Boolean,
    val defaultNoteColor: AppColor,
    val sortOrder: SortOrder,
    val viewMode: ViewMode
)