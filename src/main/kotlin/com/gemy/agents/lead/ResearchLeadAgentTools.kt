package com.gemy.agents.lead

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.gemy.agents.subagent.ResearchSubagentFactory

@LLMDescription("tool set to satisfy the search query")
class ResearchLeadAgentTools: ToolSet {

    @Tool
    @LLMDescription("Deploy subagent to create a subagent to work on a research task")
    suspend fun deploySubAgent(prompt: String): String {
        println("LeadAgent Tool run: Deploying subagent with prompt $prompt...")
        return ResearchSubagentFactory().getAgent().run(prompt)
    }

    @Tool
    @LLMDescription("complete_task_tool: a tool that's called when the task is completed")
    fun completeTask(
        @LLMDescription("The result of the task")
        result: String
    ): String {
        println("Subagent Tool run: Task completed! Result: $result")
        return "Task completed!"
    }
}