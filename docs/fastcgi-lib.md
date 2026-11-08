# `com.fastcgi` (libs/fastcgi_lib.jar) — справка по использованию

Классический Java FastCGI Developer's Kit (`com.fastcgi`, версия `$Id: FCGIMessage.java,v 1.4 2000/10/02$`,
автор — Open Market / robs). В jar только 6 классов, исходников нет — всё ниже восстановлено
дизассемблированием (`javap -p -c -v`), поэтому это не пересказ javadoc, а проверенное поведение байткода.

## Классы и их роль

| Класс | Назначение |
|---|---|
| `FCGIInterface` | Точка входа: `FCGIaccept()` — главный цикл приёма запросов. |
| `FCGIRequest` | Состояние одного запроса: сокет, потоки, `params`, роль, keep-alive. |
| `FCGIInputStream` / `FCGIOutputStream` | Потоки тела запроса/ответа, обёрнутые в протокол FastCGI (разбор/сборка record-ов). |
| `FCGIMessage` | Низкоуровневый разбор заголовков FastCGI-пакетов и `FCGI_PARAMS` (имя-значение). |
| `FCGIGlobalDefs` | Константы протокола (типы записей, роли, коды ошибок) — использовать не нужно. |

На практике в коде приложения трогаются только `FCGIInterface` и `FCGIInterface.request` (объект `FCGIRequest`).

## Главный цикл (единственный паттерн использования)

```kotlin
fun main(args: Array<String>) {
    val fcgi = FCGIInterface()
    while (fcgi.FCGIaccept() >= 0) {
        val params = FCGIInterface.request.params   // java.util.Properties

        // тело запроса (если POST) читается из System.in — он уже подменён
        val body = System.`in`.readBytes().decodeToString()

        print("Content-Type: application/json\n\n")
        print(buildResponseJson(params, body))

        System.out.flush()   // обязательно: поток буферизован
    }
}
```

Внутри `FCGIaccept()` на каждой итерации библиотека сама:
1. принимает соединение (`ServerSocket.accept()`);
2. разбирает FastCGI-протокол и наполняет `FCGIRequest.params`;
3. **подменяет** `System.in` / `System.out` / `System.err` на потоки текущего запроса (`BufferedInputStream`/`BufferedOutputStream` обёрнутые вокруг `FCGIInputStream`/`FCGIOutputStream`);
4. вызывает `System.setProperties(request.params)`.

Это значит, что `System.getProperty("REQUEST_METHOD")` внутри итерации тоже сработает — но надёжнее и явнее читать через `FCGIInterface.request.params.getProperty(...)`, не завязываясь на подмену глобальных properties.

⚠️ Важно: `request.params` создаётся как `new Properties(startupProps)`, где `startupProps` — снимок исходных `System.getProperties()`, сделанный при первом вызове `FCGIaccept()`. То есть оригинальные JVM-свойства (`java.class.path` и т.п.) не теряются между запросами — они просто фоллбек-дефолты, а наверху лежат CGI-переменные конкретного запроса.

## ⚠️ Главный неочевидный момент: как библиотека понимает, что она FastCGI

Из байткода `FCGIaccept()`:

```java
isFCGI = System.getProperties().containsKey("FCGI_PORT");
```

- Это **Java system property**, а не переменная окружения и не файловый дескриптор stdin.
- Если `FCGI_PORT` не задан → `isFCGI == false` → **`FCGIaccept()` всегда возвращает `-1` без каких-либо действий**. Цикл `while (fcgi.FCGIaccept() >= 0)` не выполнится ни разу. Запуск `java -jar web.jar` без этого свойства — это не "fallback в обычный CGI", это просто немедленный выход из программы. Библиотека не умеет работать как plain CGI (однократный запуск с окружением из ОС) — только как сетевой FastCGI-responder.
- Если `FCGI_PORT` задан, на **первом** вызове `FCGIaccept()` библиотека сама открывает `new ServerSocket(FCGI_PORT)` и дальше в цикле вызывает `srvSocket.accept()` на каждой итерации — то есть **процесс сам слушает TCP-порт** и остаётся живым между запросами.

Практический вывод для запуска/тестирования без Apache:

```bash
java -DFCGI_PORT=9000 -jar build/libs/web.jar
```

