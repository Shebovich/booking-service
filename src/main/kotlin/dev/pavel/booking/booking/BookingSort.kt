package dev.pavel.booking.booking

// День 3, шаг 4. По каким полям МОЖНО сортировать.
// Почему enum, а не просто строка с именем поля:
// - клиент не сможет попросить сортировку по полю, которого нет или которое нельзя светить;
// - когда появится база, имя поля не попадёт в SQL напрямую (защита от SQL-инъекций);
// - Spring сам переведёт строку ?sortBy=CHECK_IN в enum, а на ?sortBy=hack ответит 400.
enum class BookingSortField { CREATED_AT, CHECK_IN, GUEST_NAME }

enum class SortDirection { ASC, DESC }
