package ru.dan.rag.config

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder  // ← этот импорт был пропущен
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy
import org.apache.hc.core5.ssl.SSLContexts
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext

@Configuration
class GigachatSslConfig {

    /**
     * REST клиент с сертификатами для работы с моделями gigachat.
     */
    @Bean(name = ["gigachatRestTemplate"])
    fun gigachatRestTemplate(): RestTemplate {
        val certFactory = CertificateFactory.getInstance("X.509")
        val certInput: InputStream = javaClass.getResourceAsStream("/russian-root-ca.crt")
            ?: throw IllegalStateException("Не найден russian-root-ca.crt в resources")

        val rootCert = certFactory.generateCertificate(certInput) as java.security.cert.X509Certificate

        val keyStore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null, null)
            setCertificateEntry("russian-trusted-root-ca", rootCert)
        }

        val sslContext: SSLContext = SSLContexts.custom()
            .loadTrustMaterial(keyStore, null)
            .build()

        val tlsStrategy = DefaultClientTlsStrategy(sslContext)
        val connManager = PoolingHttpClientConnectionManagerBuilder.create()
            .setTlsSocketStrategy(tlsStrategy)
            .build()

        val httpClient: CloseableHttpClient = HttpClients.custom()
            .setConnectionManager(connManager)
            .build()

        val requestFactory = HttpComponentsClientHttpRequestFactory(httpClient)

        return RestTemplate(requestFactory)
    }
}