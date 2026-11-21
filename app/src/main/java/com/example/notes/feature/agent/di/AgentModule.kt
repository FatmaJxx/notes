package com.example.notes.feature.agent.di

import com.example.notes.feature.agent.AgentViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val agentViewModelModule = module {
    viewModelOf(::AgentViewModel)
}