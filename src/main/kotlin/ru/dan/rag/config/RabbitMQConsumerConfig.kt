package ru.dan.rag.config

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.dan.rag.service.ArticleMessageListener

@Configuration
class RabbitMQConsumerConfig(
    private val ragPropertiesConfig: RagPropertiesConfig
) {

    @Bean
    fun articleQueue(): Queue {
        return QueueBuilder.durable(ragPropertiesConfig.rabbit.queue).build()
    }

    @Bean
    fun jsonMessageConverter(): MessageConverter = JacksonJsonMessageConverter()

    @Bean
    fun articleExchange(): DirectExchange {
        return DirectExchange(ragPropertiesConfig.rabbit.exchange)
    }

    @Bean
    fun articleBinding(): Binding {
        return BindingBuilder
            .bind(articleQueue())
            .to(articleExchange())
            .with(ragPropertiesConfig.rabbit.routingKey)
    }

    /**
     * Адаптер, который связывает Listener с RabbitMQ
     */
    @Bean
    fun messageListenerAdapter(
        articleMessageListener: ArticleMessageListener,
        jsonMessageConverter: MessageConverter
    ): MessageListenerAdapter {
        val adapter = MessageListenerAdapter(articleMessageListener, "processMessage")
        adapter.setMessageConverter(jsonMessageConverter)
        return adapter
    }

}