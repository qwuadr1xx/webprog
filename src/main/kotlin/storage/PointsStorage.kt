package ru.qwuadrixx.webprog.storage

import ru.qwuadrixx.webprog.dto.PointResponse

class PointsStorage {
    private val points = mutableListOf<PointResponse>()

    fun add(point: PointResponse) {
        points.add(point)
    }

    fun getAll(): List<PointResponse> = points
}