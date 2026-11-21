package com.example.notes.core.agent.chat.domain

data class ToolParameter(
    val name: String,
    val type: Type,
    val description: String,
    val isRequired: Boolean = true,
    val properties: List<ToolParameter> = emptyList(),
    val items: ToolParameter? = null
) {
    enum class Type {
        STRING,
        BOOLEAN,
        OBJECT,
        ARRAY
    }
}

