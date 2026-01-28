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
    // todo переделать чанкование.
    // todo 1. Если статья больше рекомендованной чанки в 2 раза, то чанковать такую статью как единую целую
    // todo 2. Если статья больше рекомендованной чанки в 3 раза, то чанковать такую статью как две пересекающиеся
    fun buildChunks(
        articleId: UUID,
        elements: List<ArticleElement>,
        targetChars: Int = 1100,       // желаемый целевой размер ~ 300–400 токенов
        overlapChars: Int = 350,       // перекрытие ~ 25–30%
        minChunkChars: Int = 250       // не создаём микрочанки меньше этого
    ): List<ArticleChunk> {

        val chunks = mutableListOf<ArticleChunk>()
        val buffer = StringBuilder()
        val elementIdsInBuffer = mutableListOf<UUID>()
        var currentSectionTitle = ""

        var chunkIndex = 0

        fun flushChunk(force: Boolean = false) {
            if (buffer.length < minChunkChars && !force) return

            val rawText = buffer.toString().trim()
            if (rawText.isBlank()) return

            // Добавляем заголовок раздела в начало, если его там ещё нет
            val displayText = if (currentSectionTitle.isNotBlank() &&
                !rawText.startsWith(currentSectionTitle.trim())
            ) {
                "$currentSectionTitle\n\n$rawText"
            } else {
                rawText
            }

            chunks += ArticleChunk(
                id = uuidGenerator.generateUUID(),
                articleId = articleId,
                chunkIndex = chunkIndex++,
                textForSearch = displayText,
                processingStatus = "PENDING",
                sourceElementIds = objectMapper.writeValueAsString(
                    elementIdsInBuffer.map { it.toString() }
                ),
                chunkMetadata = objectMapper.writeValueAsString(
                    mapOf(
                        "section" to currentSectionTitle,
                        "elementTypes" to elementIdsInBuffer
                            .mapNotNull { id -> elements.find { it.id == id }?.elementType }
                            .distinct(),
                        "charCount" to displayText.length,
                        "hasOverlap" to (overlapChars > 0)
                    )
                )
            )

            // Подготавливаем перекрытие для следующего чанка
            if (overlapChars > 0 && buffer.length > overlapChars) {
                val overlapPart = buffer.substring(buffer.length - overlapChars)
                buffer.clear()
                buffer.append(overlapPart)

                // Сохраняем только последние элементы, которые попали в перекрытие
                // (примерно — можно улучшить точнее, но для начала достаточно)
                elementIdsInBuffer.clear()
            } else {
                buffer.clear()
                elementIdsInBuffer.clear()
            }
        }

        for (element in elements) {

            val textPart = when (element.elementType) {
                "heading" -> {
                    val title = (element.content ?: "").trim()
                    if (title.isNotBlank()) {
                        currentSectionTitle = title
                    }
                    "$title\n"
                }

                "paragraph" -> {
                    (element.content?.trim() ?: "") + "\n\n"
                }

                "list" -> {
                    if (element.items.isNullOrBlank()) ""
                    else {
                        try {
                            val items = objectMapper.readValue<List<String>>(element.items)
                            if (items.isEmpty()) ""
                            else items.joinToString(
                                separator = "\n",
                                prefix = "• ",
                                postfix = "\n\n"
                            ) { "• $it" }
                        } catch (e: Exception) {
                            log.warn("Cannot parse list items", e)
                            ""
                        }
                    }
                }

                else -> ""
            }

            if (textPart.isBlank()) continue

            // Проверяем, не превысит ли добавление лимит
            if (buffer.isNotEmpty() && buffer.length + textPart.length > targetChars) {
                flushChunk()
            }

            // Особая логика для заголовков — начинаем новый чанк перед важным заголовком
            if (element.elementType == "heading" && buffer.isNotEmpty()) {
                flushChunk()
            }

            buffer.append(textPart)
            elementIdsInBuffer.add(element.id)
        }

        // Не забываем последний кусок
        flushChunk(force = true)

        return chunks
    }
}