package dev.pavel.booking.lab

import java.util.concurrent.Executors

// День 2. Прокси «руками», БЕЗ Spring. Ровно то, что Spring генерирует сам для @Async.
// Запуск: зелёный треугольник слева от fun main() ниже. Spring при этом не стартует.

// 1. Обычный класс с бизнес-логикой. Ничего не знает о потоках.
//    open — потому что прокси будет его наследником (так работает CGLIB).
open class Reports {
    open fun generate(): String = Thread.currentThread().name

    // Вызов generate() изнутри класса. Здесь this — это ВСЕГДА этот объект, а не прокси.
    open fun generateViaThis(): String = this.generate()
}

// 2. Прокси. Наследник Reports, поэтому его можно подсунуть туда, где ждут Reports.
//    Внутри держит ссылку на НАСТОЯЩИЙ объект (target) и пересылает ему вызовы.
class ReportsProxy(private val target: Reports) : Reports() {

    // Пул потоков. В Spring его роль играет бин applicationTaskExecutor (потоки task-1, task-2...).
    private val pool = Executors.newFixedThreadPool(2) { r -> Thread(r, "async-pool") }

    // Метод «с аннотацией @Async»: прокси добавляет своё поведение —
    // отправляет вызов настоящего метода в другой поток.
    override fun generate(): String = pool.submit<String> { target.generate() }.get()

    // Метод без аннотации: прокси ничего не добавляет, просто передаёт вызов target.
    // Дальше код выполняется ВНУТРИ target, и его this.generate() идёт мимо прокси.
    override fun generateViaThis(): String = target.generateViaThis()

    fun close() = pool.shutdown()
}

fun main() {
    val real = Reports()
    val proxy = ReportsProxy(real)

    // Так видит мир контроллер: у него в поле лежит proxy, но тип поля — Reports.
    val service: Reports = proxy

    println("Поток main:                  ${Thread.currentThread().name}")
    println("service.generate():          ${service.generate()}")        // через прокси -> другой поток
    println("service.generateViaThis():   ${service.generateViaThis()}") // мимо прокси -> тот же поток main
    println("Класс объекта в поле:        ${service.javaClass.simpleName}")

    proxy.close()
}
