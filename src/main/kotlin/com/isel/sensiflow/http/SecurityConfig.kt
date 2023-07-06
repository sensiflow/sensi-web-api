package com.isel.sensiflow.http

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Value("\${server.ssl.enabled}")
    private var sslEnabled: Boolean = false

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain? {

        return http
            .requiresChannel { channel ->
                if (sslEnabled) {
                    channel.anyRequest().requiresSecure()
                } else {
                    channel.anyRequest().requiresInsecure()
                }

            }
            .build()
    }
}
