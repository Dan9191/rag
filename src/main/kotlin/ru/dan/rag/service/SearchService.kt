package ru.dan.rag.service

import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import ru.dan.rag.config.RagPropertiesConfig
import ru.dan.rag.model.answer.SearchRequest
import ru.dan.rag.model.answer.SearchResponse
import ru.dan.rag.model.answer.SearchResult

@Service
class SearchService (
    private val jdbcTemplate: JdbcTemplate,
    private val chunkEmbeddingService: ChunkEmbeddingService,
    private val ragPropertiesConfig: RagPropertiesConfig
) {

    private val log = LoggerFactory.getLogger(SearchService::class.java)

    fun search(request: SearchRequest): SearchResponse {
        log.info("Поиск по запросу: ${request.query}")

        val queryEmbedding = chunkEmbeddingService.fetchEmbedding(request.query)

        val results = findSimilarChunks(queryEmbedding, limit = 5, minSimilarity = ragPropertiesConfig.minSimilarity)

        return SearchResponse(results = results)
    }

    /**
     * Векторный поиск среди чанк по запросу пользователя
     */
    private fun findSimilarChunks(
        queryEmbedding: List<Float>,
        limit: Int,
        minSimilarity: Double
    ): List<SearchResult> {

        val embeddingString = queryEmbedding.joinToString(", ", "[", "]")

        val sql = """
            SELECT 
                c.id as chunk_id,
                c.text_for_search,
                c.chunk_index,
                a.id as article_id,
                a.title as article_title,
                1 - (c.embedding <=> ?::vector) AS similarity
            FROM article_chunks c
            JOIN articles a ON a.id = c.article_id
            WHERE 
                c.embedding IS NOT NULL
                AND (1 - (c.embedding <=> ?::vector)) >= ?
            ORDER BY c.embedding <=> ?::vector
            LIMIT ?
        """.trimIndent()

        val params = arrayOf(
            embeddingString,
            embeddingString,
            minSimilarity,
            embeddingString,
            limit
        )

        return jdbcTemplate.query(sql, params) { rs, _ ->
            SearchResult(
                chunkId = rs.getString("chunk_id"),
                text = rs.getString("text_for_search"),
                articleId = rs.getString("article_id"),
                articleTitle = rs.getString("article_title"),
                similarity = rs.getDouble("similarity")
            )
        }
    }
}