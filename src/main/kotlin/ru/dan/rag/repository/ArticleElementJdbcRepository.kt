package ru.dan.rag.repository

import ru.dan.rag.entity.ArticleElement

interface ArticleElementJdbcRepository {

    fun batchInsert(elements: List<ArticleElement>)
}