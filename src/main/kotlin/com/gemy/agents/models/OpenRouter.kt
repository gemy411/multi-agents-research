package com.gemy.agents.models

import ai.koog.prompt.executor.llms.all.simpleOpenRouterExecutor
import io.github.cdimascio.dotenv.Dotenv

object OpenRouterConfig {
    private val dotenv = Dotenv.load()
    private val apiKey = dotenv.get("OPEN_ROUTER_API_KEY") ?: throw IllegalArgumentException("OPEN_ROUTER_API_KEY not found in .env file")
    // Use the OpenAI executor with an API key from an environment variable
    val openRouterExecutor = simpleOpenRouterExecutor(apiKey)
}