package ru.qwuadrixx.webprog.storage

import ru.qwuadrixx.webprog.dto.PointResult
import ru.qwuadrixx.webprog.model.Point
import java.util.Collections
import java.util.concurrent.atomic.AtomicLong

class PointsStorage {
    private val points = Collections.synchronizedList(mutableListOf<PointResult>())
    private val lastModified: AtomicLong = AtomicLong(System.currentTimeMillis())

    fun add(point: PointResult) {
        points.add(point)
        lastModified.set(System.currentTimeMillis())
    }

    fun getAll(): List<PointResult> = points

    fun getLastModified(): Long = lastModified.get()
}