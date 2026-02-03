package ru.dan.rag.entity

import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

/**
 * Оригинальная статья.
 */
@Table(name = "articles")
data class Article(
    @Id
    val id: UUID,

    val externalArticleId: String,
    val title: String,
    @Column("original_content")
    val originalContent: String,
    val metadata: String?,

    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null
)