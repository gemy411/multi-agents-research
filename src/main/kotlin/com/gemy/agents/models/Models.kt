package com.gemy.agents.models

import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
private fun openRouterModel(name: String) = LLModel(
    provider = LLMProvider.OpenRouter,
    id = name,
    capabilities = listOf(
        LLMCapability.Temperature,
        LLMCapability.Schema.JSON.Full,
        LLMCapability.Speculation,
        LLMCapability.Tools,
        LLMCapability.ToolChoice,
        LLMCapability.Completion
    )
)
val geminiFlashModel = openRouterModel("google/gemini-2.5-flash")
val sonarModel = openRouterModel("perplexity/sonar")
val gptOss = openRouterModel("openai/gpt-oss-120b")
val gpt5Mini = openRouterModel("openai/gpt-5-mini")
val gpt4oMiniSearch = openRouterModel("openai/gpt-4o-mini-search-preview")

val placeholder = openRouterModel("deepseek/deepseek-chat-v3-0324")