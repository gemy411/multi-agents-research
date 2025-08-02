package com.gemy.agents

import ai.koog.prompt.executor.llms.all.simpleOpenRouterExecutor
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import io.github.cdimascio.dotenv.Dotenv

private val dotenv = Dotenv.load()
private val apiKey = dotenv.get("OPEN_ROUTER_API_KEY") ?: throw IllegalArgumentException("OPEN_ROUTER_API_KEY not found in .env file")
// Use the OpenAI executor with an API key from an environment variable
val openRouterExecutor = simpleOpenRouterExecutor(apiKey)

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