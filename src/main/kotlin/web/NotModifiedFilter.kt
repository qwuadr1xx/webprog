package ru.qwuadrixx.webprog.web

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.annotation.WebFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import ru.qwuadrixx.webprog.storage.PointsStorage

@WebFilter("/points")
class NotModifiedFilter : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse

        if (httpRequest.method == "GET" && httpRequest.queryString.isNullOrBlank()) {
            val ifModifiedSince = try {
                httpRequest.getDateHeader("If-Modified-Since")
            } catch (e: IllegalArgumentException) {
                -1L
            }

            if (ifModifiedSince != -1L) {
                val storage = httpRequest.servletContext.getAttribute(STORAGE_ATTRIBUTE) as PointsStorage
                val lastModified = storage.getLastModified()

                if (ifModifiedSince >= lastModified) {
                    httpResponse.status = HttpServletResponse.SC_NOT_MODIFIED
                    return
                }
            }
        }

        chain.doFilter(request, response)
    }
}
