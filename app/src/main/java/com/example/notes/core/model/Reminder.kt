package com.example.notes.core.model

data class Reminder(
    val id: Long = 0,
    val noteId: Long,
    val timestamp: Long,
    val repeatInterval: RepeatInterval = RepeatInterval.ONCE,
    val isActive: Boolean = true
)

enum class RepeatInterval(val label: String) {
    ONCE("Once"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}