package com.example.notes.core.agent.chat.tools

import android.util.Log
import com.example.notes.core.agent.chat.domain.AgentTool
import com.example.notes.core.agent.chat.domain.ToolArguments
import com.example.notes.core.agent.chat.domain.ToolParameter
import com.example.notes.core.data.repository.NoteRepository
import com.example.notes.core.model.AppColor
import com.example.notes.core.model.Note
import com.google.ai.edge.litertlm.ToolException

class CreateNoteTool(
    private val noteRepository: NoteRepository
) : AgentTool {

    private val colorNames = AppColor.entries.joinToString {
        it.name.lowercase()
    }

    override val name = "create_note"

    override val description = """
        Create and save a new note in the user's notes app.

        Use this tool only when user explicitly or clearly ask you
        to create, write, make, save, or store new note.

        A note can contain plain text, a checklist, or both.
        It must have a title, It can optionally have a content, checklist items,
        a color, and organizational properties such as pinned,
        archived, or favorite.

        Do not use this tool when the user only wants an explanation,
        an answer to a question, or text generated in the conversation
        without asking to save it as a note.
    """.trimIndent()

    override val parameters = listOf(
        ToolParameter(
            name = "title",
            type = ToolParameter.Type.STRING,
            description = """
                A short, meaningful title summarizing the main reason or
                purpose of the note

                Keep it concise and use no more than 8 words,
                don't include dates or time
                
                Example:
                Create a note about Noura's birthday party ...
                
                Output:
                {title:"Noura's birthday party"}
            """.trimIndent()
        ),


        ToolParameter(
            name = "content",
            type = ToolParameter.Type.STRING,
            description = """
        Descriptive text for the note.

        Use this for:
        - Explanations and descriptions.
        - Facts, ideas, and information.
        - Meeting notes and instructions.
        - Lists of information that are not tasks.
        - places

        Do not put actionable checklist items here.
        Do not duplicate checklist items.

        Return an empty string when the note contains
        only checklist items.
    """.trimIndent(),
            isRequired = false
        ),


        ToolParameter(
            name = "isPinned",
            type = ToolParameter.Type.BOOLEAN,
            description = """
                Whether the new note should be pinned.

                Set to true only when the user explicitly asks to pin,
                keep at the top, or prioritize the note, otherwise
                omit this parameter.
            """.trimIndent(),
            isRequired = false
        ),

        ToolParameter(
            name = "isArchived",
            type = ToolParameter.Type.BOOLEAN,
            description = """
                Whether the new note should be archived.

                Set to true only when the user explicitly asks
                to create or save the note as archived, otherwise
                omit this parameter.
            """.trimIndent(),
            isRequired = false
        ),

        ToolParameter(
            name = "isFavorite",
            type = ToolParameter.Type.BOOLEAN,
            description = """
                Whether the new note should be marked as a favorite.

                Set to true only when the user explicitly asks to mark
                the note as a favorite or add it to favorites, otherwise
                omit this parameter.
            """.trimIndent(),
            isRequired = false
        ),

        ToolParameter(
            name = "color",
            type = ToolParameter.Type.STRING,
            description = """
                The background color of the new note.

                Only set this parameter when the user explicitly
                requests a particular color.

                The value must match one of the supported color names:
                $colorNames

                If the user describes a shade or uses a color synonym,
                choose the closest supported color when the match
                is unambiguous.
            """.trimIndent(),
            isRequired = false
        ),

        ToolParameter(
            name = "checklist",
            type = ToolParameter.Type.ARRAY,
            description = """
        Actionable items that the user needs to complete.

        Use this for explicitly requested checklists,
        to-do lists, shopping lists, packing lists,
        and other collections of tasks.

        Each item must contain:
        - text: the task description.
        - isDone: true if the user says it is completed,
          otherwise false.

        Do not use this for lists of facts or information.

        Return an empty array when there are no tasks.
        Never omit tasks provided by the user.
    """.trimIndent(),
            isRequired = false,
            items = ToolParameter(
                name = "item",
                type = ToolParameter.Type.OBJECT,
                description = "One actionable checklist item.",
                properties = listOf(
                    ToolParameter(
                        name = "text",
                        type = ToolParameter.Type.STRING,
                        description = "The task description."
                    ),
                    ToolParameter(
                        name = "isDone",
                        type = ToolParameter.Type.BOOLEAN,
                        description = "Whether the task is completed.",
                        isRequired = false
                    )
                )
            )
        ),

        ToolParameter(
            name = "tags",
            type = ToolParameter.Type.ARRAY,
            description = """
                Tags are a collection of strings, each string will be precedes by #
                
                Example:
                #Work, #Birthday, #Vacation
                
                Output:
                you should remove the hashtag from each tag and only keep string
                {"Work", "Birthday", "Vacation"}
            """.trimIndent(),
            isRequired = false,
            items = ToolParameter(
                name = "tag",
                type = ToolParameter.Type.OBJECT,
                description = "A single tag consist of string and #",
                properties = listOf(
                    ToolParameter(
                        name = "name",
                        type = ToolParameter.Type.STRING,
                        description = "name is the string that come after #"
                    )
                )
            )
        ),
        ToolParameter(
            name = "reminder",
            type = ToolParameter.Type.STRING,
            description = """
                Only when user tell you to remind them about this note,
                
                it will come in form of date and time
                
                Return the reminder as a JSON object containing:
                - year: four-digit year
                - month: month number (1-12)
                - day: day of month (1-31)
                - hour: 24-hour format (0-23)
                - minute: minute (0-59)
        
                the date is entered in this format DD/MM/yyyy
                
                Example,
                remind me about this note on Sunday 1/11 at 5 pm
                
                Output:
                {"year":2026,"month":11,"day":1,"hour":17,"minute":0}
            """.trimIndent(),
            isRequired = false
        ),
    )

    override suspend fun execute(
        arguments: ToolArguments
    ): Map<String, Any?> {

        val title = arguments.getString("title")
            ?: throw ToolException("The note title is missing.")

        val content = arguments.getString("content") ?: ""

        val isPinned = arguments.getBoolean("isPinned") ?: false

        val isArchived = arguments.getBoolean("isArchived") ?: false

        val isFavorite = arguments.getBoolean("isFavorite") ?: false

        val color = arguments.getColor("color") ?: AppColor.LAVENDER

        val checklist = arguments.getCheckListItems("checklist")

        val tagsList = arguments.getTags("tags")

        val reminder = arguments.getReminder("reminder")

        Log.d("AGENT", "Parsed checklist: $checklist")

        Log.d("AGENT", "Parsed tags: $tagsList")

        Log.d("AGENT", "Parsed reminder: $reminder")


        // Validate the title.
        if (title.isBlank()) {
            throw ToolException("The note title cannot be empty.")
        }

        // A note must contain text, checklist items, or both.
        if (content.isBlank() && checklist.isEmpty()) {
            throw ToolException(
                "The note needs content or at least one checklist item."
            )
        }

        val note = Note(
            title = title.trim(),
            content = content,
            isPinned = isPinned,
            isArchived = isArchived,
            isFavorite = isFavorite,
            color = color
        )

        val noteId = noteRepository.saveNote(
            note = note,
            checklist = checklist,
            tags = tagsList,
            reminder = reminder
        )

        return mapOf(
            "noteId" to noteId,
            "title" to title.trim(),
            "content" to content,
            "isPinned" to isPinned,
            "color" to color,
            "tags" to arguments.values["tags"],
            "checklist" to arguments.values["checklist"]
        )
    }
}

