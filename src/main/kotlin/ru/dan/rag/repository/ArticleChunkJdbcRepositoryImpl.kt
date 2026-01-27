package ru.dan.rag.repository

import java.sql.ResultSet
import java.util.*
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import ru.dan.rag.entity.ArticleChunk
import ru.dan.rag.model.ChunkForProcessing

/**
 * Реализация методов для работы с чанками.
 */
@Repository
class ArticleChunkJdbcRepositoryImpl (
    private val jdbcTemplate: JdbcTemplate
) : ArticleChunkJdbcRepository {

    /**
     * Пакетная вставка для чанк.
     */
    override fun batchInsert(elements: List<ArticleChunk>) {

        jdbcTemplate.batchUpdate(
            """
            INSERT INTO article_chunks (
                id,
                article_id,
                chunk_index,
                text_for_search,
                processing_status,
                source_element_ids,
                chunk_metadata
            ) VALUES (
                ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb
            )
            """.trimIndent(),
            elements,
            elements.size
        ) { ps, element ->
            ps.setObject(1, element.id)
            ps.setObject(2, element.articleId)
            ps.setInt(3, element.chunkIndex)
            ps.setString(4, element.textForSearch)
            ps.setString(5, element.processingStatus)
            ps.setString(6, element.sourceElementIds)
            ps.setString(7, element.chunkMetadata)
        }
    }

    /**
     * Поиск необработанных чанк.
     */
    override fun findPendingChunks(limit: Int): List<ChunkForProcessing> {
        val sql = """
            SELECT id, text_for_search 
            FROM article_chunks 
            WHERE processing_status = 'PENDING' 
            ORDER BY created_at 
            LIMIT ?
        """.trimIndent()

        return jdbcTemplate.query(sql, arrayOf(limit), chunkForProcessingRowMapper)
    }

    /**
     * Обновление чанк вектором.
     */
    override fun updateWithEmbedding(chunkId: UUID, embedding: List<Float>) {
        val sql = """
            UPDATE article_chunks 
            SET 
                embedding = ?::vector,
                processing_status = 'COMPLETED',
                processed_at = CURRENT_TIMESTAMP,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """.trimIndent()

        val embeddingString = embedding.joinToString(", ", "[", "]")

        jdbcTemplate.update(sql, embeddingString, chunkId)
    }

    private val chunkForProcessingRowMapper = RowMapper { rs: ResultSet, _ ->
        ChunkForProcessing(
            id = rs.getObject("id") as UUID,
            text = rs.getString("text_for_search")
        )
    }

}