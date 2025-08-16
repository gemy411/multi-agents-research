package com.gemy.agents.search

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
import kotlinx.serialization.json.*

fun main() {
    runBlocking {

//        val anotherQuery = runSearchQuery("https://proandroiddev.com/layoutnode-what-actually-happens-when-you-write-composable-05c0275875fa", true)
        val url = "https://proandroiddev.com/kotlins-requiresoptin-a-hidden-safety-net-you-probably-ignore-fde858a73188"
        val anotherQuery = runSearchQuery(url, true)
        println("\nSearch file test: getting contents:")
        println(anotherQuery)
    }
}
/**
 * Run a search query.
 *
 * - When getPage=false: performs a normal search Q&A against OpenRouter and appends citations.
 * - When getPage=true: derives page sections and fetches each section's content; citations are omitted.
 *
 * @param input the text query or page URL.
 * @param getPage whether to get page section contents instead of a normal answer.
 * @param withCitation include citations for normal queries.
 */
suspend fun runSearchQuery(input: String, getPage: Boolean, withCitation: Boolean = true): String {
    if (!getPage) {
        val json = runDirectOpenRouterQuery(input, getPage = false)
        val content = extractAssistantContent(json)
        val citations = extractCitations(json)
        return appendCitations(content, citations, enabled = withCitation)
    }

    val json = runDirectOpenRouterQuery(input, getPage = true)
    val content = extractAssistantContent(json)
    val sections = indexPageSections(content)
    println("Search file test: the obj is $sections")
    return fetchPageSectionsContent(input, sections)
}

//region private
/**
 * Parameters used for an OpenRouter chat completion request.
 */
private data class ChatParams(
    val maxTokens: Int,
    val temperature: Double,
    val model: String,
)

/**
 * Compute request parameters based on whether this is a page-content task or a normal query.
 *
 * @param getPage true if the intent is to extract page structure/content; false for normal search Q&A.
 */
private fun chatParamsFor(getPage: Boolean): ChatParams =
    ChatParams(
        maxTokens = if (getPage) 1500 else 1500,
        temperature = if (getPage) 0.0 else 0.7,
        model = if (getPage) "perplexity/sonar" else "perplexity/sonar",
    )

/**
 * Build the messages payload expected by OpenRouter for the requested task.
 *
 * - For normal search queries: passes the `input` directly as the user message.
 * - For page indexing: sets brief system/assistant priming and asks for a 2-4 section array.
 */
private fun buildMessages(input: String, getPage: Boolean): JsonArray = buildJsonArray {
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
        put(
            "content",
            if (!getPage) input else """
                For $input: If this page's content can be divided into 2-4 sections in a Json array of section titles
                What will the array be?
            """.trimIndent()
        )
    }
}

/**
 * Execute a direct OpenRouter chat completion call.
 *
 * @param input the user query or page URL.
 * @param getPage whether the request is for page indexing guidance.
 * @return raw JSON response as JsonObject.
 */
private suspend fun runDirectOpenRouterQuery(
    input: String,
    getPage: Boolean = false,
): JsonObject {
    val apiKey = OpenRouterConfig.apiKey
    val url = "https://openrouter.ai/api/v1/chat/completions"

    val params = chatParamsFor(getPage)
    val response = httpClient.post(url) {
        contentType(ContentType.Application.Json)
        header("Authorization", "Bearer $apiKey")
        parameters { append("max_tokens", params.maxTokens.toString()) }
        setBody(
            buildJsonObject {
                put("temperature", params.temperature)
                put("model", params.model)
                put("max_tokens", params.maxTokens)
                put("messages", buildMessages(input, getPage))
            }.toString()
        )
    }

    val responseText = response.bodyAsText()
    return Json.parseToJsonElement(responseText).jsonObject
}

/**
 * Extract assistant textual content from an OpenRouter response.
 */
private fun extractAssistantContent(json: JsonObject): String {
    val choices = json["choices"]?.jsonArray
    val message = choices?.getOrNull(0)?.jsonObject?.get("message")?.jsonObject
    return message?.get("content")?.jsonPrimitive?.content ?: "No response content found"
}

/**
 * Extract citations URLs, if present, from an OpenRouter response.
 */
private fun extractCitations(json: JsonObject): List<String> =
    json["citations"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()

/**
 * Append formatted citations to the content if enabled and available.
 */
private fun appendCitations(content: String, citations: List<String>, enabled: Boolean): String {
    if (!enabled || citations.isEmpty()) return content
    val citationsText = citations.mapIndexed { ind, url -> "${ind + 1}. $url" }.joinToString("\n")
    return "$content\n\nCitations:\n$citationsText"
}

/**
 * Build a structured index of page sections for a given URL using the Koog structured execution.
 * Returns up to 4 section titles.
 */
private suspend fun indexPageSections(url: String): List<String> {
    val index = OpenRouterConfig.openRouterExecutor.executeStructured(
        prompt = prompt("structured-data") {
            system(
                """You are a page index extractor, You will digest string content to derive a list of 
                        section titles in a page. Do NOT return more than 4 sections.
                """.trimIndent()
            )
            user("Process $url")
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
    return index.getOrThrow().structure.sectionNames
}

/**
 * For each section name, fetch the full text content by prompting the model with the URL and section name.
 * Sections are fetched concurrently and concatenated.
 */
private suspend fun fetchPageSectionsContent(url: String, sectionNames: List<String>): String = supervisorScope {
    sectionNames.map { name ->
        async {
            runSearchQuery(
                input = """If the contents of the page $url are divided into this list of sections: $sectionNames
                            What will be the full contents of the section: $name?
                """.trimIndent(),
                getPage = false,
                withCitation = false,
            )
        }
    }.awaitAll().joinToString("\n\n")
}
//endregion