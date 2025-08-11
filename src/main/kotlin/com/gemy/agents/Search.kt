package com.gemy.agents

import ai.koog.agents.core.agent.AIAgent
import com.gemy.agents.models.OpenRouterConfig.openRouterExecutor
import com.gemy.agents.models.sonarModel
import com.gemy.agents.models.sonarProModel
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
suspend fun runSearchQuery(query: String, deep: Boolean): String {
    return AIAgent(
        executor = openRouterExecutor,
        systemPrompt = """Your are a helpful researcher, you reply with the most concise ways,
             The reply should NEVER be more than 300 words
        """.trimIndent(),
        llmModel = if (deep) sonarProModel else sonarModel,
//        maxIterations = 1,
        temperature = 0.7,
    ).run(query)
}