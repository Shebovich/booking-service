package dev.pavel.booking.config

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.validation.annotation.Validated
import java.time.Duration

// День 2, шаг 5. Все настройки раздела booking.* из application.yml — одним типизированным объектом.
// Аналог из Android: data class с настройками, который Hilt отдаёт всем, кому нужен.
//
// Как Spring заполняет этот класс:
//   booking.greeting       -> greeting
//   booking.max-guests     -> maxGuests     (kebab-case в yml сам превращается в camelCase)
//   booking.hold-timeout   -> holdTimeout   (строку "15m" Spring сам переведёт в Duration)
// Переменная окружения BOOKING_MAX_GUESTS тоже попадёт в maxGuests (relaxed binding).
@ConfigurationProperties("booking")
// @Validated: проверить значения при старте. Неверный конфиг = приложение не запустится.
// Лучше упасть сразу при деплое, чем через неделю на живом запросе.
@Validated
data class BookingProperties(

    // @field: — чтобы аннотация валидации попала на поле, где её ищет Hibernate Validator.
    @field:NotBlank
    val greeting: String,

    // Если в yml ключа нет — возьмётся 4.
    // @param: — чтобы @DefaultValue попала на параметр конструктора, где её ищет Spring.
    @field:Min(1) @field:Max(20)
    @param:DefaultValue("4")
    val maxGuests: Int,

    // Сколько держим неоплаченную бронь. В yml можно писать 15m, 30s, 2h.
    @param:DefaultValue("15m")
    val holdTimeout: Duration,
)
