package com.gemy.agents

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.*
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.tools
import ai.koog.prompt.dsl.Prompt
import com.gemy.agents.models.OpenRouterConfig.openRouterExecutor
import com.gemy.agents.models.geminiFlashModel
import com.gemy.agents.tools.ResearchSubagentTools

class ResearchSubagentFactory() {
    fun getAgent(): AIAgent<String, String> {
        val completeTaskTool = "Complete task"
        val strategy = strategy("Research-Subagent") {
            val sendInput by nodeLLMRequestMultiple("requestInput")
            val useTool by nodeExecuteMultipleTools(parallelTools = true)
            val sendToolResult by nodeLLMSendMultipleToolResults("sendToolResult")
            edge(nodeStart forwardTo sendInput)
            edge(sendInput forwardTo useTool onMultipleToolCalls {true})
            edge(useTool forwardTo sendToolResult)
            edge(sendToolResult forwardTo useTool onMultipleToolCalls { it.isNotEmpty() })
            edge(sendToolResult forwardTo nodeFinish onMultipleAssistantMessages {true} transformed { it.joinToString { it.content }})
        }
        val toolRegistry = ToolRegistry {
            tools(ResearchSubagentTools())
        }
        val config = AIAgentConfig(
            prompt = Prompt.build("research-sub-agent") {
                system(PromptFactory().getResearchSubagentPrompt(completeTaskToolName = completeTaskTool))
            },
            model = geminiFlashModel,
            maxAgentIterations = 30,
        )
        val agent = AIAgent(
            promptExecutor = openRouterExecutor,
            toolRegistry = toolRegistry,
            agentConfig = config,
            strategy = strategy,
        )
        return agent
    }

}

suspend fun main() {
    val agent = ResearchSubagentFactory().getAgent()
    val result = agent.run("5 android games with multiplayer for friends")
    val print = """
        ***************==========Agent Result======================***************
        $result
        ***************==========Agent Result======================***************
    """.trimIndent()
    println(print)
}