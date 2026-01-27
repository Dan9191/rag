package ru.dan.rag.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import ru.dan.rag.entity.ArticleChunk

@Repository
class ArticleChunkJdbcRepositoryImpl (
    private val jdbcTemplate: JdbcTemplate
) : ArticleChunkJdbcRepository {

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

}