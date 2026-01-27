package ru.dan.rag.repository

import java.util.*
import org.springframework.data.repository.CrudRepository
import ru.dan.rag.entity.ArticleChunk

/**
 * Репозиторий для работы с чанками.
 */
interface ArticleChunkRepository : CrudRepository<ArticleChunk, UUID>, ArticleChunkJdbcRepository {

}