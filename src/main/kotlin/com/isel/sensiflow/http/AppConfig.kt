package com.isel.sensiflow.http

import com.isel.sensiflow.http.pipeline.authentication.AuthenticationInterceptor
import com.isel.sensiflow.http.pipeline.authentication.UserIDArgumentResolver
import org.springframework.stereotype.Component
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Component
class AppConfig(
    val userIDArgumentResolver: UserIDArgumentResolver,
    val authenticationInterceptor: AuthenticationInterceptor
) : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<org.springframework.web.method.support.HandlerMethodArgumentResolver>) {
        resolvers.add(userIDArgumentResolver)
    }

    override fun addInterceptors(registry: org.springframework.web.servlet.config.annotation.InterceptorRegistry) {
        registry.addInterceptor(authenticationInterceptor)
    }
}
