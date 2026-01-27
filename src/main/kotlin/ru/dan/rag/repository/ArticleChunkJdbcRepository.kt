package ru.dan.rag.repository

import java.util.*
import ru.dan.rag.entity.ArticleChunk
import ru.dan.rag.model.ChunkForProcessing

/**
 * Репозиторий для работы с чанками.
 */
interface ArticleChunkJdbcRepository {
    fun batchInsert(elements: List<ArticleChunk>)
    fun findPendingChunks(limit: Int): List<ChunkForProcessing>
    fun updateWithEmbedding(chunkId: UUID, embedding: List<Float>)
}