package ru.dan.rag.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "app.rag")
class RagPropertiesConfig {

    /**
     * Урл сервиса векторизации.
     */
    val embeddingServiceUrl = "http://localhost:8000/v1/embeddings"

    /**
     * Задержка (в миллисекундах) между запусками задачи отправки чанк на векторизацию.
     */
    val embeddingDelay = 30000L

    /**
     * Минимальный коэффициент схожести в векторном поиске.
     */
    val minSimilarity = 0.80

    /**
     * Максимальный размер сегмента при разбиении на чанки.
     */
    val maxSegmentSizeInChars = 1000

    /**
     * Размер перекрытия чанк.
     */
    val maxOverlapSizeInChars = 150
}