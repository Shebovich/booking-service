package dev.pavel.booking.booking

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

// День 3. Доменная модель — «как бронь живёт внутри сервиса».
// Это НЕ то, что уходит клиенту в JSON. Для клиента есть отдельные DTO (BookingDtos.kt).
// Аналог из Android: entity в Room против UI-модели — разные классы под разные задачи.
//
// День 5. Теперь это JPA-сущность — строка таблицы bookings (таблицу создаёт Flyway, V1).
// - обычный class, не data class: equals/hashCode по всем полям, toString и copy ломают работу Hibernate;
// - id назначает сервис (UUID), поэтому без @GeneratedValue;
// - var — поля, которые меняются (Hibernate сам заметит изменение: dirty checking), val — неизменные;
// - enum храним текстом (STRING): ORDINAL ломается, если вставить новое значение в середину enum;
// - колонки не указываем: Spring Boot переводит guestName -> guest_name, а ddl-auto: validate проверит при старте.
@Entity
@Table(name = "bookings")
class Booking(
    @Id
    val id: UUID,
    var guestName: String,
    var roomId: String,
    var checkIn: LocalDate,
    var checkOut: LocalDate,
    var guests: Int,
    @Enumerated(EnumType.STRING)
    var status: BookingStatus,
    val createdAt: Instant,
)

enum class BookingStatus { PENDING, CONFIRMED, CANCELLED }

// Своё исключение «бронь не найдена». Пока сервис бросает его, а Spring превращает в 500.
// На шаге 3 научим Spring отвечать на него правильным статусом 404.
class BookingNotFoundException(id: UUID) : RuntimeException("Бронь $id не найдена")
