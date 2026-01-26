package ru.dan.rag.entity

import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.*


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