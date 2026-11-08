package ru.qwuadrixx.webprog.handler

import ru.qwuadrixx.webprog.dto.PointResponse

sealed class RequestResult {
    data class Success(val pointResponse: PointResponse) : RequestResult()
    data class Failure(val message: String) : RequestResult()
}