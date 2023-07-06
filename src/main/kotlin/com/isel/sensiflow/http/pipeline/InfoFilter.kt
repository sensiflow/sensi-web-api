package com.isel.sensiflow.http.pipeline

import com.isel.sensiflow.http.controller.RequestPaths
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.io.UnsupportedEncodingException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper

@Component
class LoggingFilter : OncePerRequestFilter() {
    /**
     * Same contract as for `doFilter`, but guaranteed to be
     * just invoked once per request within a single request thread.
     * See [.shouldNotFilterAsyncDispatch] for details.
     *
     * Provides HttpServletRequest and HttpServletResponse arguments instead of the
     * default ServletRequest and ServletResponse ones.
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestWrapper = ContentCachingRequestWrapper(request)
        val startTime = System.currentTimeMillis()
        filterChain.doFilter(requestWrapper, response)
        val timeTaken = System.currentTimeMillis() - startTime

        val paramsString = request.parameterMap.map { "${it.key}=${it.value[0]}" }.joinToString("&")

        val requestBody = getStringValue(
            requestWrapper.contentAsByteArray,
            request.characterEncoding
        )
        val cookiesSize = if(request.cookies == null) 0 else request.cookies.size

        val logMessage = "Request ${request.method} on ${request.requestURI} took $timeTaken millis with params: " +
                "[$paramsString] and with ${cookiesSize} cookies."

        if(request.method == "POST" && request.requestURI == RequestPaths.Users.USERS + RequestPaths.Users.LOGIN ||
            request.method == "POST" && request.requestURI == RequestPaths.Users.USERS){
            LOGGER.info(logMessage)
            return
        }

        LOGGER.info("$logMessage body: [$requestBody]")
    }

    private fun getStringValue(contentAsByteArray: ByteArray, characterEncoding: String): String {
        try {
            return String(contentAsByteArray, charset(characterEncoding))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return ""
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(LoggingFilter::class.java)
    }
}

