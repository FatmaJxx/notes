package com.example.notes.core.agent.a2ui

import androidx.a2ui.compose.runtime.A2uiComponentProperties
import androidx.a2ui.compose.runtime.A2uiComponentScope
import androidx.a2ui.compose.runtime.A2uiProperty
import androidx.a2ui.compose.ui.A2uiComponent
import androidx.a2ui.model.schema.A2uiAnySchema
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.notes.core.designsystem.components.StripeNoteCard
import com.example.notes.core.model.AppColor
import com.example.notes.core.model.Note

class StripeNoteCardComponent : A2uiComponent {
    private val notesProp = A2uiProperty.dynamicCustom(
        key = "notes", required = false, schema = A2uiAnySchema(
            description = "notes"
        ), safeCast = { value ->
            val notes = value as? List<*> ?: return@dynamicCustom null

            val items = mutableListOf<Note>()

            for (item in notes) {
                if (item is Map<*, *>) {
                    val noteId = item["noteId"].toString().toLongOrNull() ?: 0L
                    val title = item["title"].toString()
                    val content = item["content"].toString()
                    val color = item["color"] as? String
                    val noteColor =
                        AppColor.entries.firstOrNull { it.name.equals(color, ignoreCase = true) }
                            ?: AppColor.LAVENDER
                    val createdAt = item["createAt"].toString().toLongOrNull() ?: 0L

                    if (noteId != 0L && title.isNotEmpty()) {
                        items.add(
                            Note(
                                id = noteId, title = title,
                                content = content, color = noteColor, createdAt = createdAt
                            )
                        )
                    }
                }
            }
            items
        })

    override val name: String = "StripeNoteCard"
    override val description: String = "A Card used to represent note"
    override val properties: List<A2uiProperty<*>> = listOf(
        notesProp
    )

    @Composable
    override fun A2uiComponentScope.isReady(properties: A2uiComponentProperties): Boolean {
        return properties.bind(notesProp) != null
    }

    @Composable
    override fun A2uiComponentScope.Content(
        properties: A2uiComponentProperties, modifier: Modifier
    ) {
        val notes = properties.bind(notesProp) ?: return

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(maxHeight = 300.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(notes, key = { it.id }) { note ->
                StripeNoteCard(
                    note = note,
                    onClick = {
                        dispatchAction(
                            actionPayload = mapOf(
                                "event" to mapOf(
                                    "name" to "open_editor",
                                    "context" to mapOf(
                                        "noteId" to note.id
                                    )
                                )
                            )
                        )
                    }
                )
            }
        }
    }
}