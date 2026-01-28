package ru.dan.rag.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.dan.rag.model.answer.SearchRequest
import ru.dan.rag.model.answer.SearchResponse
import ru.dan.rag.service.SearchService

@RestController
@RequestMapping("/api/v1/request")
class RequestController (
    private val searchService: SearchService
) {

    @PostMapping
    fun search(@RequestBody request: SearchRequest): SearchResponse {
        return searchService.search(request)
    }
}