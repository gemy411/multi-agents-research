package com.gemy.agents.models

import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel

val geminiFlashModel = LLModel(
    provider = LLMProvider.OpenRouter,
    id = "google/gemini-2.5-flash",
    capabilities = listOf(
        LLMCapability.Temperature,
        LLMCapability.Schema.JSON.Full,
        LLMCapability.Speculation,
        LLMCapability.Tools,
        LLMCapability.ToolChoice,
        LLMCapability.Completion
    )
)
val sonarModel = LLModel(
    provider = LLMProvider.OpenRouter,
    id = "perplexity/sonar",
    capabilities = listOf(
        LLMCapability.Temperature,
        LLMCapability.Schema.JSON.Full,
        LLMCapability.Speculation,
        LLMCapability.Tools,
        LLMCapability.ToolChoice,
        LLMCapability.Completion
    )
)