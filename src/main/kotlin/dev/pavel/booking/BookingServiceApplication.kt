package dev.pavel.booking

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@OpenAPIDefinition(info = Info(title = "Booking API", version = "v1", description = "API бронирования"))
@SpringBootApplication
@ConfigurationPropertiesScan
class BookingServiceApplication

fun main(args: Array<String>) {
	runApplication<BookingServiceApplication>(*args)
}
