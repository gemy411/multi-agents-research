package com.gemy.agents.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.gemy.agents.runSearchQuery

@LLMDescription("tool set to satisfy the search query, only tools that are available are search, deep_search, think and complete_task")
    class ResearchSubagentTools: ToolSet {
        @Tool
        @LLMDescription("web_search tool: searches using the query and returns a natural language result")
        suspend fun webSearch(
            @LLMDescription("The query to get natural language results for")
            query: String,
        ): String {
            println("Subagent Tool run: Searching for $query...")
            val result = runSearchQuery(input = query, getPage = false)
            println("Subagent Tool run: Search result for query $query is: $result")
            return result
        }
        @Tool
        @LLMDescription("web_fetch tool: retriever clean content of web pages")
        suspend fun webFetch(
            @LLMDescription("The url to fetch the content of")
            url: String,
        ): String {
            println("Subagent Tool run: getting content of $url...")
            val result = runSearchQuery(input = url, getPage = false)
            println("Subagent Tool run: content of $url is: $result")
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