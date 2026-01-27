package ru.dan.rag.entity

import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

/**
 * Блок из статьи.
 */
@Table(name = "article_elements")
data class ArticleElement(
    @Id
    val id: UUID,

    val articleId: UUID,
    val elementIndex: Int,
    val elementType: String,

    val content: String?,
    val items: String?,     // JSONB
    val metadata: String?,  // JSONB

    val createdAt: OffsetDateTime? = null
)