package ru.dan.rag.controller

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import ru.dan.rag.model.answer.SearchRequest
import ru.dan.rag.model.answer.SearchResponse
import ru.dan.rag.service.LlmService
import ru.dan.rag.service.SearchService

@RestController
@RequestMapping("/api/v1/rag")
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

    @PostMapping("/answer")
    fun chat(@RequestBody request: SearchRequest): Map<String, String> {
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
        val response = llmService.generateResponse(request.query, context)
        return mapOf("response" to response)
    }

    @PostMapping("/search")
    fun search(@RequestBody request: SearchRequest): SearchResponse {
        return searchService.search(request)
    }

}
