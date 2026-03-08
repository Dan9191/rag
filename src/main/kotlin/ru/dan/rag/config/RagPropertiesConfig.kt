package ru.dan.rag.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "app.rag")
class RagPropertiesConfig (
    var rabbit: RabbitConfig = RabbitConfig(),
    var embedding: EmbeddingConfig = EmbeddingConfig(),

    /**
     * Задержка (в миллисекундах) между запусками задачи отправки чанк на векторизацию.
     */
    var embeddingDelay: Long = 30000L,

    /**
     * Минимальный коэффициент схожести в векторном поиске.
     */
    var minSimilarity: Double = 0.80,

    /**
     * Максимальный размер сегмента при разбиении на чанки.
     */
    var maxSegmentSizeInChars: Int = 1000,

    /**
     * Размер перекрытия чанк.
     */
    var maxOverlapSizeInChars: Int = 150,

) {

    data class RabbitConfig(
        var exchange: String = "",
        var routingKey: String = "",
        var queue: String = ""
    )

    data class EmbeddingConfig(

        /**
         * Урл сервиса получения токена доступа.
         */
        var tokenUrl: String = "https://ngw.devices.sberbank.ru:9443/api/v2/oauth",

        /**
         * Урл сервиса векторизации.
         */
        var embeddingUrl: String = "https://gigachat.devices.sberbank.ru/api/v1/embeddings",

        /**
         * Токен для gigachat моделей.
         */
        var secretToken: String = "",

        /**
         * Модель для векторизации.
         */
        var embeddingModel: String = "EmbeddingsGigaR"
    )
}