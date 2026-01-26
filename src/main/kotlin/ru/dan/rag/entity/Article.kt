package ru.dan.rag.entity

import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.*

@Table(name = "articles")
data class Article(
    @Id
    val id: UUID,

    val externalArticleId: String,
    val title: String,
    val originalJson: String, // JSONB → String (Jackson на сервисном уровне)
    val metadata: String?,

    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null
)