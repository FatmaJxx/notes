package com.example.notes.feature.agent

import androidx.a2ui.model.processor.A2uiSurfaceModel

data class AgentUiState(
    val messages: List<Message> = emptyList(),
    val prompt: String = "",
    val isLoading: Boolean = false,
    val isListening: Boolean = false,
    val isVoiceReady: Boolean = false,
    val error: String? = null,
    val isGemmaDownloaded: Boolean = false,
    val isGemmaDownloading: Boolean = false,
    val isGemmaLoading: Boolean = false,
    val isGemmaReady: Boolean = false,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L
)

data class Message(
    val message: String = "",
    val type: Type,
    val activeSurface : A2uiSurfaceModel? = null
) {
    enum class Type {
        AI, HUMAN
    }
}

sealed interface AgentUiEvent {
    data class PromptChanged(val prompt: String) : AgentUiEvent
    data object PromptSend : AgentUiEvent
    data object MicPressed : AgentUiEvent
    data object DownloadModel : AgentUiEvent
    data object LoadModel : AgentUiEvent
}

sealed interface AgentNavigationEvents {
    data class NavigateToEditor(val noteId: Long) : AgentNavigationEvents
}