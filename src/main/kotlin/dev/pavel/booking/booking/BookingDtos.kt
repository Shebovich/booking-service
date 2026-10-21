package dev.pavel.booking.booking

import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

// День 3. DTO (Data Transfer Object) — «как бронь выглядит снаружи», в JSON запросов и ответов.
//
// Зачем отдельные классы, если поля почти те же, что у Booking:
// - в запросе на создание клиент НЕ присылает id, status, createdAt — их назначает сервер;
// - внутреннюю модель можно менять (переименовать поле, добавить служебное),
//   не ломая API для клиентов (мобилки, фронта);
// - никакое внутреннее поле случайно не утечёт наружу.

// Тело запроса POST /api/v1/bookings. Только то, что клиент имеет право задать.
@ValidStayDates
data class CreateBookingRequest(
    @field:NotBlank
    val guestName: String,
    @field:NotBlank
    val roomId: String,
    @field:FutureOrPresent
    val checkIn: LocalDate,// в JSON строкой "2026-11-01" — Jackson сам переведёт в LocalDate
    val checkOut: LocalDate,
    @field:Min(1) @field:Max(20)
    val guests: Int,
)

// Тело ответа. То, что клиенту можно видеть.
data class BookingResponse(
    val id: UUID,
    val guestName: String,
    val roomId: String,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val guests: Int,
    val status: BookingStatus,
    val createdAt: Instant,
)

// Перевод «внутреннее -> внешнее». Extension-функция, как мапперы в Android.
fun Booking.toResponse() = BookingResponse(
    id = id,
    guestName = guestName,
    roomId = roomId,
    checkIn = checkIn,
    checkOut = checkOut,
    guests = guests,
    status = status,
    createdAt = createdAt,
)
