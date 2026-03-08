package ru.dan.rag.client

import java.util.*
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import ru.dan.rag.config.RagPropertiesConfig

private val logger = KotlinLogging.logger {}

@Component
class GigachatModelsClient(
    @Qualifier("gigachatRestTemplate") private val restTemplate: RestTemplate,
    private val ragPropertiesConfig: RagPropertiesConfig) {

    /**
     * Класс для обработки ответа с токеном доступа.
     */
    data class TokenResponse(
        val access_token: String,
        val expires_at: Long
    )

    /**
     * Классы для обработки ответа от embedding моделей.
     */
    data class EmbeddingResponse(
        val `object`: String,
        val model: String,
        val data: List<EmbeddingData>
    )

    data class EmbeddingData(
        val `object`: String,
        val index: Int,
        val embedding: List<Float>,
        val usage: Usage?
    )

    data class Usage(
        val prompt_tokens: Int
    )

    /**
     * Классы для обработки ответа от LLM моделей.
     */
    data class ChatRequest(
        val model: String,
        val messages: List<Message>,
        val stream: Boolean = false,
        val repetition_penalty: Double = 1.0
    )

    data class Message(
        val role: String,
        val content: String
    )

    data class ChatResponse(
        val choices: List<Choice>
    )

    data class Choice(
        val message: AssistantMessage
    )

    data class AssistantMessage(
        val role: String,
        val content: String
    )

    /**
     * Запрос для получения токена доступа.
     *
     * @return access token.
     */
    fun getAccessToken(): String? {
        return try {

            val body = LinkedMultiValueMap<String, String>().apply {
                add("scope", "GIGACHAT_API_PERS")
            }

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                add("Authorization", "Basic ${ragPropertiesConfig.gigachat.secretToken}")
                add("RqUID", UUID.randomUUID().toString())
            }

            val entity = HttpEntity(body, headers)

            val response = restTemplate.postForObject(
                ragPropertiesConfig.gigachat.tokenUrl,
                entity,
                TokenResponse::class.java
            )

            response?.access_token

        } catch (e: Exception) {
            logger.error("Error getting token", e)
            null
        }
    }

    /**
     * Метод для векторизации строки.
     *
     * @param text Строка для векторизации
     * @return Список чисел с плавающей точкой (Float) — векторизация текста
     */
    fun getVector(text: String): List<Float>? {
        try {
            val token = getAccessToken() ?: return null

            val requestBody = mapOf(
                "model" to ragPropertiesConfig.gigachat.embeddingModel,
                "input" to listOf(text)
            )
            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                add("Authorization", "Bearer $token")
            }

            val entity = HttpEntity(requestBody, headers)

            val response = restTemplate.postForObject(
                ragPropertiesConfig.gigachat.embeddingUrl,
                entity,
                EmbeddingResponse::class.java
            ) ?: return null

            return response.data.firstOrNull()?.embedding

        } catch (e: Exception) {
            logger.error("Failed to get embedding", e)
            return null
        }
    }

    /**
     * Метод для генерации ответа.
     *
     * @param messages результат векторного поиска в БД.
     * @return Сгенерированный ответ.
     */
    fun generateText(messages: List<Message>): String? {

        val token = getAccessToken() ?: return null

        val requestBody = ChatRequest(
            model = ragPropertiesConfig.gigachat.llmModel,
            messages = messages
        )

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(token)
        }

        val entity = HttpEntity(requestBody, headers)

        val response = restTemplate.postForObject(
            ragPropertiesConfig.gigachat.llmUrl,
            entity,
            ChatResponse::class.java
        ) ?: return null

        return response.choices.firstOrNull()?.message?.content
    }
}
