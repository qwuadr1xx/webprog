package ru.qwuadrixx.webprog

import com.fastcgi.FCGIInterface
import ru.qwuadrixx.webprog.handler.RequestHandler

fun main(args: Array<String>) {
    val fcgi = FCGIInterface()
    val handler = RequestHandler()

    while (fcgi.FCGIaccept() >= 0) {
        val params = FCGIInterface.request.params
        val method = params.getProperty("REQUEST_METHOD") ?: continue
        val query = params.getProperty("QUERY_STRING")

        print(handler.handle(method, query))
        System.out.flush()
    }
}