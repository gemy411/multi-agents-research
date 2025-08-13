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
            prompt = Prompt.build("research-sub-agent-${System.currentTimeMillis()}") {
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
    val result = agent.run("""
        Your task is to identify Android games similar to BombSquad. To do this, first, research the core gameplay 
        mechanics and distinctive features of BombSquad. Focus on aspects like multiplayer (local, online), 
        genre (party game, action, fighting), physics-based gameplay, chaotic nature, minigames, and overall "feel."

        Once you have a clear understanding of BombSquad, use this information to search for Android games that share 
        these characteristics. Aim to find at least 5-7 potential games. For each potential game, provide a brief 
        description highlighting its key features and how it relates to BombSquad's gameplay elements.

        Prioritize sources like reputable gaming review sites (e.g., Pocket Gamer, Android Authority, IGN), app store 
        descriptions, and user reviews on Google Play. Do not list PC or console games, only Android.

        Your final output should be a list of these potential games, each with a concise summary of its similarity to BombSquad....
    """.trimIndent())
    val print = """
        ***************==========Agent Result======================***************
        $result
        ***************==========Agent Result======================***************
    """.trimIndent()
    println(print)
}