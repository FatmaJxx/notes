package com.example.notes

import com.example.notes.core.model.AppColor
import com.example.notes.core.model.ChecklistItem
import com.example.notes.core.model.Note
import com.example.notes.core.model.Tag
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.collections.get


class NoteModelTest {

    @Test
    fun testNoteCreationAndDefaults() {
        val note = Note(
            id = 1L,
            title = "Weekend in Kyoto",
            content = "Tea ceremony, Nishiki Market, and the quiet garden near Gion.",
            color = AppColor.LAVENDER,
            isPinned = true
        )

        assertEquals("Weekend in Kyoto", note.title)
        assertEquals(AppColor.LAVENDER, note.color)
        assertTrue(note.isPinned)
        assertFalse(note.isFavorite)
        assertFalse(note.isArchived)
    }

    @Test
    fun testChecklistProgress() {
        val items = listOf(
            ChecklistItem(id = 1, text = "Task 1", isDone = true),
            ChecklistItem(id = 2, text = "Task 2", isDone = true),
            ChecklistItem(id = 3, text = "Task 3", isDone = false)
        )

        val note = Note(
            id = 2L,
            title = "Checklist Note",
            checklist = items
        )

        assertEquals(3, note.checklist.size)
        assertEquals(2, note.checklist.count { it.isDone })
    }

    @Test
    fun testTagsAssociation() {
        val tags = listOf(
            Tag(id = 1, name = "travel"),
            Tag(id = 2, name = "kyoto")
        )

        val note = Note(
            id = 3L,
            title = "Travel plan",
            tags = tags
        )

        assertEquals(2, note.tags.size)
        assertEquals("travel", note.tags[0].name)
    }
}
