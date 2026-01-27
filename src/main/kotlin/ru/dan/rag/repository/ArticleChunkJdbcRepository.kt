package ru.dan.rag.repository

import ru.dan.rag.entity.ArticleChunk

interface ArticleChunkJdbcRepository {

    fun batchInsert(elements: List<ArticleChunk>)
}