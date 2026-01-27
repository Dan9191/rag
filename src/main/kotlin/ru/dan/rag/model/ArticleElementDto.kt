package ru.dan.rag.model

import com.fasterxml.jackson.annotation.JsonProperty

data class ArticleElementDto(
    @JsonProperty("type")
    val type: String,

    @JsonProperty("content")
    val content: String? = null,

    @JsonProperty("items")
    val items: List<String>? = null
)
