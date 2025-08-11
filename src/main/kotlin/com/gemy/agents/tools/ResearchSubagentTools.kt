package com.gemy.agents.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.gemy.agents.runSearchQuery

@LLMDescription("tool set to satisfy the search query, only tools that are available are search, deep_search, think and complete_task")
    class ResearchSubagentTools: ToolSet {
        @Tool
        @LLMDescription("search_tool: searches using the query and returns a natural language result")
        suspend fun search(
            @LLMDescription("The query to get natural language results for")
            query: String,
        ): String {
            println("Subagent Tool run: Searching for $query...")
            val result = runSearchQuery(query, deep = false)
            println("Subagent Tool run: Search result for query $query is: $result")
            return result
        }
        @Tool
        @LLMDescription("deep_search_tool: searches deeply using the query and returns a natural language result")
        suspend fun deepSearch(
            @LLMDescription("The query to get natural language results for")
            query: String,
        ): String {
            println("Subagent Tool run: Deep searching for $query...")
            val result = runSearchQuery(query, deep = true)
            println("Subagent Tool run: Deep search result for query $query is: $result")
            return result
        }

//        @Tool
//        @LLMDescription("Think for a while")
        suspend fun think(thoughts: String): String {
            println("Subagent Tool run: Thinking about $thoughts...")
            return thoughts
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