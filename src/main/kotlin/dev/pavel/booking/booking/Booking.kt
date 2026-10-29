package dev.pavel.booking.booking

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

// День 3. Доменная модель — «как бронь живёт внутри сервиса».
// Это НЕ то, что уходит клиенту в JSON. Для клиента есть отдельные DTO (BookingDtos.kt).
// Аналог из Android: entity в Room против UI-модели — разные классы под разные задачи.
// TODO День 5, шаг 2: преврати Booking в JPA-сущность.
//   1) @Entity и @Table(name = "bookings") над классом (пакет jakarta.persistence).
//   2) Убери слово data: data class — плохая сущность (equals/hashCode по всем полям, toString и copy — см. конспект, раздел 3).
//      Сделай обычный class.
//   3) @Id над полем id. Генерацию не включаем: UUID назначает сервис.
//   4) Поля, которые могут меняться (статус, даты...), — var; id — val.
//   5) status: по умолчанию JPA пишет enum ЧИСЛОМ (порядковым номером). Нам нужен текст — найди нужную аннотацию.
//   Имена колонок не указывай: Spring Boot сам переведёт guestName -> guest_name.
//   Проверка, что маппинг верный: ddl-auto: validate. Ошибётся колонка или тип — приложение не стартует.
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
