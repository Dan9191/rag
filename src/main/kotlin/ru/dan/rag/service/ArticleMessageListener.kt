package ru.dan.rag.service

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import ru.dan.rag.model.ArticleMessage

private val logger = KotlinLogging.logger {}

@Component
class ArticleMessageListener(
    private val articleProcessingService: ArticleProcessingService,
    private val objectMapper: ObjectMapper
) {

    /**
     * Метод для обработки входящих сообщений из RabbitMQ
     * Имя метода должно совпадать с тем, что мы укажем в MessageListenerAdapter
     */
    @RabbitListener(queues = ["\${app.rag.rabbit.queue}"])
    fun processMessage(message: String) {
        val startTime = System.currentTimeMillis()

        try {
            val articleMessage = objectMapper.readValue(message, ArticleMessage::class.java)
            logger.info { "Processing article id=${articleMessage.articleId}, event=${articleMessage.eventType}" }
            val resultId = articleProcessingService.processArticle(articleMessage)
            val duration = System.currentTimeMillis() - startTime
            logger.info { "Article processed successfully, result=$resultId, took ${duration}ms" }

        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            logger.error(e) { "Failed to process message after ${duration}ms" }
            throw e
        }
    }
}