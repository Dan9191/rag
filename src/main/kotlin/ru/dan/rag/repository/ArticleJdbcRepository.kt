package ru.dan.rag.repository

import ru.dan.rag.entity.Article

/**
 * Репозиторий для работы со статьями.
 */
interface ArticleJdbcRepository {
    fun insert(article: Article)
}