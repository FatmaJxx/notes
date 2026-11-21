package com.example.notes.core.agent.chat.domain

interface AgentTool {
    val name: String
    val description: String
    val parameters: List<ToolParameter>

    suspend fun execute(arguments: ToolArguments): Map<String, Any?>
}