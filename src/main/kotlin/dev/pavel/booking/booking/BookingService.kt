package dev.pavel.booking.booking

import dev.pavel.booking.common.PageResponse
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

// День 3. Бизнес-логика. Здесь решается «что можно, а что нельзя» с бронью.
// Сервис ничего не знает про HTTP: ни статусов, ни заголовков, ни JSON.
// Поэтому его легко тестировать и можно вызвать откуда угодно (из Kafka-консьюмера на дне 15, например).
@Service
class BookingService(
    private val repository: BookingRepository,
) {

    fun create(request: CreateBookingRequest): Booking {
        val booking = Booking(
            id = UUID.randomUUID(),            // id назначает сервер, а не клиент
            guestName = request.guestName,
            roomId = request.roomId,
            checkIn = request.checkIn,
            checkOut = request.checkOut,
            guests = request.guests,
            status = BookingStatus.PENDING,    // новая бронь всегда ждёт подтверждения
            createdAt = Instant.now(),
        )
        return repository.save(booking)
    }

    fun findAll(): List<Booking> = repository.findAll()

    // День 3, шаг 4. Страница броней с сортировкой.
    // Пока сортируем и режем список в памяти. На дне 8 это сделает PostgreSQL
    // (ORDER BY ... LIMIT ... OFFSET ...) — и сигнатура метода не изменится.
    fun findPage(page: Int, size: Int, sortBy: BookingSortField, direction: SortDirection): PageResponse<Booking> {
        val all = repository.findAll()

        val comparator: Comparator<Booking> = when (sortBy) {
            BookingSortField.CREATED_AT -> compareBy { it.createdAt }
            BookingSortField.CHECK_IN -> compareBy { it.checkIn }
            BookingSortField.GUEST_NAME -> compareBy { it.guestName }
        }
        val sorted = all.sortedWith(if (direction == SortDirection.DESC) comparator.reversed() else comparator)

        // drop = пропустить предыдущие страницы, take = взять одну страницу
        val items = sorted.drop(page * size).take(size)
        val totalPages = (all.size + size - 1) / size   // деление с округлением вверх
        return PageResponse(items, page, size, all.size, totalPages)
    }

    // Нет брони — бросаем исключение. Решать, какой HTTP-статус вернуть, будет не сервис.
    fun findById(id: UUID): Booking =
        repository.findById(id) ?: throw BookingNotFoundException(id)

    fun delete(id: UUID) {
        if (!repository.deleteById(id)) throw BookingNotFoundException(id)
    }
}
