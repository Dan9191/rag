package ru.dan.rag.model

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.util.*

/**
 * Модель сообщения для отправки в RabbitMQ
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ArticleMessage(
    val id: UUID,
    val articleId: UUID,
    val articleName: String,
    val eventType: String,
    val body: String,
    val source: String?
)
