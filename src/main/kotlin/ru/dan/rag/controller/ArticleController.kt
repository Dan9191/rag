package ru.dan.rag.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.dan.rag.model.ArticleMessage
import ru.dan.rag.service.ArticleProcessingService

@RestController
@RequestMapping("/api/v1/articles")
class ArticleController(
    private val articleProcessingService: ArticleProcessingService
) {

    @PostMapping
    fun processArticle(@RequestBody articleMessage: ArticleMessage): ResponseEntity<Map<String, Any>> {
        val articleId = articleProcessingService.processArticle(articleMessage)

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
            mapOf(
                "status" to "processing_started",
                "article_id" to articleId.toString(),
                "message" to "Статья принята в обработку"
            )
        )
    }
}