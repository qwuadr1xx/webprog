package ru.qwuadrixx.webprog.validation

import ru.qwuadrixx.webprog.model.Point

sealed class ValidationResult {
    data class Valid(val point: Point) : ValidationResult()
    data class Invalid(val reason: String) : ValidationResult()
}
