package dev.pavel.booking.booking

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

// День 3. Доменная модель — «как бронь живёт внутри сервиса».
// Это НЕ то, что уходит клиенту в JSON. Для клиента есть отдельные DTO (BookingDtos.kt).
// Аналог из Android: entity в Room против UI-модели — разные классы под разные задачи.
data class Booking(
    val id: UUID,
    val guestName: String,
    val roomId: String,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val guests: Int,
    val status: BookingStatus,
    val createdAt: Instant,
)

enum class BookingStatus { PENDING, CONFIRMED, CANCELLED }

// Своё исключение «бронь не найдена». Пока сервис бросает его, а Spring превращает в 500.
// На шаге 3 научим Spring отвечать на него правильным статусом 404.
class BookingNotFoundException(id: UUID) : RuntimeException("Бронь $id не найдена")
