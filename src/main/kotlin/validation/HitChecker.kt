package ru.qwuadrixx.webprog.validation

import ru.qwuadrixx.webprog.model.Point

class HitChecker {
    companion object {
        fun check(points: Point): Boolean {
            val x = points.x
            val y = points.y
            val r = points.r

            return (y >= 0 && x * x + y * y <= r * r)
                    || (x <= 0 && y <= 0 && x + 2 * y >= -r)
                    || (x >= 0 && x <= r && y >= 0 && y <= r)
        }
    }
}