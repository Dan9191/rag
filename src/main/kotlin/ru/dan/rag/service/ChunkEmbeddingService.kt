package ru.dan.rag.service

import kotlin.math.sqrt
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.dan.rag.client.GigachatModelsClient
import ru.dan.rag.model.ChunkForProcessing
import ru.dan.rag.repository.ArticleChunkRepository

/**
 * Сервис работы с векторизацией.
 */
@Service
class ChunkEmbeddingService(
    private val articleChunkRepository: ArticleChunkRepository,
    private val gigaEmbeddingClient: GigachatModelsClient
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
        val response: List<Float> = gigaEmbeddingClient.getVector(text)
            ?: throw RuntimeException("Embedding service returned empty data list")

        return normalizeEmbedding(response)
    }

    /**
     * Нормализация вектора.
     */
    private fun normalizeEmbedding(embedding: List<Float>): List<Float> {
        val norm = sqrt(embedding.map { it * it }.sum())
        return embedding.map { it / norm }
    }
}