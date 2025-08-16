package com.gemy

import com.gemy.agents.lead.LeadResearchAgentFactory
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            val agent = LeadResearchAgentFactory().getAgent()
            val result = agent.run("Websites like OpenRouter for LLM proxy-ing, find the top 5")
            val print = """
        ***************==========Agent Result======================***************
        $result
        ***************==========Agent Result======================***************
    """.trimIndent()
            println(print)
            call.respondText(print)
        }
    }
}
