package dev.pavel.booking.booking

import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

// День 3. Хранилище броней. Пока в памяти — на дне 8 заменим на PostgreSQL.
// Контроллер и сервис не узнают о замене: они работают только через эти методы.
//
// ConcurrentHashMap, а не обычный HashMap — вспомни день 1:
// бин один, а запросы идут из 200 потоков Tomcat одновременно.
// TODO День 5, шаг 2: замени этот класс на интерфейс, который наследует JpaRepository<сущность, тип id>.
//   Реализацию (save, findById, findAll, deleteById, existsById, findAll(Pageable)...) Spring создаст сам при старте.
//   @Repository и ConcurrentHashMap больше не нужны: потоки разведёт база (MVCC из дня 4).
//   После замены сервис перестанет компилироваться — это и есть следующая часть задания (BookingService.kt).
@Repository
class BookingRepository {

    private val storage = ConcurrentHashMap<UUID, Booking>()

    fun save(booking: Booking): Booking {
        storage[booking.id] = booking
        return booking
    }

    fun findById(id: UUID): Booking? = storage[id]

    fun findAll(): List<Booking> = storage.values.sortedBy { it.createdAt }

    fun deleteById(id: UUID): Boolean = storage.remove(id) != null
}
