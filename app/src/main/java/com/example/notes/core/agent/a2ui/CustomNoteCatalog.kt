package com.example.notes.core.agent.a2ui

import androidx.a2ui.compose.ui.A2uiCatalog

const val NOTE_ASSISTANT_CATALOG_ID: String
= "https://example.com/catalogs/note_assistant/v1/catalog.json"

fun noteAssistantCatalog(): A2uiCatalog{
    return A2uiCatalog(
        catalogId = NOTE_ASSISTANT_CATALOG_ID,
        components = listOf(
            NoteCardComponent(),
            StripeNoteCardComponent()
        )
    )
}