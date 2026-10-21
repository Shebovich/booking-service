package dev.pavel.booking.common

// День 3, шаг 4. Ответ-«страница»: сами элементы + сведения, чтобы клиент мог листать дальше.
// Аналог из Android: то, что ты получал от сервера для Paging 3 / бесконечного списка.
data class PageResponse<T>(
    val items: List<T>,       // элементы текущей страницы
    val page: Int,            // номер страницы, с нуля
    val size: Int,            // сколько элементов на странице просили
    val totalElements: Int,   // сколько всего элементов
    val totalPages: Int,      // сколько всего страниц
) {
    // Превратить страницу одних объектов в страницу других (Booking -> BookingResponse),
    // сохранив номера страниц. Как map у списка.
    fun <R> map(transform: (T) -> R) = PageResponse(items.map(transform), page, size, totalElements, totalPages)
}
