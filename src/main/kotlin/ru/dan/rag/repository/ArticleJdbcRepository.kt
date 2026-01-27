package ru.dan.rag.repository

import ru.dan.rag.entity.Article

interface ArticleJdbcRepository {
    fun insert(article: Article)
}