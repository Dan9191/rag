package ru.dan.rag.repository

import java.util.*
import org.springframework.data.repository.CrudRepository
import ru.dan.rag.entity.Article

interface ArticleRepository : CrudRepository<Article, UUID> {

    fun findByExternalArticleId(externalArticleId: String): Article?
}