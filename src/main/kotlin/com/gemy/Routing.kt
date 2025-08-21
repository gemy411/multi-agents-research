package com.gemy

import com.gemy.agents.lead.LeadResearchAgentFactory
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/search") {
            val query = call.request.queryParameters["query"]
            if (query.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Missing or empty 'query' parameter")
                return@get
            }
            
            try {
                val agent = LeadResearchAgentFactory().getAgent()
                val result = agent.run(query)
                val response = result.trim()
                println(response)
                call.respondText(response)
            } catch (e: Exception) {
                println("Error processing query: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, "Error processing query: ${e.message}")
            }
        }
        
        get("/") {
            call.respondText("Research Agent API - Use /search?query=<your_query> to search")
        }
    }
}
