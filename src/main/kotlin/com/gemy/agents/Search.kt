package com.gemy.agents

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.structure.executeStructured
import ai.koog.prompt.structure.json.JsonSchemaGenerator
import ai.koog.prompt.structure.json.JsonStructuredData
import com.gemy.agents.models.OpenRouterConfig
import com.gemy.agents.models.OpenRouterConfig.httpClient
import com.gemy.agents.models.geminiFlashModel
import com.gemy.agents.models.geminiProModel
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

fun main() {
    runBlocking {

//        val anotherQuery = runSearchQuery("https://proandroiddev.com/layoutnode-what-actually-happens-when-you-write-composable-05c0275875fa", true)
        val url = "https://docs.anthropic.com/en/docs/claude-code/sub-agents"
        val anotherQuery = runSearchQuery(url, true)
        println("\nSearch file test: getting contents:")
        println(anotherQuery)
        val index = OpenRouterConfig.openRouterExecutor.executeStructured(
            prompt = prompt("structured-data") {
                system(
                    """You are a page index extractor, You will digest string content to derive a list of 
                        section titles in a page.
                    """.trimIndent())
                user(
                    "Process $anotherQuery"
                )
            },
            structure = JsonStructuredData.createJsonStructure<PageContentIndex>(
                schemaFormat = JsonSchemaGenerator.SchemaFormat.JsonSchema,
                examples = PageContentIndex.examples,
                schemaType = JsonStructuredData.JsonSchemaType.SIMPLE
            ),
            mainModel = geminiFlashModel,
            retries = 2,
            fixingModel = geminiProModel,
        )
        val fullContent = index.getOrThrow().let { res ->
            println("\n the obj is $res")
            supervisorScope {
                res.structure.sectionNames.map { name ->
                    async { runSearchQuery(
                        input = """If the contents of the page $url are divided into this list of sections: ${res.structure.sectionNames}
                            What will be the full contents of the section $name?
                    """.trimIndent(),
                        getPage = false,
                        withCitation = false,
                    ) }
                }.awaitAll().joinToString("\n\n")
            }
        }
        println("\nFull page result is: \n\n")
        println(fullContent)
    }
}
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
private suspend fun runDirectOpenRouterQuery(
    input: String,
    getPage: Boolean = false,
): JsonObject {
    val apiKey = OpenRouterConfig.apiKey
    val url = "https://openrouter.ai/api/v1/chat/completions"

    val response = httpClient.post(url) {
        contentType(ContentType.Application.Json)
        header("Authorization", "Bearer $apiKey")
        val maxTokens = if (getPage) 1500 else 1500
        val temp = if (getPage) 0.0 else 0.7
        val model = if (getPage) "perplexity/sonar" else "perplexity/sonar"
        parameters {
            append("max_tokens", maxTokens.toString())
        }
        setBody(buildJsonObject {
            put("temperature", temp)
            put("model", model)
            put("max_tokens", maxTokens)
            putJsonArray("messages") {
                if (getPage) {
                    addJsonObject {
                        put("role", "system")
                        put("content", "Follow user's instructions")
                    }
                    addJsonObject {
                        put("role", "assistant")
                        put("content", "The page index array is:")
                    }
                }
                addJsonObject {
                    put("role", "user")
                    put("content", if (!getPage) input else """
                        For $input: If this page's content can be divided into at least 3-7 sections in a Json array of section titles
                        What will the array be?
                    """.trimIndent())
                }
            }
        }.toString())
    }

    val responseText = response.bodyAsText()
    return Json.parseToJsonElement(responseText).jsonObject
}

suspend fun runSearchQuery(input: String, getPage: Boolean, withCitation: Boolean = true): String {
    val jsonResponse = runDirectOpenRouterQuery(input, getPage)

    // Extract the assistant's message content
    val choices = jsonResponse["choices"]?.jsonArray
    val message = choices?.getOrNull(0)?.jsonObject?.get("message")?.jsonObject
    val content = message?.get("content")?.jsonPrimitive?.content ?: "No response content found"

    // Extract citations
    val citations = jsonResponse["citations"]?.jsonArray

    // Append citations to content if available
    val result = if (citations != null && citations.isNotEmpty() && !getPage && withCitation) {
        val citationsText = citations.mapIndexed { ind, str ->
            """
                ${ind+1}. ${str.jsonPrimitive.content}
            """.trimIndent()
        }.joinToString("\n")
        "$content\n\nCitations:\n$citationsText"
    } else {
        content
    }
    return result
}