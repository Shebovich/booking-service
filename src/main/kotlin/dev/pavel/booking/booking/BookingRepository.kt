package dev.pavel.booking.booking

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

// День 5. Репозиторий броней — только интерфейс: JpaRepository<сущность, тип id>.
// Реализацию (save, findById, existsById, deleteById, findAll(Pageable)...) Spring Data создаёт сам при старте.
// @Repository не нужен: Spring Boot сканирует пакет приложения и находит наследников Repository.
// Аналог из Android: @Dao-интерфейс в Room или интерфейс API в Retrofit.
interface BookingRepository : JpaRepository<Booking, UUID>
