package ru.dan.rag

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@Testcontainers
class RagApplicationTests {

	companion object {
		@Container
		@JvmStatic
		val postgresContainer = PostgreSQLContainer<Nothing>("postgres:15")
			.apply {
				withDatabaseName("testdb")
				withUsername("test")
				withPassword("test")
			}

		@JvmStatic
		@DynamicPropertySource
		fun configureProperties(registry: DynamicPropertyRegistry) {
			registry.add("spring.r2dbc.url") {
				"r2dbc:postgresql://${postgresContainer.host}:${postgresContainer.firstMappedPort}/${postgresContainer.databaseName}"
			}
			registry.add("spring.r2dbc.username") { postgresContainer.username }
			registry.add("spring.r2dbc.password") { postgresContainer.password }
			registry.add("spring.flyway.enabled") { "false" }
		}
	}

	@Test
	fun contextLoads() {
	}

}
