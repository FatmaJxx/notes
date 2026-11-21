package com.example.notes.core.agent.speech

data class VoiceInputState(
    val isListening: Boolean = false,
    val transcription: String = "",
    val partialTranscription: String = "",
    val error: String? = "",
    val isReady: Boolean = false,
)
