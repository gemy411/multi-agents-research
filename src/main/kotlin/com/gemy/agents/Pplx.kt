package com.gemy.agents

import ai.koog.agents.core.agent.AIAgent
import com.gemy.agents.models.OpenRouterConfig.openRouterExecutor
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