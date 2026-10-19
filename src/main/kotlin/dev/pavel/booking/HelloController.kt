package dev.pavel.booking

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch

// Форма ответа. Spring сам превратит объект в JSON (как Gson/Moshi в Retrofit, только наоборот).
data class HelloResponse(
    val message: String,
    val servedBy: String,
    val requestNumber: Long
)

// @RestController: Spring сам создаст этот класс (как Hilt) и будет отдавать ответы методов как JSON.
@RestController
class HelloController(@Value("\${booking.greeting}") private val greeting: String) {

    @OptIn(ExperimentalAtomicApi::class)
    private val counter = AtomicLong(0)

    // @GetMapping: этот метод отвечает на GET http://localhost:8080/api/hello
    // Аналог @GET("/api/hello") из Retrofit, только на стороне сервера.
    @OptIn(ExperimentalAtomicApi::class)
    @GetMapping("/api/hello")
    fun hello(
        // @RequestParam: берёт ?name=... из адреса (аналог @Query в Retrofit).
        // Если параметра нет, подставит "world".
        @RequestParam(defaultValue = "world") name: String
    ): HelloResponse {
        return HelloResponse(
            message = "$greeting, $name",
            // Имя потока Tomcat, который обработал этот запрос.
            servedBy = Thread.currentThread().name,
            requestNumber = counter.incrementAndFetch()
        )
    }
}
