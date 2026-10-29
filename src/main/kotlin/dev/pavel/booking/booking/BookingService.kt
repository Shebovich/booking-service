package dev.pavel.booking.booking

import dev.pavel.booking.common.PageResponse
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

// День 3. Бизнес-логика. Здесь решается «что можно, а что нельзя» с бронью.
// Сервис ничего не знает про HTTP: ни статусов, ни заголовков, ни JSON.
// Поэтому его легко тестировать и можно вызвать откуда угодно (из Kafka-консьюмера на дне 15, например).
// TODO День 5, шаг 2: переведи сервис на JpaRepository.
//   - @Transactional (org.springframework.transaction.annotation) на методы, которые пишут: create, delete;
//     @Transactional(readOnly = true) — на методы, которые только читают.
//   - findById: JpaRepository.findById возвращает Optional. В Kotlin есть расширение findByIdOrNull
//     (org.springframework.data.repository.findByIdOrNull) — оно вернёт Booking?.
//   - delete: deleteById ничего не возвращает. Как узнать, была ли бронь? Есть метод existsById.
//   - findPage: сортировку и LIMIT/OFFSET должна сделать база, а не Kotlin.
//       PageRequest.of(page, size, Sort.by(направление, "имяПоляВСущности")) -> repository.findAll(pageable) -> Page<Booking>.
//       Имя поля — Kotlin-имя (createdAt), не колонка (created_at). Переведи BookingSortField в эту строку через when.
//       Добавь вторую сортировку по id (.and(Sort.by("id"))): при одинаковых значениях порядок станет стабильным,
//       и записи не будут «прыгать» между страницами.
//       Из Page возьми content, totalElements, totalPages и собери наш PageResponse.
//   - findAll() без страниц можно удалить, если он нигде не используется (проверь через Find Usages).
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
