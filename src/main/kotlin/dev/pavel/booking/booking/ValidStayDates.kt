package dev.pavel.booking.booking

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

// День 3, шаг 2. Свой валидатор: «дата выезда позже даты заезда».
// Стандартные @NotBlank/@Min проверяют ОДНО поле. Здесь нужно сравнить ДВА поля,
// поэтому аннотация вешается на весь класс, а не на поле.

// 1. Аннотация. Три обязательных параметра (message, groups, payload) требует стандарт Bean Validation.
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [StayDatesValidator::class]) // кто будет проверять
annotation class ValidStayDates(
    val message: String = "Дата выезда должна быть позже даты заезда",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

// 2. Сам проверяющий. Его создаёт и вызывает Hibernate Validator.
//    Вернул false — объект невалиден, сообщение возьмётся из message аннотации.
class StayDatesValidator : ConstraintValidator<ValidStayDates, CreateBookingRequest> {
    override fun isValid(value: CreateBookingRequest?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true // null проверяют другие аннотации, не мы
        return value.checkOut.isAfter(value.checkIn)
    }
}
