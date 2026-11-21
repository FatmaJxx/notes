package com.example.notes.feature.agent

import android.util.Log
import androidx.a2ui.compose.runtime.A2uiMessageParser
import androidx.a2ui.model.processor.A2uiMessageProcessor
import androidx.a2ui.model.processor.processInput
import androidx.a2ui.model.protocol.A2uiClientEventMessage
import androidx.a2ui.compose.ui.A2uiMessageProcessor as createA2uiMessageProcessor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.core.agent.a2ui.NOTE_ASSISTANT_CATALOG_ID
import com.example.notes.core.agent.a2ui.noteAssistantCatalog
import com.example.notes.core.agent.chat.domain.LlmEngine
import com.example.notes.core.agent.chat.domain.ToolResponse
import com.example.notes.core.data.repository.NoteRepository
import com.example.notes.core.model.AppColor
import com.example.notes.core.model.ChecklistItem
import com.example.notes.core.agent.speech.VoiceInputManager
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random

class AgentViewModel(
    private val llm: LlmEngine,
    private val noteRepository: NoteRepository,
    private val voiceInputManager: VoiceInputManager
) : ViewModel() {

    private val _state = MutableStateFlow(AgentUiState())
    val state: StateFlow<AgentUiState> = _state.asStateFlow()

    private val _uiEvent = Channel<AgentNavigationEvents>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var generationJob: Job? = null
    private var downloaderJob: Job? = null
    private var loaderJob: Job? = null

    private val a2uiParser = A2uiMessageParser()
    private val messageProcessor: A2uiMessageProcessor =
        createA2uiMessageProcessor(catalogs = listOf(noteAssistantCatalog()))

    init {
        viewModelScope.launch(Dispatchers.Default) {
            messageProcessor.collectMessages()
        }

        viewModelScope.launch {
            messageProcessor.activeSurfaces.collect { surfaces ->
                val surface = surfaces.lastOrNull() ?: return@collect

                _state.update {
                    it.copy(
                        messages = it.messages + Message(
                            type = Message.Type.AI,
                            activeSurface = surface
                        )
                    )
                }
            }
        }

        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            messageProcessor.outboundEvents.collect { message ->
                if (message is A2uiClientEventMessage) {
                    handleClientEvent(message)
                }
            }
        }

        viewModelScope.launch {
            voiceInputManager.state.collect { voice ->
                _state.update { currentState ->
                    currentState.copy(
                        isListening = voice.isListening,
                        prompt = voice.transcription.ifEmpty { voice.partialTranscription },
                        error = voice.error,
                        isVoiceReady = voice.isReady,
                    )
                }
            }
        }

        viewModelScope.launch {
            llm.state.collect { llmState ->
                _state.update { currentState ->
                    currentState.copy(
                        isGemmaDownloaded = llmState.isDownloaded,
                        isGemmaDownloading = llmState.isDownloading,
                        downloadedBytes = llmState.downloadedBytes,
                        totalBytes = llmState.totalBytes,
                        isGemmaReady = llmState.isReady,
                        isGemmaLoading = llmState.isLoading,
                        error = llmState.error
                    )
                }
            }
        }
    }

    private fun handleClientEvent(event: A2uiClientEventMessage) {
        val type = event.type
        when (type) {
            "open_editor" -> {
                val context = event.context
                val noteId = context["noteId"].toString().toLongOrNull() ?: return
                _uiEvent.trySend(
                    AgentNavigationEvents.NavigateToEditor(noteId)
                )
            }

            "note_pinned" -> {
                val context = event.context
                val noteId = context["noteId"].toString().toLongOrNull() ?: return
                val pinned = context["pinned"].toString().toBoolean()

                viewModelScope.launch {
                    noteRepository.setPinned(
                        noteId = noteId,
                        isPinned = pinned
                    )
                }
            }

            "note_checklist" -> {
                val context = event.context
                val noteId = context["noteId"].toString().toLongOrNull() ?: 0L
                val checklist = context["checklist"] as? List<*> ?: return

                val items = mutableListOf<ChecklistItem>()

                for (item in checklist) {
                    val text = when (item) {
                        is Map<*, *> -> item["text"]?.toString()
                        is ChecklistItem -> item.text
                        else -> null
                    } ?: continue

                    val isDone = when (item) {
                        is Map<*, *> -> item["isDone"] as? Boolean ?: false
                        is ChecklistItem -> item.isDone
                        else -> false
                    }

                    items.add(
                        ChecklistItem(
                            text = text,
                            isDone = isDone
                        )
                    )
                }
                viewModelScope.launch {
                    noteRepository.setChecklist(
                        noteId = noteId,
                        checklist = items
                    )
                }
            }
        }
    }


    fun onNetworkMessage(messages: List<String>) {
        messages.forEach { message ->
            messageProcessor.processInput(a2uiParser, message)
        }
    }

    fun onEvent(event: AgentUiEvent) {
        when (event) {
            is AgentUiEvent.PromptChanged -> {
                _state.update { it.copy(prompt = event.prompt) }
            }

            AgentUiEvent.PromptSend -> {
                val prompt = _state.value.prompt

                if (prompt.isBlank()) return

                _state.update {
                    it.copy(
                        messages = it.messages + Message(
                            message = prompt,
                            type = Message.Type.HUMAN
                        ),
                        prompt = ""
                    )
                }
                ask(prompt)
            }

            AgentUiEvent.MicPressed -> {
                Log.d("VOICE", "is Listening ${_state.value.isListening}")
                Log.d("VOICE", "error ${_state.value.error}")

                viewModelScope.launch {
                    if (_state.value.isListening) {
                        voiceInputManager.stopListening()
                    } else {
                        if (_state.value.isVoiceReady)
                            voiceInputManager.startListening()
                    }
                }
            }

            AgentUiEvent.DownloadModel -> downloadModel()
            AgentUiEvent.LoadModel -> loadModel()
        }
    }

    fun loadModel(){
        if(loaderJob?.isActive == true)return
        loaderJob = viewModelScope.launch {
            llm.initialize()
        }
    }

    fun downloadModel() {
        if (downloaderJob?.isActive == true) return

        downloaderJob = viewModelScope.launch {
            llm.downloadModel()
        }
    }

    fun ask(prompt: String) {
        generationJob?.cancel()

        generationJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                llm.createConversation()
                val answer = llm.generate(prompt)
                _state.update { it.copy(isLoading = false) }

                if (answer.toolResponse != null) {
                    if (answer.toolResponse.name == "delete_note") {
                        _state.update {
                            it.copy(
                                messages = it.messages +
                                        Message(
                                            message = answer.text,
                                            type = Message.Type.AI
                                        )
                            )
                        }
                    } else {
                        createJson(answer.toolResponse)
                    }
                } else {
                    _state.update {
                        it.copy(
                            messages = it.messages +
                                    Message(
                                        message = answer.text,
                                        type = Message.Type.AI
                                    )
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.localizedMessage) }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun createJson(toolResponse: ToolResponse) {
        if (toolResponse.name == "create_note") {
            createNoteJson(toolResponse)
        } else if (toolResponse.name == "search_note") {
            searchNoteJson(toolResponse)
        }
    }

    private fun searchNoteJson(toolResponse: ToolResponse) {
        val notes = toolResponse.toolResponse["notes"] as? List<*> ?: return

        val surfaceId = "note-${Random.nextInt()}"
        val version = "v0.9.1"

        val notesJsonArray = createJsonArray(list = notes)

        val createSurface = JSONObject()
            .put("version", version)
            .put(
                "createSurface", JSONObject()
                    .put("surfaceId", surfaceId)
                    .put("catalogId", NOTE_ASSISTANT_CATALOG_ID)
            )

        val updateComponents = JSONObject()
            .put("version", version)
            .put(
                "updateComponents", JSONObject()
                    .put("surfaceId", surfaceId)
                    .put(
                        "components", JSONArray().put(
                            JSONObject()
                                .put("id", "root")
                                .put("component", "StripeNoteCard")
                                .put("notes", notesJsonArray)

                        )
                    )
            )
        val list = listOf(createSurface, updateComponents).map { it.toString() }
        onNetworkMessage(list)
    }

    private fun createNoteJson(toolResponse: ToolResponse) {
        val response = toolResponse.toolResponse

        val noteId = response["noteId"] as? Long ?: return
        val title = response["title"] as? String ?: return
        val isPinned = response["isPinned"] as? Boolean ?: false
        val content = response["content"] as? String ?: ""
        val color = response["color"] as? AppColor
        val tags = response["tags"] as? List<*>
        val checklist = response["checklist"] as? List<*>
        val tagsJsonArray = createJsonArray(list = tags)
        val checklistJsonArray = createJsonArray(list = checklist)


        val surfaceId = "note-${noteId}"
        val version = "v0.9.1"

        val createSurface = JSONObject()
            .put("version", version)
            .put(
                "createSurface", JSONObject()
                    .put("surfaceId", surfaceId)
                    .put("catalogId", NOTE_ASSISTANT_CATALOG_ID)
            )

        val updateDataModel = JSONObject()
            .put("version", version)
            .put(
                "updateDataModel", JSONObject()
                    .put("surfaceId", surfaceId)
                    .put("path", "/")
                    .put(
                        "value", JSONObject()
                            .put(
                                "note", JSONObject()
                                    .put("isPinned", isPinned)
                                    .put("checklist", checklistJsonArray)
                            )
                    )
            )

        val updateComponents = JSONObject()
            .put("version", version)
            .put(
                "updateComponents", JSONObject()
                    .put("surfaceId", surfaceId)
                    .put(
                        "components", JSONArray().put(
                            JSONObject()
                                .put("id", "root")
                                .put("component", "NoteCard")
                                .put("noteId", noteId)
                                .put("title", title)
                                .put("isPinned", JSONObject().put("path", "/note/isPinned"))
                                .put("content", content)
                                .put("color", color)
                                .put("tags", tagsJsonArray)
                                .put("checklist", JSONObject().put("path", "/note/checklist"))
                        )
                    )
            )
        val list = listOf(createSurface, updateDataModel, updateComponents).map { it.toString() }
        onNetworkMessage(list)
    }

    override fun onCleared() {
        generationJob?.cancel()
        llm.close()
        voiceInputManager.onCleared()
        downloaderJob?.cancel()
        loaderJob?.cancel()
    }

    private fun createJsonArray(list: List<*>?) = JSONArray().apply {
        list?.forEach { item ->
            if (item is Map<*, *>) {
                val obj = JSONObject()
                item.forEach { (k, v) ->
                    if (k != null) obj.put(k.toString(), v)
                }
                put(obj)
            }
        }
    }
}
