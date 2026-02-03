package ru.dan.rag.service

import org.springframework.ai.chat.client.ChatClient
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
    private val chatClientBuilder: ChatClient.Builder,
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
        val systemPrompt = """
            Отвечай на основе следующей информации из базы знаний. 
            Не придумывай факты. Если в контексте нет ответа — напиши: "Информация отсутствует". 
            Форматируй ответ в markdown, будь краток и структурирован. 
            Структурируй текст по логическим блокам, которые начинаются с новой строки в виде отдельного абзаца, для этого используй символ <br>.
            В конце ответа приложи список ссылок и названия статей из полей articleId и articleTitle.
            Ограничивай статью размером до 1000 слов.
            
            Контекст (самые релевантные фрагменты):
            $context
            
            Вопрос:
        """.trimIndent()

        return chatClient.prompt()
            .system(systemPrompt)
            .user(query)
            .stream()
            .content()
    }

}