package ru.qwuadrixx.webprog.web

import jakarta.servlet.ServletContextEvent
import jakarta.servlet.ServletContextListener
import jakarta.servlet.annotation.WebListener
import ru.qwuadrixx.webprog.storage.PointsStorage

const val STORAGE_ATTRIBUTE = "pointsStorage"

@WebListener
class AppContextListener : ServletContextListener {
    override fun contextInitialized(event: ServletContextEvent) {
        event.servletContext.setAttribute(STORAGE_ATTRIBUTE, PointsStorage())
    }
}
