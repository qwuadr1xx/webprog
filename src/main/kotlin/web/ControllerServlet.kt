package ru.qwuadrixx.webprog.web

import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

@WebServlet("/points")
class ControllerServlet : HttpServlet() {
    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        val hasPointData = !request.getParameter("x").isNullOrEmpty() &&
                !request.getParameter("y").isNullOrEmpty() &&
                !request.getParameter("r").isNullOrEmpty()

        if (hasPointData) {
            request.servletContext.getNamedDispatcher("AreaCheckServlet").forward(request, response)
        } else {
            request.getRequestDispatcher("/WEB-INF/jsp/form.jsp").forward(request, response)
        }
    }
}