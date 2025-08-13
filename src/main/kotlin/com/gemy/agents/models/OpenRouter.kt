package com.gemy.agents.models

import ai.koog.prompt.executor.clients.ConnectionTimeoutConfig
import ai.koog.prompt.executor.clients.openrouter.OpenRouterClientSettings
import ai.koog.prompt.executor.clients.openrouter.OpenRouterLLMClient
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers

object OpenRouterConfig {
    private val dotenv = Dotenv.load()
    val apiKey = dotenv.get("OPEN_ROUTER_API_KEY") ?: throw IllegalArgumentException("OPEN_ROUTER_API_KEY not found in .env file")
    // Use the OpenAI executor with an API key from an environment variable
    val httpClient = HttpClient(CIO) {
        defaultRequest {
            parameters {
                this.append("max_tokens", "3000")
            }
        }
        engine {
            dispatcher = Dispatchers.IO
            endpoint {
                maxConnectionsPerRoute = 128
                connectTimeout = 50_000
                keepAliveTime = 50_000
                connectAttempts = 4
            }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 60_000
        }
        install(HttpRequestRetry) {
            retryOnExceptionOrServerErrors(
                maxRetries = 4,
            )
            exponentialDelay(
                base = 2.0,
                baseDelayMs = 100L,
                maxDelayMs = 1_000,
            )
            modifyRequest { it.headers.append("X-Retry", retryCount.toString()) }
        }
        
//        install(ContentNegotiation) {
//            json(Json {
//                prettyPrint = true
//                isLenient = true
//                ignoreUnknownKeys = true
//            })
//        }
    }
    private val client = OpenRouterLLMClient(
        apiKey = apiKey,
        settings = OpenRouterClientSettings(timeoutConfig = ConnectionTimeoutConfig()),
        baseClient = httpClient,
    )
    val openRouterExecutor = SingleLLMPromptExecutor(client)
}