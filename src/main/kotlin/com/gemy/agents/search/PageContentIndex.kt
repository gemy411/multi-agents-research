package com.gemy.agents.search

import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("PageContentIndex")
data class PageContentIndex(
    @property:LLMDescription("A list containing the titles of the sections in a page")
    val sectionNames: List<String>,
) {
    companion object {
        val examples = listOf(
            PageContentIndex(
                listOf(
                    "Introduction to Subagents",
                    "Subagent Configuration",
                    "User vs Project Subagents",
                    "Creating Custom Subagents",
                    "Using Subagents Automatically",
                    "Explicitly Requesting Subagents",
                )
            ),
            PageContentIndex(
                listOf(
                    "The definitions of function programming",
                    "Usages of function programming",
                    "Example of functional programming in a banking system",
                    "Example of functional programming in a restaurant management system",
                )
            )
        )
    }
}