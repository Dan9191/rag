package ru.dan.rag.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.segment.TextSegment
import java.time.OffsetDateTime
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.dan.rag.config.RagPropertiesConfig
import ru.dan.rag.entity.Article
import ru.dan.rag.entity.ArticleChunk
import ru.dan.rag.model.ArticleMessage
import ru.dan.rag.repository.ArticleChunkRepository
import ru.dan.rag.repository.ArticleRepository

/**
 * Сервис первичного приема статей в формате Markdown.
 */
@Service
class ArticleProcessingService(
    private val articleRepository: ArticleRepository,
    private val chunkRepository: ArticleChunkRepository,
    private val uuidGenerator: TimeOrderedUuidGenerator,
    private val objectMapper: ObjectMapper,
    private val ragPropertiesConfig: RagPropertiesConfig
) {

    private val log = LoggerFactory.getLogger(ArticleProcessingService::class.java)

    @Transactional
    fun processArticle(articleMessage: ArticleMessage): UUID {

        val article = createOrGetArticle(articleMessage)
        val preparedText: String = markdownToPlainText(articleMessage.content)

        val splitter = DocumentSplitters.recursive(
            ragPropertiesConfig.maxSegmentSizeInChars,
            ragPropertiesConfig.maxOverlapSizeInChars
        )

        val document = Document.from(preparedText)
        val segments = splitter.split(document)

        val chunks = createArticleChunksFromTextSegments(segments, article)
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
                originalContent = articleMessage.content,
                metadata = articleMessage.metadata,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now()
            )
        }
        articleRepository.insert(newArticle)
        return newArticle
    }

    /**
     * Очистка текста от markdown тегов.
     */
    fun markdownToPlainText(markdown: String): String {
        val parser = Parser.builder().build()
        val document = parser.parse(markdown)
        val htmlRenderer = HtmlRenderer.builder().build()
        val html = htmlRenderer.render(document)
        return org.jsoup.Jsoup.parse(html).text()
    }

    /**
     * Создание чанк на основе сегметов.
     */
    fun createArticleChunksFromTextSegments(
        textSegments: List<TextSegment>,
        article: Article,
        ): List<ArticleChunk> {
        return textSegments.mapIndexed { index, textSegment ->
            ArticleChunk(
                id = uuidGenerator.generateUUID(),
                articleId = article.id,
                chunkIndex = index,
                textForSearch = textSegment.text(),
                processingStatus = "PENDING",
                chunkMetadata = objectMapper.writeValueAsString(
                            mapOf(
                                "articleName" to article.title,
                                "externalArticleId" to article.externalArticleId,
                                "chunk_index" to index,
                                "total_chunks" to textSegments.size,
                            )
                        )
            )
        }
    }
}