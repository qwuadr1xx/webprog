package ru.qwuadrixx.webprog.utils

import java.net.URLDecoder

fun parseQueryParams(query: String?): Map<String, String> {
    if (query.isNullOrEmpty()) return emptyMap()

    return query.split("&")
        .mapNotNull { param ->
            val parts = param.split("=", limit = 2)
            if (parts.size != 2) return@mapNotNull null
            URLDecoder.decode(parts[0], "UTF-8") to URLDecoder.decode(parts[1], "UTF-8")
        }
        .toMap()
}