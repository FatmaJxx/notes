package com.example.notes.core.agent.chat.data

import android.util.Log
import com.example.notes.core.agent.chat.domain.AgentResponse
import com.example.notes.core.agent.chat.domain.AgentTool
import com.example.notes.core.agent.chat.domain.LlmEngine
import com.example.notes.core.agent.chat.domain.ToolArguments
import com.example.notes.core.agent.chat.domain.ToolCall
import com.example.notes.core.agent.chat.domain.ToolResponse
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.tool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.files.SystemFileSystem
import kotlin.Boolean
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.io.files.Path


data class ModelState(
    val isDownloaded: Boolean = false,
    val isDownloading: Boolean = false,
    val isLoading: Boolean = false,
    val isReady: Boolean = false,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val error: String? = null
)
class LiteRtLlmEngine(
    private val tools: List<AgentTool>,
    private val modelDownloader: ModelDownloader,
    private val path: ModelPath,
) : LlmEngine {

    private val _state = MutableStateFlow(ModelState())
    override val state = _state.asStateFlow()

    private val toolsByName = tools.associateBy { it.name }

    private val mutex = Mutex()

    private var engine: Engine? = null

    private var conversation: Conversation? = null


    init {
        isModelDownloaded()
    }

    override suspend fun initialize() {
        mutex.withLock {
            if (engine != null) {
                _state.update {
                    it.copy(
                        isDownloaded = true,
                        isDownloading = false,
                        isLoading = false,
                        isReady = true,
                        error = null
                    )
                }
                return@withLock
            }

            withContext(Dispatchers.Default) {
                _state.update { it.copy(isLoading = true, error = null) }
                try {
                    val config = EngineConfig(
                        modelPath = path.modelFile,
                        backend = Backend.CPU(),
                        maxNumTokens = MAX_NUM_TOKENS,
                        cacheDir = path.cacheDir
                    )

                    val newEngine = Engine(config)
                    newEngine.initialize()
                    engine = newEngine

                    _state.update {
                        it.copy(
                            isLoading = false,
                            isReady = true,
                            isDownloading = false,
                            isDownloaded = true,
                            error = null
                        )
                    }
                } catch (e: CancellationException) {
                    _state.update { it.copy(isLoading = false) }
                    throw e
                } catch (e: Exception) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isReady = false,
                            error = "Model init failed: ${e.message ?: e::class.simpleName}"
                        )
                    }
                }
            }
        }
    }

    override fun isModelDownloaded() {
        _state.update { it.copy(isDownloaded = SystemFileSystem.exists(Path(path.modelFile))) }
    }

    override suspend fun createConversation() {
        mutex.withLock {
            if (conversation != null) {
                return@withLock
            }

            val currentEngine =
                checkNotNull(engine) { "Call initialize() before createConversation()" }

            conversation = withContext(Dispatchers.Default) {
                currentEngine.createConversation(
                    ConversationConfig(
                        systemInstruction = Contents.of(systemPrompt),
                        tools = tools.map { tool(AgentToolAdapter(it)) },
                        automaticToolCalling = false
                    )
                )
            }

        }
    }

    override suspend fun generate(prompt: String): AgentResponse {
        return mutex.withLock {
            val conversation = checkNotNull(conversation) {
                "Call createConversation() before generate()"
            }

            withContext(Dispatchers.Default) {
                var message = conversation.sendMessage(prompt)
                var round = 0
                var toolResponse: ToolResponse? = null

                while (message.toolCalls.isNotEmpty() && round < MAX_TOOL_ROUNDS) {
                    round++
                    val responses = message.toolCalls.map { call ->
                        val result = executeTool(
                            ToolCall(
                                name = call.name,
                                arguments = ToolArguments(call.arguments)
                            )
                        )

                        toolResponse = ToolResponse(call.name, result)
                        Content.ToolResponse(call.name, result.toString())
                    }
                    message = conversation.sendMessage(Message.tool(Contents.of(responses)))
                }

                AgentResponse(
                    text = message.contents.toString().trim(),
                    toolResponse = toolResponse
                )
            }
        }
    }

    override suspend fun downloadModel() {
        _state.update {
            it.copy(isDownloading = true, error = null)
        }
        try {
            modelDownloader.download(
                url = LocalModel.DOWNLOAD_URL,
                targetFile = path.modelFile,
                expectedSizeBytes = LocalModel.SIZE_BYTES
            ).collect { progress ->
                _state.update {
                    it.copy(
                        downloadedBytes = progress.downloadedBytes,
                        totalBytes = progress.totalBytes
                    )
                }
            }

            _state.update {
                it.copy(isDownloading = false, isDownloaded = true, error = null)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _state.update { it.copy(error = "Model download failed: $e", isDownloading = false) }
            return
        }
    }

    private suspend fun executeTool(call: ToolCall): Map<String, Any?> {
        Log.d("AGENT", "Tool name: ${call.name}")
        Log.d("AGENT", "Tool parameters: ${call.arguments}")


        val tool = toolsByName[call.name]
            ?: return mapOf("error" to "Unknown tool '${call.name}'. Available: ${toolsByName.keys.joinToString()}")

        return try {
            tool.execute(call.arguments)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            mapOf("error" to (e.message ?: "The tool failed."))
        }
    }

    override fun close() {
        conversation?.close()
        conversation = null
        engine?.close()
        engine = null
    }

    private companion object {
        const val MAX_NUM_TOKENS = 4096
        const val MAX_TOOL_ROUNDS = 5
    }

    private val systemPrompt: String = """
You are an AI assistant inside a Notes app.

- Answer normally without using tools.
- Call create_note ONLY when the user explicitly asks to create, write, or save a note.
- Never create notes during normal conversation.
- call search_note ONLY when user explicitly ask you to search notes.
- call delete_note ONLY when user explicitly ask you to delete note by it's title.

When creating a note:
- Generate a meaningful title (maximum 8 words), never leave title empty.
- Use content for descriptive information, ideas, explanations, and general notes.
- Use checklist ONLY for actionable tasks or explicitly requested checklists.
- Checklist items must be objects: {"text": "...", "isDone": false}.
- Use tags only when requested. Tags must be objects: {"name": "Work"}. Never use plain strings.
- Use color only when requested. Valid values: LAVENDER, PINK, BLUE, GREEN, YELLOW, ORANGE, RED.
- Set isPinned, isFavorite, and isArchived to true only when explicitly requested.
- Add reminders only when requested. Use a JSON string with year, month, day, hour, and minute.
- Preserve the user's language and meaning. Never invent information.
- Do not put checklist items or tags inside content.
- Use both content and checklist only when both descriptive information and actionable tasks are provided.

After calling the tool, confirm success only if the tool returns success=true. Otherwise, explain the failure.

Be concise and follow the user's instructions.

""".trimIndent()
}