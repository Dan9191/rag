package ru.dan.rag.model.answer

/**
 * Запрос от пользователя.
 */
data class SearchRequest(
    val query: String
)