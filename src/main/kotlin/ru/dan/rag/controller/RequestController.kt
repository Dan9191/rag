package ru.dan.rag.controller

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import ru.dan.rag.model.answer.SearchRequest
import ru.dan.rag.model.answer.SearchResponse
import ru.dan.rag.service.LlmService
import ru.dan.rag.service.SearchService

@RestController
@RequestMapping("/api/v1/request")
@CrossOrigin(
    origins = ["http://localhost:63343", "http://localhost:*"],
    allowedHeaders = ["*"],
    methods = [RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS],
    exposedHeaders = ["*"],
    maxAge = 3600
)
class RequestController (
    private val searchService: SearchService,
    private val llmService: LlmService
) {

    @PostMapping
    fun search(@RequestBody request: SearchRequest): SearchResponse {
        return searchService.search(request)
    }

    @PostMapping("/ai/chat")
    fun chat(@RequestBody request: SearchRequest): Map<String, String> {
        val response = llmService.generateResponse(request.query)
        return mapOf("response" to response)
    }

    @PostMapping(
        value = ["/chat/stream"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    )
    fun chatStream(@RequestBody request: SearchRequest): Flux<String> {
        val searchResponse = searchService.search(request)

        val context = searchResponse.results
            .joinToString("\n\n───\n") { result ->
                buildString {
                    append(result.text)
                    if (result.articleTitle != null) append("\nИсточник: ${result.articleTitle}")
                    if (result.articleId != null) append(" [id: ${result.articleId}]")
                    if (result.similarity != null) append(" (score: %.3f)".format(result.similarity))
                }
            }

        return llmService.generateResponseStream(request.query, context)
    }

}