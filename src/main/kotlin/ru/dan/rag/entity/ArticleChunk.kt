package ru.dan.rag.entity

import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

/**
 * Чанка.
 */
@Table(name = "article_chunks")
data class ArticleChunk(
    @Id
    val id: UUID,
    val articleId: UUID,
    val chunkIndex: Int,
    val textForSearch: String,
    val processingStatus: String = "PENDING",
    val processingAttempts: Int = 0,
    val lastAttemptAt: OffsetDateTime? = null,
    val processedAt: OffsetDateTime? = null,
    val chunkMetadata: String?,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null
)