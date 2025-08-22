package com.gemy.agents.search.sonar

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.structure.executeStructured
import ai.koog.prompt.structure.json.JsonSchemaGenerator
import ai.koog.prompt.structure.json.JsonStructuredData
import com.gemy.agents.models.OpenRouterConfig
import com.gemy.agents.models.geminiFlashModel
import com.gemy.agents.models.geminiProModel
import com.gemy.agents.search.ISearchManager
import com.gemy.agents.search.PageContentIndex
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.json.Json

class SonarSearchManager(): ISearchManager {
    private val webFetchMode: SonarWebFetchMode = SonarWebFetchMode.DirectHit
    private val apiKey = OpenRouterConfig.apiKey
    private val httpClient = OpenRouterConfig.httpClient.config {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        defaultRequest {
            url("https://openrouter.ai/api/v1/chat/completions")
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $apiKey")
        }
    }
    override suspend fun search(query: String): String {
        val params = ChatParams(
            maxTokens = 1500,
            temperature = 0.7,
            model = "perplexity/sonar",
        )
        val req = OpenRouterRequest(
            model = params.model,
            messages = listOf(ChatMessage("user", query)),
            temperature = params.temperature,
            max_tokens = params.maxTokens
        )
        return runCatching {
            httpClient.post {
              setBody(req)
            }.body<OpenRouterResponse>().messageWithCitations()
        }.getOrElse { "Error while fetching from Sonar" }
    }

    override suspend fun fetchWebsite(url: String): String {
        val messages = buildMessages(url, webFetchMode)
        val req = OpenRouterRequest(
            model = "perplexity/sonar",
            messages = messages,
            temperature = 0.1,
            max_tokens = 3000,
        )
        val apiRes = httpClient.post { setBody(req) }.body<OpenRouterResponse>()
        val result = when(webFetchMode) {
            SonarWebFetchMode.DirectHit -> {
                apiRes.messageWithCitations(false)
            }
            SonarWebFetchMode.SectionBased -> {
                val sections = indexPageSections(apiRes.messages())
                fetchPageSectionsContent(url, sections)
            }
        }
        return result.trim()
    }
    //region private
    /**
     * Build the messages payload expected by OpenRouter for the requested task.
     *
     * - For normal search queries: passes the `input` directly as the user message.
     * - For page indexing: sets brief system/assistant priming and asks for a 2-4 section array.
     */
    private fun buildMessages(url: String, mode: SonarWebFetchMode): List<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()

        when(mode) {
            SonarWebFetchMode.DirectHit -> {
                messages.add(ChatMessage("system", "Follow user's instructions"))
                messages.add(ChatMessage("assistant", "The synthesized content is:"))
                messages.add(ChatMessage("user", "Synthesize a MD page from: $url"))
            }
            SonarWebFetchMode.SectionBased -> {
                messages.add(ChatMessage("system", "Follow user's instructions"))
                messages.add(ChatMessage("assistant", "The page index array is:"))
                messages.add(ChatMessage("user",
                    """For $url: If this page's content can be divided into sections in a Json array of section
                        titles What will the array be?""".trimIndent()
                ))
            }
        }
        return messages
    }
    /**
     * Build a structured index of page sections for a given URL using the Koog structured execution.
     * Returns up to 4 section titles.
     */
    private suspend fun indexPageSections(content: String): List<String> {
        val index = OpenRouterConfig.openRouterExecutor.executeStructured(
            prompt = prompt("structured-data") {
                system(
                    """You are a page index extractor, You will digest string content to derive a list of 
                        section titles in a page.
                """.trimIndent()
                )
                user("Process $content")
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
                val params = ChatParams(
                    maxTokens = 1000,
                    temperature = 0.7,
                    model = "perplexity/sonar",
                )
                val req = OpenRouterRequest(
                    model = params.model,
                    messages = listOf(
                        ChatMessage("system", "Follow user's instructions"),
                        ChatMessage("user", """If the contents of the page $url are divided into this list of sections: $sectionNames
                            What will be the full contents of the section: $name?
                """.trimIndent())),
                    temperature = params.temperature,
                    max_tokens = params.maxTokens
                )
                httpClient.post { setBody(req) }.body<OpenRouterResponse>().messageWithCitations(false)
            }
        }.awaitAll().joinToString("\n\n")
    }

    //endregion
}

suspend fun main() {
    val manager = SonarSearchManager()
//    val result = manager.search("What is the weather in NY and SF")
    val result = manager.fetchWebsite("https://android-developers.googleblog.com/2025/08/media3-180-whats-new.html?m=1")
    println(result)
}