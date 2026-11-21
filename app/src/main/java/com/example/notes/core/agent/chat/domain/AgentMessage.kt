package com.example.notes.core.agent.chat.domain

data class AgentResponse(
    val text: String,
    val toolResponse: ToolResponse? = null
)

data class ToolCall(
    val name: String,
    val arguments: ToolArguments
)

data class ToolResponse(
    val name: String,
    val toolResponse: Map<String, Any?>
)