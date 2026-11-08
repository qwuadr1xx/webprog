package ru.qwuadrixx.webprog.validation

import ru.qwuadrixx.webprog.dto.PointRequest
import ru.qwuadrixx.webprog.model.Point

class Validator {
    companion object {
        private val allowedX = listOf(-3, -2, -1, 0, 1, 2, 3, 4, 5)
        private val allowedY: (Double) -> Boolean = { it > -3 && it < 5 }
        private val allowedR = listOf(1, 2, 3, 4, 5)

        fun validate(point: PointRequest): ValidationResult {
            val x = point.x?.toDoubleOrNull() ?: return ValidationResult.Invalid("x must be a number")
            val y = point.y?.toDoubleOrNull() ?: return ValidationResult.Invalid("y must be a number")
            val r = point.r?.toDoubleOrNull() ?: return ValidationResult.Invalid("r must be a number")

            if (!x.isExactInt() || x.toInt() !in allowedX) {
                return ValidationResult.Invalid("x must be one of $allowedX")
            }
            if (!allowedY(y)) {
                return ValidationResult.Invalid("y must be in range (-3, 5)")
            }
            if (!r.isExactInt() || r.toInt() !in allowedR) {
                return ValidationResult.Invalid("r must be one of $allowedR")
            }

            return ValidationResult.Valid(Point(x.toInt(), y, r.toInt()))
        }
    }
}

private fun Double.isExactInt(): Boolean = this == toInt().toDouble()