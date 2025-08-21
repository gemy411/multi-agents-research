package com.gemy.agents.search.sonar

import kotlinx.serialization.Serializable

/**
 * Parameters used for an OpenRouter chat completion request.
 */
data class ChatParams(
    val maxTokens: Int,
    val temperature: Double,
    val model: String,
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenRouterRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double,
    val max_tokens: Int
)

@Serializable
data class OpenRouterMessage(
    val content: String,
    val role: String
)

@Serializable
data class OpenRouterChoice(
    val message: OpenRouterMessage
)

@Serializable
data class OpenRouterResponse(
    val choices: List<OpenRouterChoice>,
    val citations: List<String>? = null
) {
    fun messages() = buildString {
        choices.forEach { it.message.content.let { content -> appendLine(content)} }
    }
    fun messageWithCitations(enabled: Boolean = true) = buildString {
        append(messages())
        if (enabled) {
            if (!citations.isNullOrEmpty()) appendLine("Citations:")
            citations?.forEachIndexed { ind, url -> appendLine("${ind + 1}. $url") }
        }
    }
}