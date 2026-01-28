package ru.dan.rag.model.answer

/**
 * Результат поиска.
 */
class SearchResult (
    val chunkId: String,
    val text: String,
    val articleId: String? = null,
    val articleTitle: String? = null,
    val similarity: Double? = null
)

data class SearchResponse(
    val results: List<SearchResult>
)