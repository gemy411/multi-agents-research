package com.gemy.mcp

import com.gemy.agents.lead.LeadResearchAgentFactory
import io.ktor.server.application.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

fun Application.setupMcp() {
    mcp {
        Server(
            serverInfo = Implementation(name = "ResearchAgents", version = "0.0.1"),
            options = ServerOptions(
                capabilities = ServerCapabilities(
                    tools = ServerCapabilities.Tools(null),
                    logging = null
                )
            )
        ).apply {
            addTool(
                name = "DeepResearch",
                description = """
                        This tool uses a deep research LLM agent based management to answer complex and multi-factor queries.
                    """.trimIndent(),
                inputSchema = Tool.Input(properties = buildJsonObject {
                    putJsonObject("query") {
                        put("type", "string")
                        put("description", "The query to get deep search results for")
                    }
                }, required = listOf("query")),
                handler = { request ->
                    val query = request.arguments.getOrElse("query") { error("No query provided") }.toString()
                    try {
                        val agent = LeadResearchAgentFactory().getAgent()
                        val result = agent.run(query)
                        val response = result.trim()
                        println(response)
                        return@addTool CallToolResult(listOf(TextContent(response)))
                    } catch (e: Exception) {
                        println("Error processing query: ${e.message}")
                        return@addTool CallToolResult(
                            listOf(TextContent("Error processing query: ${e.message}")),
                            true
                        )
                    }
                }
            )
        }
    }
}