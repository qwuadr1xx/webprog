package ru.qwuadrixx.webprog.handler

import org.json.JSONArray
import ru.qwuadrixx.webprog.dto.PointRequest
import ru.qwuadrixx.webprog.dto.PointResponse
import ru.qwuadrixx.webprog.storage.PointsStorage
import ru.qwuadrixx.webprog.utils.badRequest
import ru.qwuadrixx.webprog.utils.methodNotAllowed
import ru.qwuadrixx.webprog.utils.methodOK
import ru.qwuadrixx.webprog.utils.parseQueryParams
import ru.qwuadrixx.webprog.validation.HitChecker
import ru.qwuadrixx.webprog.validation.ValidationResult
import ru.qwuadrixx.webprog.validation.Validator

class RequestHandler {
    private val storage = PointsStorage()

    fun handle(method: String, query: String?): String {
        if (method != "GET") {
            return methodNotAllowed(method)
        }

        return if (query.isNullOrEmpty()) {
            listPoints()
        } else {
            processPoint(query)
        }
    }

    private fun listPoints(): String {
        val points = JSONArray(storage.getAll().map { it.toJson() })
        return methodOK(points.toString())
    }

    private fun processPoint(query: String): String {
        val start = System.nanoTime()
        val queries = parseQueryParams(query)
        val point = PointRequest(queries["x"], queries["y"], queries["r"])

        val result = when (val validationResult = Validator.validate(point)) {
            is ValidationResult.Invalid -> RequestResult.Failure(validationResult.reason)
            is ValidationResult.Valid -> {
                val hit = HitChecker.check(validationResult.point)
                val response = PointResponse(
                    x = validationResult.point.x,
                    y = validationResult.point.y,
                    r = validationResult.point.r,
                    hit = hit,
                    requestTime = System.currentTimeMillis(),
                    executionTimeMs = (System.nanoTime() - start) / 1_000_000
                )
                storage.add(response)
                RequestResult.Success(response)
            }
        }

        return when (result) {
            is RequestResult.Success -> methodOK(result.pointResponse.toJson().toString())
            is RequestResult.Failure -> badRequest(result.message)
        }
    }
}