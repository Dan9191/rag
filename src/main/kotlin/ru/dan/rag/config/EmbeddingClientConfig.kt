package ru.dan.rag.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

@Configuration
class EmbeddingClientConfig {

    @Bean
    fun embeddingRestTemplate(): RestTemplate {
        val requestFactory = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(60_000)
            setReadTimeout(60_000)
        }

        return RestTemplate(requestFactory)
    }
}