package com.isel.sensiflow.http

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.isel.sensiflow.http.pipeline.authentication.AuthenticationInterceptor
import com.isel.sensiflow.http.pipeline.authentication.UserIDArgumentResolver
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Component
class AppConfig(
    val userIDArgumentResolver: UserIDArgumentResolver,
    val authenticationInterceptor: AuthenticationInterceptor
) : WebMvcConfigurer {

    @Bean
    fun objectMapper(): ObjectMapper = with(jacksonObjectMapper()) {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
    }

    override fun addArgumentResolvers(resolvers: MutableList<org.springframework.web.method.support.HandlerMethodArgumentResolver>) {
        resolvers.add(userIDArgumentResolver)
    }

    override fun addInterceptors(registry: org.springframework.web.servlet.config.annotation.InterceptorRegistry) {
        registry.addInterceptor(authenticationInterceptor)
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/**")
            .allowedOriginPatterns("http://localhost:[*]")
            .allowedMethods("GET", "POST", "OPTIONS", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true)
    }
}
