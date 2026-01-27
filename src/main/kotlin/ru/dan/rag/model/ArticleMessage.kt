package ru.dan.rag.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Модель статьи для чтения из брокера.
 */
data class ArticleMessage (
    @JsonProperty("id")
    val id: String,
    @JsonProperty("title")
    val title: String,
    @JsonProperty("content")
    val content: String,
    @JsonProperty("metadata")
    val metadata: String,
)