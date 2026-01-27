package ru.dan.rag.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.time.OffsetDateTime
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.dan.rag.entity.Article
import ru.dan.rag.entity.ArticleChunk
import ru.dan.rag.entity.ArticleElement
import ru.dan.rag.model.ArticleMessage
import ru.dan.rag.model.HeadingBlock
import ru.dan.rag.model.ListBlock
import ru.dan.rag.model.ParagraphBlock
import ru.dan.rag.model.RawArticleBlock
import ru.dan.rag.repository.ArticleChunkRepository
import ru.dan.rag.repository.ArticleElementRepository
import ru.dan.rag.repository.ArticleRepository

/**
 * Сервис первичного приема статей.
 */
@Service
class ArticleProcessingService(
    private val articleRepository: ArticleRepository,
    private val articleElementRepository: ArticleElementRepository,
    private val chunkRepository: ArticleChunkRepository,
    private val objectMapper: ObjectMapper,
    private val uuidGenerator: TimeOrderedUuidGenerator
) {

    private val log = LoggerFactory.getLogger(ArticleProcessingService::class.java)

    @Transactional
    fun processArticle(articleMessage: ArticleMessage): UUID {

        val article = createOrGetArticle(articleMessage)

        // 1. JSON → Raw blocks
        val blocks: List<RawArticleBlock> =
            objectMapper.readValue(articleMessage.content)

        // 2. Raw blocks → ArticleElement
        val elements = toArticleElements(article.id, blocks)
        articleElementRepository.batchInsert(elements)

        // 3. Elements → Chunks
        val chunks = buildChunks(article.id, elements)
        chunkRepository.batchInsert(chunks)

        return article.id
    }


    /**
     * Создаем или получаем статью.
     */
    private fun createOrGetArticle(articleMessage: ArticleMessage): Article {
        val existingArticle = articleRepository.findByExternalArticleId(articleMessage.id)
        val newArticle: Article

        if (existingArticle != null) {
            log.info("Статья уже существует, обновляем: id=${existingArticle.id}")
            newArticle = existingArticle.copy(
                    title = articleMessage.title,
                    metadata = articleMessage.metadata,
                    updatedAt = OffsetDateTime.now()
                )
        } else {
            newArticle = Article(
                id = uuidGenerator.generateUUID(),
                externalArticleId = articleMessage.id,
                title = articleMessage.title,
                originalJson = articleMessage.content,
                metadata = articleMessage.metadata,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now()
            )
        }
        articleRepository.insert(newArticle)
        return newArticle
    }

    private fun parseContentToElements(articleId: UUID, content: String): List<ArticleElement> {

        val paragraphs = content.split("\n\n")

        return paragraphs.mapIndexed { index, paragraph ->
            val trimmedParagraph = paragraph.trim()
            if (trimmedParagraph.isNotEmpty()) {
                ArticleElement(
                    id = uuidGenerator.generateUUID(),
                    articleId = articleId,
                    elementIndex = index,
                    elementType = "paragraph",
                    content = trimmedParagraph,
                    items = null,
                    metadata = null,
                    createdAt = OffsetDateTime.now()
                )
            } else {
                null
            }
        }.filterNotNull()
    }


    /**
     * Получаем из сырых блоков элементы, готовые для чанкования.
     */
    fun toArticleElements(
        articleId: UUID,
        blocks: List<RawArticleBlock>
    ): List<ArticleElement> {

        return blocks.mapIndexed { index, block ->
            when (block) {
                is ParagraphBlock -> ArticleElement(
                    id = uuidGenerator.generateUUID(),
                    articleId = articleId,
                    elementIndex = index,
                    elementType = "paragraph",
                    content = block.content,
                    items = null,
                    metadata = null
                )

                is HeadingBlock -> ArticleElement(
                    id = uuidGenerator.generateUUID(),
                    articleId = articleId,
                    elementIndex = index,
                    elementType = "heading",
                    content = block.content,
                    items = null,
                    metadata = null
                )

                is ListBlock -> ArticleElement(
                    id = uuidGenerator.generateUUID(),
                    articleId = articleId,
                    elementIndex = index,
                    elementType = "list",
                    content = null,
                    items = objectMapper.writeValueAsString(block.items),
                    metadata = null
                )
            }
        }
    }

    /**
     * Делим элементы на чанки.
     */
    fun buildChunks(
        articleId: UUID,
        elements: List<ArticleElement>,
        maxChars: Int = 1500
    ): List<ArticleChunk> {

        val chunks = mutableListOf<ArticleChunk>()

        var currentElements = mutableListOf<ArticleElement>()
        var currentText = StringBuilder()
        var chunkIndex = 0

        fun flushChunk() {
            if (currentElements.isEmpty()) return

            chunks += ArticleChunk(
                id = uuidGenerator.generateUUID(),
                articleId = articleId,
                chunkIndex = chunkIndex++,
                textForSearch = currentText.toString().trim(),
                processingStatus = "PENDING",
                sourceElementIds = objectMapper.writeValueAsString(
                    currentElements.map { it.id.toString() }
                ),
                chunkMetadata = objectMapper.writeValueAsString(
                    mapOf(
                        "elementTypes" to currentElements.map { it.elementType },
                        "charCount" to currentText.length
                    )
                )
            )

            currentElements = mutableListOf()
            currentText = StringBuilder()
        }

        for (element in elements) {

            if (element.elementType == "heading" && currentElements.isNotEmpty()) {
                flushChunk()
            }

            val text = when (element.elementType) {
                "heading" -> element.content ?: ""
                "paragraph" -> element.content ?: ""
                "list" -> {
                    val items = objectMapper.readValue<List<String>>(element.items!!)
                    items.joinToString("\n• ", prefix = "• ")
                }
                else -> ""
            }

            if (currentText.length + text.length > maxChars) {
                flushChunk()
            }

            currentElements += element
            currentText.append(text).append("\n\n")
        }

        flushChunk()

        return chunks
    }
}