После этого приложение слушает `127.0.0.1:9000` как FastCGI-responder. Протокол FastCGI, а не "голый HTTP" — curl'ом напрямую не постучаться, нужен клиент, понимающий FastCGI (например `cgi-fcgi` из `fcgi` пакета, или сам Apache через `mod_fastcgi`/`mod_proxy_fcgi`/`mod_fcgid`, настроенные ходить на этот TCP-адрес). Это и определяет, какой модуль Apache понадобится при развёртывании на удалённой машине — тема следующего шага.

## `FCGIRequest.params` — что там лежит

Это обычный CGI-окружение, которое веб-сервер передаёт в составе `FCGI_PARAMS`-записи (имя-значение, `UTF-8`), плюс один служебный ключ, который добавляет сама библиотека:

- `ROLE` — `RESPONDER` / `AUTHORIZER` / `FILTER` (для лабы нужен только `RESPONDER`, остальные роли библиотека и не поддерживает полноценно — для них не выставляются `inStream`/доп. потоки заголовка).

Остальные ключи — стандартные CGI/1.1 переменные, которые пришлёт Apache: `REQUEST_METHOD`, `QUERY_STRING`, `CONTENT_LENGTH`, `CONTENT_TYPE`, `SCRIPT_NAME`, `SERVER_NAME`, `REMOTE_ADDR` и т.д. Сама библиотека их имена не хардкодит — `FCGIMessage.readParams` просто читает то, что передал сервер.

```kotlin
val method = params.getProperty("REQUEST_METHOD")       // "GET" / "POST"
val query  = params.getProperty("QUERY_STRING") ?: ""    // "x=1&y=2&r=3"
val length = params.getProperty("CONTENT_LENGTH")?.toIntOrNull() ?: 0
```

## Чтение тела запроса (POST)

`System.in` на время итерации — это `BufferedInputStream` над `FCGIRequest.inStream` (`FCGIInputStream`). Читать нужно ровно `CONTENT_LENGTH` байт (поток не даёт EOF сам по себе в рамках типичного использования):

```kotlin
val length = params.getProperty("CONTENT_LENGTH")?.toIntOrNull() ?: 0
val body = if (length > 0) System.`in`.readNBytes(length).decodeToString() else ""
```

## Запись ответа

Никакого авто-HTTP: нужно вручную напечатать заголовки, пустую строку-разделитель и тело, затем **обязательно `flush()`** — `System.out` оборачивает буферизованный поток, без flush данные могут не уйти клиенту до закрытия сокета в следующей итерации `FCGIaccept()`.

```kotlin
print("Status: 200 OK\n")
print("Content-Type: application/json; charset=utf-8\n")
print("\n")
print(jsonBody)
System.out.flush()
```

Для ошибок есть отдельный `System.err`, подключенный к `FCGIRequest.errStream` — попадает в лог веб-сервера (stderr), а не в тело ответа клиенту.

## Жизненный цикл процесса

- Один процесс = один блокирующий цикл `accept → обработать → accept → …`. Запросы обрабатываются **строго последовательно**, параллелизма внутри одного процесса нет (простой `while`, без тредов).
- Любое состояние в `var`/`val` на уровне файла или в объектах, созданных до цикла, переживает между запросами одного процесса — это разумное место для in-memory хранения результатов (если Apache не поднимает несколько экземпляров одновременно).
- Исключение, вылетевшее из тела цикла и не пойманное, уронит весь процесс — стоит оборачивать обработку одного запроса в `try/catch`, чтобы не терять долгоживущий сервер из-за одной плохой записи.

## Минимальный рабочий пример (с `org.json`)

```kotlin
package ru.qwuadrixx.webprog

import com.fastcgi.FCGIInterface
import org.json.JSONObject

fun main(args: Array<String>) {
    val fcgi = FCGIInterface()
    while (fcgi.FCGIaccept() >= 0) {
        try {
            val params = FCGIInterface.request.params
            val query = params.getProperty("QUERY_STRING") ?: ""

            val response = JSONObject().put("echo", query).toString()

            print("Content-Type: application/json; charset=utf-8\n\n")
            print(response)
        } catch (e: Exception) {
            System.err.println("request failed: ${e.message}")
            print("Status: 500 Internal Server Error\n\n")
        } finally {
            System.out.flush()
        }
    }
}
```

## Роли, которые библиотека не стоит пытаться использовать

`AUTHORIZER` и `FILTER` обрабатываются в `FCGIaccept()` только на уровне выставления `ROLE` в `params` — отдельных потоков/семантики под них нет. Для лабы нужен только `RESPONDER` (это роль по умолчанию, которую будет присылать Apache).
