package ru.dan.rag.repository

import java.util.*
import org.springframework.data.repository.CrudRepository
import ru.dan.rag.entity.Article

/**
 * Репозиторий для работы со статьями.
 */
interface ArticleRepository : CrudRepository<Article, UUID>, ArticleJdbcRepository {

    fun findByExternalArticleId(externalArticleId: String): Article?
}