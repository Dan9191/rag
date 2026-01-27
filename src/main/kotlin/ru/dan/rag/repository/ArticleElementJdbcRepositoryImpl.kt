package ru.dan.rag.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import ru.dan.rag.entity.ArticleElement

@Repository
class ArticleElementJdbcRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate
) : ArticleElementJdbcRepository {

    override fun batchInsert(elements: List<ArticleElement>) {

        jdbcTemplate.batchUpdate(
            """
            INSERT INTO article_elements (
                id,
                article_id,
                element_index,
                element_type,
                content,
                items,
                metadata,
                created_at
            ) VALUES (
                ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?
            )
            """.trimIndent(),
            elements,
            elements.size
        ) { ps, element ->
            ps.setObject(1, element.id)
            ps.setObject(2, element.articleId)
            ps.setInt(3, element.elementIndex)
            ps.setString(4, element.elementType)
            ps.setString(5, element.content)
            ps.setString(6, element.items)
            ps.setString(7, element.metadata)
            ps.setObject(8, element.createdAt)
        }
    }
}