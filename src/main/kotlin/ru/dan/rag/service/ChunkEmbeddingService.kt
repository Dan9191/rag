package ru.dan.rag.service

import kotlin.math.sqrt
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import ru.dan.rag.config.RagPropertiesConfig
import ru.dan.rag.model.ChunkForProcessing
import ru.dan.rag.repository.ArticleChunkRepository

/**
 * Сервис наполнения чанков векторами.
 */
@Service
class ChunkEmbeddingService(
    private val articleChunkRepository: ArticleChunkRepository,
    private val ragPropertiesConfig: RagPropertiesConfig,
    private val embeddingRestTemplate: RestTemplate
) {

    private val log = LoggerFactory.getLogger(ChunkEmbeddingService::class.java)

    /**
     * Задача для отправки чанк на векторизацию.
     */
    @Scheduled(fixedDelayString = "#{@ragPropertiesConfig.embeddingDelay}")
    @Transactional
    fun processPendingChunks() {
        log.info("Starting processing of pending chunks")

        val pendingChunks = articleChunkRepository.findPendingChunks(100)

        if (pendingChunks.isEmpty()) {
            log.info("No pending chunks found")
            return
        }

        log.info("Found {} chunks to process", pendingChunks.size)

        for (chunk in pendingChunks) {
            processSingleChunk(chunk)
        }

        log.info("Finished processing chunks")
    }

    /**
     * Сохранение вектора в БД.
     */
    private fun processSingleChunk(chunk: ChunkForProcessing) {
        log.debug("Processing chunk with id={}", chunk.id)

        try {
            val embedding = fetchEmbedding(chunk.text)
            articleChunkRepository.updateWithEmbedding(chunk.id, embedding)
            log.debug("Chunk with id={} processed successfully", chunk.id)
        } catch (e: Exception) {
            log.error(
                "Failed to process chunk with id={}: {}",
                chunk.id,
                e.message,
                e
            )
        }
    }

    /**
     * Обращение к сервису векторизации.
     */
    fun fetchEmbedding(text: String): List<Float> {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }

        val requestBody = mapOf("input" to text)
        val request = HttpEntity(requestBody, headers)

        val response: ResponseEntity<Map<String, Any>> = try {
            embeddingRestTemplate.postForEntity(ragPropertiesConfig.embeddingServiceUrl, request)
        } catch (e: Exception) {
            throw RuntimeException("Error while calling embedding service", e)
        }

        if (!response.statusCode.is2xxSuccessful) {
            throw RuntimeException("Embedding service returned non-success status: ${response.statusCode}")
        }

        val responseBody = response.body ?: throw RuntimeException("Пустой ответ от сервиса векторизации")

        val data = responseBody["data"] as? List<Map<String, Any>>
            ?: throw RuntimeException("Embedding service returned empty response body")

        if (data.isEmpty()) {
            throw RuntimeException("Embedding service returned empty data list")
        }

        val firstEmbedding = data[0]
        val embeddingList = firstEmbedding["embedding"] as? List<Double>
            ?: throw RuntimeException("Invalid embedding format")

        return normalizeEmbedding(embeddingList.map { it.toFloat() })
    }

    /**
     * Нормализация вектора.
     */
    private fun normalizeEmbedding(embedding: List<Float>): List<Float> {
        val norm = sqrt(embedding.map { it * it }.sum())
        return embedding.map { it / norm }
    }
}