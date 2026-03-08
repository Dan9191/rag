package ru.dan.rag.service

import org.springframework.stereotype.Service
import ru.dan.rag.client.GigachatModelsClient

/**
 * Сервис формирования читаемого ответа.
 */
@Service
class LlmService(
    private val gigachatModelsClient: GigachatModelsClient
) {

    fun generateResponse(query: String, context: String): String {

        val messages = listOf(

            GigachatModelsClient.Message(
                role = "system",
                content = """
                Ты — RAG ассистент.
                Используй только переданный контекст.
                Если ответа в переданном контексте нет — напиши "Информация отсутствует".
                Ответ должен быть на русском языке.
                Форматируй ответ в Markdown.
                """.trimIndent()
            ),

            GigachatModelsClient.Message(
                role = "user",
                content = """
                Контекст:
                $context
                
                Вопрос:
                $query
                """.trimIndent()
            )
        )

        return gigachatModelsClient.generateText(messages)
            ?: "Ошибка генерации ответа"
    }
}