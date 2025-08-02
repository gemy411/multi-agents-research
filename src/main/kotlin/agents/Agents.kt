package com.gemy.agents

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.*
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.tools
import ai.koog.agents.features.eventHandler.feature.EventHandler
import ai.koog.prompt.dsl.Prompt
import com.gemy.agents.tools.MockToolSet

val waitingPoc = strategy("Agent") {
    val sendInput by nodeLLMRequest()
    val useTool by nodeExecuteTool()
    val sendToolResult by nodeLLMSendToolResult()
    edge(nodeStart forwardTo sendInput)
    edge(sendInput forwardTo useTool onToolCall {true})
    edge(useTool forwardTo sendToolResult)
    edge(sendToolResult forwardTo useTool onToolCall {true})
    edge(sendToolResult forwardTo nodeFinish onAssistantMessage {true})
}
val toolRegistry = ToolRegistry{
    tools(MockToolSet())
}
val waitingAgentConfig = AIAgentConfig(
  prompt = Prompt.build("time-travelling-agent") {
      system("""
          You're a helpful assistant. You have access to a set of tools that you can use. You are also a time traveller.
          <tool-use>
          Actionable tools like (get weather) do not return the result immediately, but instead it will inform you that
          work has started asynchronously. You can use the checkProgress tool to check the progress of the work.
          You can use the timeTravel tool to skip for when the work to complete.
          
          When you are certain the work is done, call the checkProgress tool to get the work result
          <tool-use>
          
          <time-utilization> 
          During the work, you can think for a while, using the think tool, then check the progress using the 
          checkProgress tool. If you no longer want to think, you can skip to when the work is done by using the 
          timeTravel tool.
          
          Think AT LEAST once after each tool call 
          <time-utilization>
          
          You have the user's query and it will be mainly about the weather, the query can take a while to complete.  
          The query can have multiple parts, so, you will think while the first task is being done, think about how you 
          will handle the next request. Once done, check the progress, if the work is not done yet, use time travel.
      """.trimIndent())
  },
    model = geminiFlashModel,
    maxAgentIterations = 100,
)
val agent = AIAgent(
    promptExecutor = openRouterExecutor,
    toolRegistry = toolRegistry,
    agentConfig = waitingAgentConfig,
    strategy = waitingPoc,
    installFeatures = {
        install(EventHandler){
            onBeforeLLMCall {
                println("Event request: ${it.prompt.messages.drop(1).reversed().joinToString { it.content }}")
            }
            onAfterLLMCall {
                println("Event response: ${it.responses.filterNot { it.content.isBlank() }.joinToString { it.content }})")
            }
            onToolCall {
                val toolName = it.tool.name
                println("Event tool call: $toolName")
            }
        }
    }
)

suspend fun main() {
    val result = agent.run("What is the weather in NY and SF")
}