package dev.pavel.booking.lab

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.BeanNameAware
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

// День 2, шаг 1. Учебный бин: пишет в лог каждый этап своей жизни.
// Запусти приложение, потом останови (красный квадрат) и посмотри на строки с [LIFECYCLE].
// Порядок в логе = порядок жизненного цикла бина.
@Component
class LifecycleDemo(
    // Этап 2. Зависимость через конструктор: Spring сначала найдёт бин Environment,
    // а уже потом сможет вызвать этот конструктор.
    private val env: Environment
) : BeanNameAware, InitializingBean, DisposableBean {

    private val log = LoggerFactory.getLogger(javaClass)

    init {
        // Этап 1. Обычный конструктор. Аналог: Hilt создал объект через @Inject constructor.
        log.info("[LIFECYCLE] 1. конструктор, env передан: {}", env.javaClass.simpleName)
    }

    // Этап 3. Aware-интерфейсы: Spring сообщает бину служебные вещи (здесь его имя в контейнере).
    override fun setBeanName(name: String) {
        log.info("[LIFECYCLE] 3. BeanNameAware: меня зовут '{}'", name)
    }

    // Этап 4 (BeanPostProcessor.before) смотри в LoggingBeanPostProcessor.kt.

    // Этап 5. @PostConstruct: все зависимости уже есть, можно проверять настройки.
    // Аналог: onCreate() у Activity, но вызывается один раз за жизнь бина.
    @PostConstruct
    fun postConstruct() {
        log.info("[LIFECYCLE] 5. @PostConstruct")
    }

    // Этап 6. То же самое, но через интерфейс Spring. В реальном коде предпочитают @PostConstruct.
    override fun afterPropertiesSet() {
        log.info("[LIFECYCLE] 6. InitializingBean.afterPropertiesSet()")
    }

    // Этап 7 (BeanPostProcessor.after — здесь Spring может подменить бин прокси)
    // смотри в LoggingBeanPostProcessor.kt.

    // Этап 8. Бин работает, пока работает приложение.

    // Этап 9. Остановка приложения. Сначала @PreDestroy...
    @PreDestroy
    fun preDestroy() {
        log.info("[LIFECYCLE] 9. @PreDestroy")
    }

    // Этап 10. ...потом DisposableBean.destroy(). Здесь закрывают ресурсы.
    override fun destroy() {
        log.info("[LIFECYCLE] 10. DisposableBean.destroy()")
    }
}
