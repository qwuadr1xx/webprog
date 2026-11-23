package ru.qwuadrixx.webprog.validation

import ru.qwuadrixx.webprog.model.Point

class HitChecker {
    companion object {
        fun check(point: Point): Boolean {
            val x = point.x
            val y = point.y
            val r = point.r

            return (x <= 0 && y >= 0 && x * x + y * y <= r * r)
                    || (x >= -r / 2.0 && x <= 0 && y >= -r && y <= 0)
                    || (x >= 0 && y <= 0 && x - 2 * y <= r)
        }
    }
}