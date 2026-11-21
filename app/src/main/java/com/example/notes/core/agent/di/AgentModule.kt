package com.example.notes.core.agent.di

import com.example.notes.core.agent.chat.data.LiteRtLlmEngine
import com.example.notes.core.agent.chat.data.ModelDownloader
import com.example.notes.core.agent.chat.data.modelPath
import com.example.notes.core.agent.chat.domain.LlmEngine
import com.example.notes.core.agent.chat.tools.CreateNoteTool
import com.example.notes.core.agent.chat.tools.DeleteNoteTool
import com.example.notes.core.agent.chat.tools.SearchNoteTool
import com.example.notes.core.agent.speech.VoiceInputManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpTimeoutConfig
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val agentModule = module {
    single<LlmEngine> {
        LiteRtLlmEngine(
            tools = listOf(
                get<CreateNoteTool>(), get<SearchNoteTool>(),
                get<DeleteNoteTool>()
            ),
            modelDownloader = get(),
            path = modelPath(androidContext())
        )
    }
    single { CreateNoteTool(get()) }
    single { SearchNoteTool(get()) }
    single { DeleteNoteTool(get()) }
    single { ModelDownloader(get()) }
    single {
        HttpClient(OkHttp) {
            install(HttpTimeout) {
                connectTimeoutMillis = 30_000
                requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                socketTimeoutMillis = 300_000
            }

            engine {
                config {
                    followRedirects(true)
                    connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    readTimeout(300, java.util.concurrent.TimeUnit.SECONDS)
                }
            }
        }
    }

    single {
        VoiceInputManager()
    }
}