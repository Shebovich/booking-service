package dev.pavel.booking.booking

import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

// День 3. Хранилище броней. Пока в памяти — на дне 8 заменим на PostgreSQL.
// Контроллер и сервис не узнают о замене: они работают только через эти методы.
//
// ConcurrentHashMap, а не обычный HashMap — вспомни день 1:
// бин один, а запросы идут из 200 потоков Tomcat одновременно.
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
