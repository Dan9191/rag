package ru.dan.rag.entity

import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.*

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

    val sourceElementIds: String, // JSONB
    val chunkMetadata: String?,   // JSONB

    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null
)