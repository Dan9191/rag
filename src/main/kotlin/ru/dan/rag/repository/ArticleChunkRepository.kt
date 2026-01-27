package ru.dan.rag.repository

import java.util.*
import org.springframework.data.repository.CrudRepository
import ru.dan.rag.entity.ArticleChunk

interface ArticleChunkRepository : CrudRepository<ArticleChunk, UUID>, ArticleChunkJdbcRepository {

    fun findAllByArticleIdOrderByChunkIndex(articleId: UUID): List<ArticleChunk>

    fun findAllByProcessingStatus(status: String): List<ArticleChunk>
}