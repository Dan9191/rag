package ru.dan.rag.service

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

/**
 * Сервис формирования читаемого ответа.
 */
@Service
class LlmService (
    private val chatModel: ChatModel,
    chatClientBuilder: ChatClient.Builder,
) {

    private val chatClient: ChatClient = chatClientBuilder.build()

    /**
     *  Тестовый метод
     */
    fun generateResponse(userQuery: String): String {
        val promptTemplate = PromptTemplate("{query}")
        val prompt = promptTemplate.create(mapOf("query" to userQuery))
        val response = chatModel.call(prompt)
        return response.result?.output?.text.toString()
    }

    /**
     * Стрим ответ
     */
    fun generateResponseStream(query: String, context: String): Flux<String> {
        val messages = listOf(
            SystemMessage("""
                Ты — RAG-ассистент.
                Используй ИСКЛЮЧИТЕЛЬНО информацию из переданного контекста.
                Ответ обязательно должен быть на русском языке.
                Запрещено использовать общие знания.
                Если ответа нет в контексте — напиши: "Информация отсутствует".
                Форматируй ответ в Markdown.
                Укажи источники в конце ответа.
            """.trimIndent()),

            UserMessage("""
            Используй только следующий контекст для ответа.
                
            Контекст:$context
            
            Вопрос: $query
            """.trimIndent())
            )

        return chatClient.prompt()
            .messages(messages)
            .stream()
            .content()
    }

}