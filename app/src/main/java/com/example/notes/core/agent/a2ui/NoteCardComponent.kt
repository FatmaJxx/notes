package com.example.notes.core.agent.a2ui

import androidx.a2ui.compose.runtime.A2uiComponentProperties
import androidx.a2ui.compose.runtime.A2uiComponentScope
import androidx.a2ui.compose.runtime.A2uiProperty
import androidx.a2ui.compose.ui.A2uiComponent
import androidx.a2ui.model.schema.A2uiAnySchema
import androidx.a2ui.model.schema.A2uiArraySchema
import androidx.a2ui.model.schema.A2uiObjectSchema
import androidx.a2ui.model.schema.A2uiStringSchema
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notes.core.designsystem.theme.notes.CardShape
import com.example.notes.core.designsystem.theme.notes.PillShape
import com.example.notes.core.model.AppColor
import com.example.notes.core.model.ChecklistItem
import com.example.notes.core.model.Note
import com.example.notes.core.model.Tag
import kotlin.collections.emptyList

class NoteCardComponent : A2uiComponent {
    private val noteIdProp = A2uiProperty.dynamicNumber(
        key = "noteId", required = true, description = "Note id"
    )
    private val noteTitleProp = A2uiProperty.dynamicString(
        key = "title", required = true, description = "Note title"
    )

    private val noteIsPinned = A2uiProperty.dynamicBoolean(
        key = "isPinned", required = false, description = "whether current note is pinned"
    )

    private val noteColorProp = A2uiProperty.dynamicString(
        key = "color", required = false, description = "note color"
    )

    private val noteContentProp = A2uiProperty.dynamicString(
        key = "content", required = true, description = "Note content"
    )

    private val noteTagsProp = A2uiProperty.dynamicCustom(
        key = "tags", required = false, schema = A2uiArraySchema(
            description = "Tags related to this note", items = A2uiObjectSchema(
                properties = mapOf("name" to A2uiStringSchema()),
                required = setOf("name"),
                isAdditionalPropertiesAllowed = false
            )
        ), safeCast = { value ->
            val tagList = value as? List<*> ?: return@dynamicCustom null

            val tags = mutableListOf<Tag>()

            for (item in tagList) {
                val tagName = when (item) {
                    is Map<*, *> -> item["name"]?.toString()
                    else -> null
                }?.trim()?.trimStart('#')?.trim() ?: continue

                if (tagName.isNotEmpty()) {
                    tags.add(Tag(name = tagName))

                }
            }

            tags
        })

    private val noteChecklistProp = A2uiProperty.dynamicCustom(
        key = "checklist", required = false, schema = A2uiAnySchema(
            description = "checklist items"
        ), safeCast = { value ->
            val checklists = value as? List<*> ?: return@dynamicCustom null

            val items = mutableListOf<ChecklistItem>()

            for (item in checklists) {
                val text = when (item) {
                    is Map<*, *> -> item["text"]?.toString()
                    is ChecklistItem -> item.text
                    else -> null
                }?.trim()?.trimStart('-', '*', '•', ' ')
                    ?.replace(Regex("^\\d+[.)]\\s*"), "")
                    ?.trim() ?: continue

                val isDone = when (item) {
                    is Map<*, *> -> item["isDone"] as? Boolean ?: false
                    is ChecklistItem -> item.isDone
                    else -> false
                }

                items.add(
                    ChecklistItem(
                        text = text,
                        isDone = isDone
                    )
                )
            }

            items
        })


    override val name: String = "NoteCard"
    override val description: String = "A Card used to represent note"
    override val properties: List<A2uiProperty<*>> = listOf(
        noteIdProp,
        noteTitleProp,
        noteIsPinned,
        noteContentProp,
        noteColorProp,
        noteTagsProp,
        noteChecklistProp
    )

    @Composable
    override fun A2uiComponentScope.isReady(properties: A2uiComponentProperties): Boolean {
        return properties.bind(noteIdProp) != null &&
                properties.bind(noteTitleProp) != null
    }

    @Composable
    override fun A2uiComponentScope.Content(
        properties: A2uiComponentProperties, modifier: Modifier
    ) {
        val noteId = properties.bind(noteIdProp)?.toLong() ?: return
        val title = properties.bind(noteTitleProp) ?: "Title"
        val content = properties.bind(noteContentProp) ?: ""

        val isPinned = properties.bind(noteIsPinned) ?: false
        val onPinnedChange = properties.bindUpdater(noteIsPinned)


        val color = properties.bind(noteColorProp)
        val noteColor =
            AppColor.entries.firstOrNull { it.name.equals(color, ignoreCase = true) }
                ?: AppColor.LAVENDER

        val tags = properties.bind(noteTagsProp) ?: emptyList()
        val checklist = properties.bind(noteChecklistProp) ?: emptyList()
        val onChecklistItemChange = properties.bindUpdater(noteChecklistProp)

        val note = Note(
            id = noteId,
            title = title,
            isPinned = isPinned,
            content = content,
            color = noteColor,
            tags = tags,
            checklist = checklist
        )

        val scrollState = rememberScrollState()
        Card(
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = note.color.get()),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = modifier
                .fillMaxWidth()
                .sizeIn(maxHeight = 350.dp)
                .clip(CardShape)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Icon(
                        imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = "Pinned",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(20.dp)
                            .then(
                                if (onPinnedChange != null) Modifier.clickable(onClick = {
                                    onPinnedChange.invoke(!isPinned)

                                    dispatchAction(
                                        actionPayload = mapOf(
                                            "event" to mapOf(
                                                "name" to "note_pinned",
                                                "context" to mapOf(
                                                    "noteId" to note.id,
                                                    "pinned" to !isPinned
                                                )
                                            )
                                        )
                                    )
                                })
                                else Modifier
                            )
                    )
                }

                if (note.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        note.tags.forEach { tag ->
                            Surface(
                                shape = PillShape,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text(
                                    text = "#${tag.name}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                if (note.content.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = note.content, style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        ), maxLines = 4, overflow = TextOverflow.Ellipsis
                    )
                }

                if (note.checklist.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    note.checklist.forEachIndexed { index, item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (item.isDone) MaterialTheme.colorScheme.primaryContainer.copy(
                                alpha = 0.35f
                            )
                            else MaterialTheme.colorScheme.surface.copy(
                                alpha = 0.7f
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = item.isDone, onCheckedChange = { isChecked ->
                                        val updatedList = checklist.toMutableList()

                                        updatedList[index] = updatedList[index].copy(
                                            isDone = isChecked
                                        )

                                        onChecklistItemChange?.invoke(updatedList)

                                        dispatchAction(
                                            actionPayload = mapOf(
                                                "event" to mapOf(
                                                    "name" to "note_checklist",
                                                    "context" to mapOf(
                                                        "noteId" to note.id,
                                                        "checklist" to updatedList
                                                    )
                                                )
                                            )
                                        )
                                    }, colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary
                                    )
                                )

                                Text(
                                    text = item.text,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textDecoration = if (item.isDone) TextDecoration.LineThrough
                                        else TextDecoration.None
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}