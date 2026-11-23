package ru.qwuadrixx.webprog.dto

data class PointResult(
    val x: Double,
    val y: Double,
    val r: Int,
    val hit: Boolean,
    val requestTime: Long = 0,
)