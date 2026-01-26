package ru.dan.rag.repository

import java.util.*
import org.springframework.data.repository.CrudRepository
import ru.dan.rag.entity.ArticleElement

interface ArticleElementRepository : CrudRepository<ArticleElement, UUID> {

    fun findAllByArticleIdOrderByElementIndex(articleId: UUID): List<ArticleElement>
}