package ru.qwuadrixx.webprog.utils

import org.json.JSONObject

fun methodNotAllowed(method: String): String {
    val body = JSONObject().put("error", "unsupported method: $method").toString()
    return "Status: 405 Method Not Allowed\n" +
            "Content-Type: application/json; charset=utf-8\n" +
            "Content-Length: ${body.toByteArray().size}\n" +
            "\n" +
            body
}

fun badRequest(reason: String): String {
    val body = JSONObject().put("error", reason).toString()
    return "Status: 400 Bad Request\n" +
            "Content-Type: application/json; charset=utf-8\n" +
            "Content-Length: ${body.toByteArray().size}\n" +
            "\n" +
            body
}

fun methodOK(json: String): String {
    return "Status: 200 OK\n" +
            "Content-Type: application/json; charset=utf-8\n" +
            "Content-Length: ${json.toByteArray().size}\n" +
            "\n" +
            json
}
