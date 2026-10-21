package dev.pavel.booking.common

import dev.pavel.booking.booking.BookingNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

// День 3, шаг 3. Единая точка обработки ошибок для ВСЕХ контроллеров.
//
// @RestControllerAdvice: «советник» для всех @RestController. Его методы с @ExceptionHandler
// срабатывают, когда из контроллера вылетело исключение нужного типа.
// Аналог из Android: один общий CoroutineExceptionHandler вместо try/catch в каждой корутине.
//
// ResponseEntityExceptionHandler — базовый класс от Spring. Он уже умеет превращать
// ВСЕ стандартные ошибки Spring MVC (кривой JSON, не тот метод, не тот тип в пути...)
// в ответ формата ProblemDetail (RFC 9457). Мы наследуемся и добавляем своё.
@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    private val log = LoggerFactory.getLogger(javaClass)

    // 1. Ошибки валидации @Valid. Базовый класс сам вернёт 400,
    //    но в ответе будет только «Invalid request content.» — клиент не поймёт, что исправлять.
    //    Переопределяем: добавляем список ошибок по полям.
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        // Ошибки отдельных полей: @NotBlank на guestName, @Min на guests...
        val fieldErrors = ex.bindingResult.fieldErrors.map {
            mapOf("field" to it.field, "message" to it.defaultMessage)
        }
        // Ошибки всего объекта: наш @ValidStayDates висит на классе, у него нет одного поля.
        val globalErrors = ex.bindingResult.globalErrors.map {
            mapOf("field" to null, "message" to it.defaultMessage)
        }

        // ex.body — это уже готовый ProblemDetail. Дополняем его.
        val problem = ex.body
        problem.title = "Ошибка валидации"
        problem.detail = "Запрос содержит неверные данные"
        // Нестандартное поле. Jackson выведет его в JSON на верхнем уровне, рядом с title и status.
        problem.setProperty("errors", fieldErrors + globalErrors)

        return super.handleMethodArgumentNotValid(ex, headers, status, request)
    }

    // 2. TODO (твоя задача): BookingNotFoundException -> 404.
    //    - метод с аннотацией @ExceptionHandler(BookingNotFoundException::class)
    //    - принимает ex: BookingNotFoundException, возвращает ProblemDetail
    //    - внутри: ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message ?: "Не найдено")
    //      и задай title = "Бронь не найдена"
    //    - импорт: dev.pavel.booking.booking.BookingNotFoundException
    @ExceptionHandler(BookingNotFoundException::class)
    fun handleNotFound(ex: BookingNotFoundException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message ?: "Не найдено").apply {
            title = "Бронь не найдена"
        }
    }

    // 4. TODO (бонус, шаг 4): ошибки валидации ПАРАМЕТРОВ метода (@Min/@Max на page, size).
    //    Это другое исключение, не MethodArgumentNotValidException:
    //    @Valid на теле запроса -> MethodArgumentNotValidException (п. 1),
    //    @Min/@Max на @RequestParam/@PathVariable -> HandlerMethodValidationException.
    //    Сейчас базовый класс отвечает «Validation failure» — непонятно, какой параметр плохой.
    //    Сделай так же, как в п. 1:
    //    - override fun handleHandlerMethodValidationException(ex, headers, status, request): ResponseEntity<Any>?
    //      (типы параметров — как в п. 1, только ex: HandlerMethodValidationException,
    //       импорт org.springframework.web.method.annotation.HandlerMethodValidationException)
    //    - ex.parameterValidationResults — список результатов, по одному на каждый плохой параметр
    //    - у каждого: it.methodParameter.parameterName — имя параметра ("size"),
    //                 it.resolvableErrors — ошибки, у каждой .defaultMessage
    //      (resolvableErrors — список, тут пригодится flatMap)
    //    - дальше как в п. 1: ex.body, title, detail, setProperty("errors", ...), вызов super
    //    Проверка: запрос 9 должен вернуть errors = [{field: "size", message: "должно быть не больше ..."}]
    override fun handleHandlerMethodValidationException(
        ex: HandlerMethodValidationException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val validationResults = ex.parameterValidationResults.flatMap { result ->
            result.resolvableErrors.map { error ->
                mapOf("field" to result.methodParameter.parameterName, "message" to error.defaultMessage)
            }
        }

        val problem = ex.body
        problem.title = "Ошибка валидации"
        problem.detail = "Запрос содержит неверные данные"
        // Нестандартное поле. Jackson выведет его в JSON на верхнем уровне, рядом с title и status.
        problem.setProperty("errors", validationResults)

        return super.handleHandlerMethodValidationException(ex, headers, status, request)
    }

    // 3. Всё остальное, что мы не предусмотрели — честный 500.
    //    Но БЕЗ подробностей наружу: текст исключения и стектрейс могут раскрыть
    //    устройство сервиса (имена таблиц, пути, версии). Подробности — только в наш лог.
    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ProblemDetail {
        log.error("Необработанная ошибка", ex)
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера").apply {
            title = "Internal Server Error"
        }
    }
}
