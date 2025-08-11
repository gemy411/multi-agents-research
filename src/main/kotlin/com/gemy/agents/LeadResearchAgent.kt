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
import com.gemy.agents.tools.ResearchLeadAgentTools

class LeadResearchAgentFactory() {
    fun getAgent(): AIAgent<String, String> {
        val completeTaskTool = "Complete task"
        val strategy = strategy("Lead-Research-Agent") {
            val sendInput by nodeLLMRequestMultiple("requestInput")
            val useTool by nodeExecuteMultipleTools(parallelTools = true)
            val sendToolResult by nodeLLMSendMultipleToolResults("sendToolResult")
            val compress by nodeLLMCompressHistory<String>(
                name = "Compress history",
                strategy = HistoryCompressionStrategy.WholeHistory,
                retrievalModel = geminiFlashModel
            )
            edge(nodeStart forwardTo sendInput)
            edge(sendInput forwardTo useTool onMultipleToolCalls {true})
            edge(useTool forwardTo sendToolResult)
            edge(sendToolResult forwardTo useTool onMultipleToolCalls { it.isNotEmpty() })
            edge(sendToolResult forwardTo compress onCondition { llm.readSession { prompt.messages.size > 20 }} transformed { it.joinToString { it.content }})
            edge(compress forwardTo sendInput)
            edge(sendToolResult forwardTo nodeFinish onMultipleAssistantMessages {true} transformed { it.joinToString { it.content }})
        }
        val toolRegistry = ToolRegistry {
            tools(ResearchLeadAgentTools())
        }
        val config = AIAgentConfig(
            prompt = Prompt.build("research-lead-agent") {
                system(PromptFactory().getLeadResearchPrompt(completeTaskToolName = completeTaskTool))
            },
            model = geminiFlashModel,
            maxAgentIterations = 50,
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
    val agent = LeadResearchAgentFactory().getAgent()
    val result = agent.run("Compare gemini flash 2.5, gemini pro 2.5 and gpt-5")
    val print = """
        ***************==========Agent Result======================***************
        $result
        ***************==========Agent Result======================***************
    """.trimIndent()
    println(print)
}