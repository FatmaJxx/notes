package com.example.notes.core.agent.chat.tools

import com.example.notes.core.agent.chat.domain.AgentTool
import com.example.notes.core.agent.chat.domain.ToolArguments
import com.example.notes.core.agent.chat.domain.ToolParameter
import com.example.notes.core.data.repository.NoteRepository
import com.google.ai.edge.litertlm.ToolException
import kotlinx.coroutines.flow.first

class SearchNoteTool(
    val noteRepository: NoteRepository
) : AgentTool {

    override val name = "search_note"

    override val description = """
        Use this tool only when user explicitly or clearly ask you
        to search for notes
    """.trimIndent()

    override val parameters = listOf(
        ToolParameter(
            name = "query",
            type = ToolParameter.Type.STRING,
            isRequired = true,
            description = """
                Query is a descriptive points the user ask you to search for
                it either contains note title, content or tags
                """.trimIndent()
        )
    )

    override suspend fun execute(arguments: ToolArguments): Map<String, Any?> {
        val query = arguments.getString("query")

        if (query.isNullOrEmpty()) {
            throw ToolException("Query can't be empty")
        }

        val notes =
            noteRepository.searchNotes(query).first().map { note ->
                mapOf(
                    "noteId" to note.id,
                    "title" to note.title,
                    "content" to note.content,
                    "color" to note.color,
                    "createAt" to note.createdAt
                )
            }

        return mapOf(
            "notes" to notes
        )
    }
}