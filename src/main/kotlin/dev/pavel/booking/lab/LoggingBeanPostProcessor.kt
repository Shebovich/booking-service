package dev.pavel.booking.lab

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component

// День 2, шаг 1. BeanPostProcessor = «перехватчик» для КАЖДОГО бина в контейнере.
// Spring вызывает его дважды на каждый бин: до и после init-методов (@PostConstruct и т.д.).
// Именно так внутри Spring работают @Transactional, @Async, @Cacheable:
// их BeanPostProcessor в методе "after" возвращает не сам бин, а прокси-обёртку над ним.
@Component
class LoggingBeanPostProcessor : BeanPostProcessor {

    private val log = LoggerFactory.getLogger(javaClass)

    // Этап 4. Бин создан, зависимости проставлены, но @PostConstruct ещё НЕ вызван.
    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        if (isOurs(bean)) log.info("[LIFECYCLE] 4. BPP before: {}", beanName)
        return bean // возвращаем тот же объект, ничего не меняем
    }

    // Этап 7. Init-методы отработали. Что вернём отсюда — то и попадёт в контейнер.
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        if (isOurs(bean)) log.info("[LIFECYCLE] 7. BPP after: {} (класс {})", beanName, bean.javaClass.simpleName)
        return bean
    }

    // Spring создаёт сотни своих бинов. Логируем только наши, из пакета dev.pavel.
    private fun isOurs(bean: Any) = bean.javaClass.name.startsWith("dev.pavel")
}
