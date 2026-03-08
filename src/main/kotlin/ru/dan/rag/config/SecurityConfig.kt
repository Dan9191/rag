package ru.dan.rag.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration

@Configuration
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {

        http
            .cors { cors ->
                cors.configurationSource {
                    val config = CorsConfiguration()
                    config.allowedOrigins = listOf(
                        "http://localhost:3000",
                    )
                    config.allowedMethods = listOf("GET","POST","PUT","DELETE","OPTIONS","PATCH")
                    config.allowedHeaders = listOf(
                        "Authorization",
                        "Content-Type",
                        "X-Requested-With",
                        "Accept",
                        "Origin",
                        "Access-Control-Request-Method",
                        "Access-Control-Request-Headers"
                    )
                    config.allowCredentials = true
                    config
                }
            }
            .csrf { (it.disable()) }
            .authorizeHttpRequests { auth ->

                auth
                    .requestMatchers(HttpMethod.POST,"/api/v1/rag/answer")
                    .hasAnyAuthority("ROLE_graduation.admin", "ROLE_graduation.user")

                    .requestMatchers(HttpMethod.POST,"/api/v1/rag/search")
                    .hasAnyAuthority("ROLE_graduation.admin", "ROLE_graduation.user")

                    .requestMatchers("/ws/**").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer {
                it.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }

        return http.build()
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {

        val converter = JwtAuthenticationConverter()

        converter.setJwtGrantedAuthoritiesConverter { jwt ->
            extractRealmRoles(jwt)
                .map { SimpleGrantedAuthority("ROLE_$it") }
        }

        return converter
    }

    private fun extractRealmRoles(jwt: Jwt): List<String> {

        val realmAccess = jwt.getClaimAsMap("realm_access") ?: return emptyList()

        val roles = realmAccess["roles"]

        return if (roles is List<*>) {
            roles.filterIsInstance<String>()
        } else emptyList()
    }
}