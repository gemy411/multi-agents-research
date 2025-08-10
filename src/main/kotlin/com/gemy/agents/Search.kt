package com.gemy.agents

import ai.koog.agents.core.agent.AIAgent
import com.gemy.agents.models.OpenRouterConfig.openRouterExecutor
import com.gemy.agents.models.gpt4oMiniSearch
import com.gemy.agents.models.sonarModel
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        val pplxAgent = AIAgent(
            executor = openRouterExecutor,
            llmModel = sonarModel
        )
        val result = pplxAgent.run("What is the weather in NY and SF")
        println(result)
    }
}
suspend fun runSearchQuery(query: String): String {
    return AIAgent(
        executor = openRouterExecutor,
        systemPrompt = "Only reply with the result of the query",
        llmModel = gpt4oMiniSearch,
        temperature = 0.0,
    ).run(query)
}