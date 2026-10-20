package dev.pavel.booking.lab

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.CompletableFuture
import org.springframework.context.annotation.Lazy

// День 2, шаг 3. Прокси и self-invocation на примере @Async.
// @Async = «выполни этот метод в другом потоке». Делает это не сам метод, а прокси вокруг бина.

// Включает обработку @Async. Без этой аннотации @Async молча игнорируется.
@Configuration
@EnableAsync
class AsyncConfig

@Service
class ReportService(
    @Lazy private val self: ReportService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    // Должен выполняться в отдельном потоке (task-1, task-2...), а не в потоке Tomcat (http-nio-...).
    // Возвращаем CompletableFuture, чтобы вызывающий мог дождаться результата — имени потока.
    @Async
    fun generate(): CompletableFuture<String> {
        val thread = Thread.currentThread().name
        log.info("[PROXY] generate() выполняется в потоке {}", thread)
        return CompletableFuture.completedFuture(thread)
    }

    // Вызывает generate() ИЗНУТРИ того же класса. По сути это this.generate().
    fun generateViaThis(): CompletableFuture<String> = self.generate()
}

@RestController
class ProxyController(
    // Сюда Spring внедрит НЕ ReportService, а прокси-наследник от него.
    private val reportService: ReportService,
) {

    @GetMapping("/lab/proxy")
    fun proxy(): Map<String, String> = mapOf(
        // Настоящий класс объекта, который лежит в поле reportService.
        "injectedClass" to reportService.javaClass.name,
        // Поток Tomcat, который обрабатывает HTTP-запрос.
        "controllerThread" to Thread.currentThread().name,
        // Вызов снаружи (из другого бина) — через прокси.
        "callFromOtherBean" to reportService.generate().get(),
        // Вызов generate() изнутри ReportService — мимо прокси.
        "callViaThis" to reportService.generateViaThis().get(),
    )
}
