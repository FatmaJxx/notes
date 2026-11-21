package com.example.notes.core.agent.speech

import android.util.Log
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.audio.AudioSource
import com.google.mlkit.genai.speechrecognition.SpeechRecognition
import com.google.mlkit.genai.speechrecognition.SpeechRecognizer
import com.google.mlkit.genai.speechrecognition.SpeechRecognizerOptions
import com.google.mlkit.genai.speechrecognition.SpeechRecognizerResponse
import com.google.mlkit.genai.speechrecognition.speechRecognizerOptions
import com.google.mlkit.genai.speechrecognition.speechRecognizerRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException

class VoiceInputManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(VoiceInputState())
    val state = _state.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var listenJob: Job? = null
    private var statusCheckJob: Job? = null

    init {
        initSpeech()
    }

    private fun initSpeech() {
        if (speechRecognizer != null) {
            Log.d(
                "Voice",
                "initSpeech: Recognizer already initialized",
            )
            return
        }

        Log.d(
            "Voice",
            "initSpeech: Constructing fresh SpeechRecognizer for English language",
        )

        try {
            val speechOptions: SpeechRecognizerOptions =
                speechRecognizerOptions {
                    locale = Locale.US
                    preferredMode = SpeechRecognizerOptions.Mode.MODE_BASIC
                }
            speechRecognizer = SpeechRecognition.getClient(speechOptions)

            checkModelStatus()
        } catch (e: Exception) {
            Log.d(
                "Voice",
                "initSpeech: Failed to build ML Kit clients asynchronously.",
                e,
            )
            _state.update {
                it.copy(
                    error = "Something went wrong please try again",
                    isReady = false
                )
            }
        }
    }

    private fun checkModelStatus() {
        if (statusCheckJob?.isActive == true) {
            Log.d(
                "Voice",
                "checkModelStatus: Status check already active, skipping duplicate invocation.",
            )
            return
        }

        val recognizer =
            speechRecognizer ?: run {
                Log.d(
                    "Voice",
                    "checkModelStatus: Bypassed check because speechRecognizer is null.",
                )
                return
            }

        statusCheckJob = scope.launch {
            try {
                Log.d("Voice", "checkModelStatus: Checking speech model status asynchronously...")
                _state.update { it.copy(isReady = false, error = null) }
                val status = recognizer.checkStatus()
                Log.d("Voice", "checkModelStatus: Speech model status resolved to: $status")
                if (status == FeatureStatus.DOWNLOADABLE) {
                    recognizer.download().collect { downloadStatus ->
                        Log.d("Voice", "checkModelStatus collect: downloadStatus=$downloadStatus")

                        when (downloadStatus) {
                            DownloadStatus.DownloadCompleted -> {
                                Log.d(
                                    "Voice",
                                    "checkModelStatus collect: Download completed successfully!",
                                )
                                _state.update {
                                    it.copy(
                                        isReady = true,
                                        error = null
                                    )
                                }
                            }

                            is DownloadStatus.DownloadFailed -> {
                                _state.update {
                                    it.copy(
                                        error = downloadStatus.e.localizedMessage,
                                        isReady = false,
                                    )
                                }
                            }

                            is DownloadStatus.DownloadStarted -> {

                            }

                            else -> {
                                _state.update { it.copy(isReady = false) }
                            }
                        }
                    }
                } else if (status == FeatureStatus.AVAILABLE) {
                    _state.update {
                        it.copy(
                            isReady = true,
                            error = null
                        )
                    }
                } else {
                    _state.update { it.copy(isReady = false) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.d(
                    "VoiceInputManager",
                    "checkModelStatus: Error checking or downloading speech model",
                    e
                )
                _state.update {
                    it.copy(
                        error = "Something went wrong",
                        isReady = false,
                    )
                }
            }
        }
    }

    fun startListening() {
        if (!_state.value.isReady) return
        if (listenJob?.isActive == true) return

        val recognizer = speechRecognizer ?: return

        _state.update {
            it.copy(
                isListening = true,
                error = null,
                transcription = "",
                partialTranscription = ""
            )
        }
        listenJob = scope.launch {
            try {

                val request = speechRecognizerRequest { audioSource = AudioSource.fromMic() }
                recognizer.startRecognition(request).collect { response ->
                    when (response) {
                        is SpeechRecognizerResponse.AudioLevelResponse -> {}
                        SpeechRecognizerResponse.CompletedResponse -> {
                            _state.update {
                                it.copy(
                                    isListening = false
                                )
                            }
                        }

                        is SpeechRecognizerResponse.ErrorResponse -> {
                            _state.update {
                                it.copy(
                                    error = "error while detecting" + response.e.message,
                                    isListening = false
                                )
                            }
                        }

                        is SpeechRecognizerResponse.FinalTextResponse -> {
                            _state.update {
                                it.copy(
                                    transcription = "${it.transcription} ${response.text}".trim(),
                                    partialTranscription = ""
                                )
                            }

                            Log.d("VOICE", "final text ${response.text}")
                        }

                        is SpeechRecognizerResponse.PartialTextResponse -> {
                            _state.update {
                                it.copy(
                                    partialTranscription = response.text
                                )
                            }

                            Log.d("VOICE", "partial text ${response.text}")

                        }
                    }
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.message ?: "Speech recognition failed")
                }
            } finally {
                _state.update {
                    it.copy(isListening = false)
                }
            }
        }
    }

    suspend fun stopListening() {
        speechRecognizer?.stopRecognition()
        _state.update { it.copy(isListening = false) }
        listenJob?.cancel()
        listenJob = null
    }

    fun onCleared() {
        listenJob?.cancel()
        statusCheckJob?.cancel()

        speechRecognizer?.close()
        speechRecognizer = null

        scope.cancel()
    }
}