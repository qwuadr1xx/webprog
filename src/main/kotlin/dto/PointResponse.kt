package ru.qwuadrixx.webprog.dto

import org.json.JSONObject

data class PointResponse(
    val x: Int,
    val y: Double,
    val r: Int,
    val hit: Boolean,
    val requestTime: Long = 0,
    val executionTimeMs: Long = 0
) {
    fun toJson(): JSONObject = JSONObject()
        .put("x", x)
        .put("y", y)
        .put("r", r)
        .put("hit", hit)
        .put("requestTime", requestTime)
        .put("executionTimeMs", executionTimeMs)
}