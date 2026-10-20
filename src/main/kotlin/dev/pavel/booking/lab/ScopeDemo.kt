package dev.pavel.booking.lab

import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

// День 2, шаг 2. Singleton vs prototype.
// У каждого бина есть короткий id, который генерируется в момент создания объекта.
// Одинаковый id = тот же самый объект. Разный id = Spring создал новый.

// Singleton (scope по умолчанию): один объект на всё приложение.
@Component
class SingletonBean {
    val id: String = UUID.randomUUID().toString().take(8)
}

// Prototype: новый объект при каждом запросе бина у контейнера.
@Component
@Scope("prototype")
class PrototypeBean {
    private val log = LoggerFactory.getLogger(javaClass)
    val id: String = UUID.randomUUID().toString().take(8)

    init {
        log.info("[SCOPE] создан PrototypeBean {}", id)
    }

    // Проверь при остановке приложения: появится ли эта строка в логе?
    @PreDestroy
    fun preDestroy() {
        log.info("[SCOPE] @PreDestroy у PrototypeBean {}", id)
    }
}

// Контроллер сам singleton. Ему внедрены оба бина и «поставщик» prototype-бинов.
@RestController
class ScopeController(
    private val singleton: SingletonBean,
    // prototype, внедрённый напрямую: создаётся ОДИН раз, когда создаётся контроллер.
    private val prototypeInjected: PrototypeBean,
    // ObjectProvider: аналог Provider<T> в Dagger. Каждый getObject() просит у контейнера новый бин.
    private val prototypeProvider: ObjectProvider<PrototypeBean>,
) {

    @GetMapping("/lab/scopes")
    fun scopes(): Map<String, String> = mapOf(
        "singleton" to singleton.id,
        "prototypeInjected" to prototypeInjected.id,
        "prototypeFromProvider" to prototypeProvider.getObject().id,
    )
}
