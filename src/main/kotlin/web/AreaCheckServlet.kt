package ru.qwuadrixx.webprog.web

import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import ru.qwuadrixx.webprog.dto.PointRequest
import ru.qwuadrixx.webprog.dto.PointResult
import ru.qwuadrixx.webprog.storage.PointsStorage
import ru.qwuadrixx.webprog.validation.HitChecker
import ru.qwuadrixx.webprog.validation.ValidationResult
import ru.qwuadrixx.webprog.validation.Validator

class AreaCheckServlet : HttpServlet() {
    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        val x = request.getParameter("x")
        val y = request.getParameter("y")
        val r = request.getParameter("r")
        when (val validationResult = Validator.validate(point = PointRequest(x, y, r))) {
            is ValidationResult.Invalid  -> request.setAttribute("error", validationResult.reason)
            is ValidationResult.Valid -> {
                val hit = HitChecker.check(validationResult.point)
                val pointResult = PointResult(
                    x = validationResult.point.x,
                    y = validationResult.point.y,
                    r = validationResult.point.r,
                    hit = hit,
                    requestTime = System.currentTimeMillis()
                )
                val storage = request.servletContext.getAttribute(STORAGE_ATTRIBUTE) as PointsStorage
                storage.add(pointResult)
                request.setAttribute("result", pointResult)
            }
        }

        request.getRequestDispatcher("/WEB-INF/jsp/result.jsp").forward(request, response)
    }
}