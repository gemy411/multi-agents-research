package com.gemy.agents

import com.gemy.agents.models.OpenRouterConfig
import com.gemy.agents.models.OpenRouterConfig.httpClient
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*

fun main() {
    runBlocking {

//        val contentWithCitations = runSearchQuery("What's twitter?", false)
//        println("\nSearch file test: Direct API call content with citations:")
//        println(contentWithCitations)
//
        val anotherQuery = runSearchQuery("https://proandroiddev.com/retrofit-magic-service-interface-8efd486c8996", true)
        println("\nSearch file test: getting contents:")
        println(anotherQuery)
    }
}
private suspend fun runDirectOpenRouterQuery(
    input: String,
    getPage: Boolean = false,
): JsonObject {
    val apiKey = OpenRouterConfig.apiKey
    val url = "https://openrouter.ai/api/v1/chat/completions"

    val response = httpClient.post(url) {
        contentType(ContentType.Application.Json)
        header("Authorization", "Bearer $apiKey")
//        parameters {
//            append("max_tokens", "15000")
//        }
        setBody(buildJsonObject {
            put("temperature", if (getPage) 0.0 else 0.7)
            put("model", "perplexity/sonar")
            if (getPage) put("max_tokens", "25000")
            putJsonArray("messages") {
                if (getPage) {
                    addJsonObject {
                        put("role", "system")
                        put("content", "Follow user's instructions")
                    }
                    addJsonObject {
                        put("role", "assistant")
                        put("content", "The full content of the url is:")
                    }
                }
                addJsonObject {
                    put("role", "user")
                    put("content", if (!getPage) input else """
                        re-write $input with all details + a summary at the end
                    """.trimIndent())
                }
            }
        }.toString())
    }

    val responseText = response.bodyAsText()
    return Json.parseToJsonElement(responseText).jsonObject
}

suspend fun runSearchQuery(input: String, getPage: Boolean): String {
    val jsonResponse = runDirectOpenRouterQuery(input, getPage)

    // Extract the assistant's message content
    val choices = jsonResponse["choices"]?.jsonArray
    val message = choices?.getOrNull(0)?.jsonObject?.get("message")?.jsonObject
    val content = message?.get("content")?.jsonPrimitive?.content ?: "No response content found"

    // Extract citations
    val citations = jsonResponse["citations"]?.jsonArray

    // Append citations to content if available
    return if (citations != null && citations.isNotEmpty() && !getPage) {
        val citationsText = citations.mapIndexed { ind, str ->
            """
                ${ind+1}. ${str.jsonPrimitive.content}
            """.trimIndent()
        }.joinToString("\n")
        "$content\n\nCitations:\n$citationsText"
    } else {
        content
    }
}