package com.example.notes.core.agent.chat.tools

import com.example.notes.core.agent.chat.domain.AgentTool
import com.example.notes.core.agent.chat.domain.ToolArguments
import com.example.notes.core.agent.chat.domain.ToolParameter
import com.example.notes.core.data.repository.NoteRepository
import com.google.ai.edge.litertlm.ToolException

class DeleteNoteTool(
    val noteRepository: NoteRepository
) : AgentTool {

    override val name = "delete_note"

    override val description = """
        Use this tool only when user explicitly or clearly ask you
        to delete note by it's title
    """.trimIndent()

    override val parameters = listOf(
        ToolParameter(
            name = "title",
            type = ToolParameter.Type.STRING,
            isRequired = true,
            description = """
                the title of note
                """.trimIndent()
        )
    )

    override suspend fun execute(arguments: ToolArguments): Map<String, Any?> {
        val title = arguments.getString("title")

        if (title.isNullOrEmpty()) {
            throw ToolException("Query can't be empty")
        }

        noteRepository.moveToTrashByTitle(title = title)

        return mapOf(
            "result" to "Note moved to trash successfully"
        )
    }
}