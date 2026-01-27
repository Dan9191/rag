package ru.dan.rag.repository

import java.util.*
import org.springframework.data.repository.CrudRepository
import ru.dan.rag.entity.ArticleElement

/**
 * Репозиторий для работы с блоками статьи.
 */
interface ArticleElementRepository : CrudRepository<ArticleElement, UUID>, ArticleElementJdbcRepository {

    fun findAllByArticleIdOrderByElementIndex(articleId: UUID): List<ArticleElement>
}