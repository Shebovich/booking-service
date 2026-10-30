package dev.pavel.booking.booking

import dev.pavel.booking.common.PageResponse
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

// День 3. Бизнес-логика. Здесь решается «что можно, а что нельзя» с бронью.
// Сервис ничего не знает про HTTP: ни статусов, ни заголовков, ни JSON.
// Поэтому его легко тестировать и можно вызвать откуда угодно (из Kafka-консьюмера на дне 15, например).
@Service
class BookingService(
    private val repository: BookingRepository,
) {

    @Transactional
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

    // Страница броней с сортировкой. С дня 5 сортирует и режет база: ORDER BY ... OFFSET ... FETCH FIRST ... ROWS ONLY,
    // плюс отдельный select count(*) для totalElements. Сигнатура метода с дня 3 не изменилась.
    @Transactional(readOnly = true)
    fun findPage(page: Int, size: Int, sortBy: BookingSortField, direction: SortDirection): PageResponse<Booking> {
        val fieldName = when (sortBy) {
            BookingSortField.CREATED_AT -> "createdAt"
            BookingSortField.CHECK_IN -> "checkIn"
            BookingSortField.GUEST_NAME -> "guestName"
        }
        val sortDirection = when (direction) {
            SortDirection.ASC -> Sort.Direction.ASC
            SortDirection.DESC -> Sort.Direction.DESC
        }
        val pageable = PageRequest.of(
            page, size, Sort.by(sortDirection, fieldName)
                .and(Sort.by("id"))
        )
        val result = repository.findAll(pageable)
        return PageResponse(result.content, result.number, result.size, result.totalElements.toInt(), result.totalPages)
    }

    @Transactional(readOnly = true)
    // Нет брони — бросаем исключение. Решать, какой HTTP-статус вернуть, будет не сервис.
    fun findById(id: UUID): Booking =
        repository.findByIdOrNull(id) ?: throw BookingNotFoundException(id)

    @Transactional
    fun delete(id: UUID) {
        if (!repository.existsById(id)) throw BookingNotFoundException(id)
        repository.deleteById(id)
    }
}
