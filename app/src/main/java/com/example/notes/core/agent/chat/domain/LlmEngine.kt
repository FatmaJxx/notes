package com.example.notes.core.agent.chat.domain

import com.example.notes.core.agent.chat.data.ModelState
import kotlinx.coroutines.flow.StateFlow

interface LlmEngine {
    val state: StateFlow<ModelState>
    fun isModelDownloaded()
    suspend fun initialize()
    suspend fun downloadModel()
    suspend fun createConversation()
    suspend fun generate(prompt: String): AgentResponse
    fun close()
}