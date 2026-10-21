package dev.pavel.booking.booking

import dev.pavel.booking.common.PageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.UUID

// TODO (шаг 5, Swagger): описать API для документации. Аннотации из пакета io.swagger.v3.oas.annotations
// (приходят вместе с зависимостью springdoc, ничего отдельно подключать не надо):
// - над классом: @Tag(name = "Брони", description = "...")         импорт ...annotations.tags.Tag
// - над каждым методом: @Operation(summary = "Создать бронь")       импорт ...annotations.Operation
// - над методами, где ответ НЕ 200, и где бывают ошибки:
//   @ApiResponse(responseCode = "201", description = "Создано")      импорт ...annotations.responses.ApiResponse
//   @ApiResponse(responseCode = "400", description = "Ошибка валидации")
//   (@ApiResponse можно повторять несколько раз над одним методом)
// Какие коды у какого метода — реши сам: вспомни, что реально возвращает каждый метод (проверяли в bookings.http).

// День 3. Контроллер — «тонкий» слой: принять HTTP, вызвать сервис, вернуть HTTP.
// Никакой бизнес-логики здесь нет. Аналог из Android: Fragment, который только зовёт ViewModel.
@RestController
// Общий префикс для всех методов класса. v1 — версия API: сломаем формат — сделаем v2, а старые клиенты не упадут.
@RequestMapping("/api/v1/bookings")
@Tag(name = "Брони", description = "Раздел управления бронированием")
class BookingController(
    private val service: BookingService,
) {

    // GET /api/v1/bookings — список. Статус 200 OK по умолчанию.
    //
    // TODO (твоя задача, шаг 4): сделать список постраничным.
    // Пример запроса: GET /api/v1/bookings?page=0&size=2&sortBy=CHECK_IN&direction=DESC
    // 1. Добавь в метод 4 параметра с @RequestParam(defaultValue = "..."):
    //    - page: Int        по умолчанию "0",          правило @Min(0)
    //    - size: Int        по умолчанию "20",         правила @Min(1) @Max(100)
    //    - sortBy: BookingSortField   по умолчанию "CREATED_AT"
    //    - direction: SortDirection   по умолчанию "ASC"
    //    (@Min/@Max здесь пишутся БЕЗ @field: — это параметры метода, а не поля класса)
    // 2. Тип возврата: PageResponse<BookingResponse>
    // 3. Тело: service.findPage(page, size, sortBy, direction).map { it.toResponse() }
    @Operation(summary = "Получить список всех броней")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    @ApiResponse(responseCode = "200", description = "Список броней")
    @GetMapping
    fun findAll(
        @Min(0)
        @RequestParam(defaultValue = "0") page: Int,
        @Min(1)
        @Max(20)
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "ASC") direction: SortDirection,
        @RequestParam(defaultValue = "CREATED_AT") sortBy: BookingSortField,
    ): PageResponse<BookingResponse> = service.findPage(
        page = page,
        size = size,
        sortBy = sortBy,
        direction = direction
    ).map { it.toResponse() }

    // GET /api/v1/bookings/{id} — одна бронь.
    // @PathVariable: берёт {id} из пути и сам переводит строку в UUID.
    // Аналог @Path в Retrofit.
    @Operation(summary = "Получить бронь по идентификатору")
    @ApiResponse(responseCode = "404", description = "Бронь не найдена")
    @ApiResponse(responseCode = "200", description = "Бронь найдена")
    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): BookingResponse = service.findById(id).toResponse()

    // POST /api/v1/bookings — создать.
    // @RequestBody: Jackson читает JSON из тела запроса в CreateBookingRequest (аналог @Body в Retrofit).
    // По REST при создании отвечаем 201 Created + заголовок Location с адресом нового ресурса.
    // ResponseEntity — «ответ целиком»: статус + заголовки + тело. Нужен, когда 200 по умолчанию не подходит.
    @Operation(summary = "Создать бронь")
    @ApiResponse(responseCode = "201", description = "Создано")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    @PostMapping
    fun create(@Valid @RequestBody request: CreateBookingRequest): ResponseEntity<BookingResponse> {
        val booking = service.create(request)
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(booking.id)
            .toUri()                                   // http://localhost:8080/api/v1/bookings/<id>
        return ResponseEntity.created(location).body(booking.toResponse())
    }

    @Operation(summary = "Удалить бронь")
    @ApiResponse(responseCode = "204", description = "Удалено")
    @ApiResponse(responseCode = "404", description = "Бронь не найдена")
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }
}
