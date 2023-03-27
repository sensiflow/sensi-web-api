package com.isel.sensiflow.http.pipeline.authentication

import com.isel.sensiflow.services.UserID
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class UserIDArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        val parameterIsCorrectType = parameter.parameterType == UserID::class.java
        val parameterCorrectlyNamed = parameter.parameterName == ACCEPTED_PARAM_NAME

        return parameterCorrectlyNamed && parameterIsCorrectType
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)
        return request?.let { getUserIDFrom(it) }
    }

    companion object {
        private const val KEY = "UserIDArgumentResolver"
        private const val ACCEPTED_PARAM_NAME = "userID"

        fun addUserIDTo(user: UserID?, request: HttpServletRequest) {
            return request.setAttribute(KEY, user)
        }

        fun getUserIDFrom(request: HttpServletRequest): UserID? {
            return request.getAttribute(KEY)?.let {
                it as? UserID
            }
        }
    }
}
