package com.gemy.agents

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import kotlinx.coroutines.*

@LLMDescription("toolset for satisfying multiple requests")
class MockToolSet: ToolSet {
    var workInProgress = false
    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    var lastResult = ""
    @Tool
    @LLMDescription("Get the weather for a city")
    suspend fun getWeather(
        @LLMDescription("The name of the city to get the weather for")
        city: String,
    ): String {
        println("Tool run: Getting weather for $city...")
        scope.launch {
            lastResult = ""
            workInProgress = true
            delay(5000)
            workInProgress = false
            lastResult = "It's raining in $city"
        }
        return "work started!"
    }
    @Tool
    @LLMDescription("Check the progress of the work, the result will be either work not done yet or tool result")
    fun checkProgress(): String {
        println("Tool run: Checking progress...")
        val result = if (workInProgress) "Work not done yet" else "Tool result is: $lastResult"
        return result
    }

    @Tool
    @LLMDescription("Think for a while")
    suspend fun think(thoughts: String): String {
        println("Tool run: Thinking about $thoughts...")
        return thoughts
    }

    @Tool
    @LLMDescription("Time travel to when the work is complete")
    suspend fun timeTravel(): String {
        println("Tool run: Time travelling...")
        while (workInProgress) {
            delay(500L)
        }
        return "Time has passed, now, check progress"
    }
}