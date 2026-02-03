package ru.dan.rag.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import ru.dan.rag.entity.Article

/**
 * Реализация методов для работы со статьями.
 */
@Repository
class ArticleJdbcRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate
) : ArticleJdbcRepository {

    override fun insert(article: Article) {
        jdbcTemplate.update(
            """
            INSERT INTO articles (
                id, external_article_id, title, original_content, metadata,
                created_at, updated_at
            ) VALUES (
                ?, ?, ?, ?, ?::jsonb, ?, ?
            )
            """.trimIndent(),
            article.id,
            article.externalArticleId,
            article.title,
            article.originalContent,
            article.metadata,
            article.createdAt,
            article.updatedAt
        )
    }
}