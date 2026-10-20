package dev.pavel.booking.lab

import org.aopalliance.intercept.MethodInterceptor
import org.slf4j.LoggerFactory
import org.springframework.aop.framework.ProxyFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component

// День 2, шаг 6. Своя аннотация + свой BeanPostProcessor, который делает прокси.
// Это уменьшенная копия того, как Spring реализует @Async, @Transactional, @Cacheable.

// 1. Аннотация-метка. Сама по себе ничего не делает — это просто «наклейка» на методе.
//    RUNTIME — чтобы метку можно было прочитать рефлексией во время работы программы.
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class LogTime

// 2. BeanPostProcessor: смотрит на каждый бин и решает, нужна ли ему обёртка.
@Component
class LogTimeBeanPostProcessor : BeanPostProcessor {

    private val log = LoggerFactory.getLogger(javaClass)

    // Работаем в "after": init-методы бина уже отработали, можно подменять объект.
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        // Есть ли у класса хоть один метод с наклейкой @LogTime?
        val hasAnnotatedMethods = bean.javaClass.methods.any { it.isAnnotationPresent(LogTime::class.java) }
        if (!hasAnnotatedMethods) return bean // обёртка не нужна — возвращаем объект как есть

        // 3. Строим прокси. ProxyFactory — инструмент Spring, внутри использует CGLIB.
        val factory = ProxyFactory(bean)    // bean — это target, настоящий объект внутри прокси
        factory.isProxyTargetClass = true   // прокси = наследник класса (CGLIB), а не интерфейс

        // 4. Interceptor — код «секретаря». Вызывается вместо КАЖДОГО публичного метода бина.
        factory.addAdvice(MethodInterceptor { invocation ->
            val method = invocation.method
            if (!method.isAnnotationPresent(LogTime::class.java)) {
                invocation.proceed() // метод без наклейки — просто пропускаем к target
            } else {
                val start = System.nanoTime()
                try {
                    invocation.proceed() // вызов настоящего метода у target
                } finally {
                    val micros = (System.nanoTime() - start) / 1_000
                    log.info("[TIME] {}.{}() занял {} мкс", beanName, method.name, micros)
                }
            }
        })

        log.info("[TIME] бин '{}' обёрнут в прокси", beanName)
        // 5. Возвращаем ПРОКСИ вместо bean. Именно он попадёт в контейнер.
        return factory.proxy
    }
}
