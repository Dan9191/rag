package ru.dan.rag.repository

import ru.dan.rag.entity.ArticleElement

/**
 * Репозиторий для работы с блоками статьи.
 */
interface ArticleElementJdbcRepository {

    fun batchInsert(elements: List<ArticleElement>)
}