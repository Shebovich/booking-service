-- День 5, шаг 1. Первая миграция Flyway: таблица броней.
--
-- Правила Flyway:
-- - файл лежит в src/main/resources/db/migration (это папка по умолчанию);
-- - имя: V<версия>__<описание>.sql — две подчёркивания! Flyway применяет файлы по возрастанию версии;
-- - применённую миграцию НЕЛЬЗЯ менять: Flyway хранит её контрольную сумму в таблице flyway_schema_history
--   и при несовпадении откажется стартовать. Нужно изменить схему — пишешь новый файл V2__...sql.
--   Аналог из Android: Migration(1, 2) в Room — старые миграции не трогают, добавляют новые.
--
-- TODO: напиши create table bookings под класс Booking (Booking.kt). Подсказки:
--   - имена колонок в snake_case: guestName -> guest_name. Так Hibernate по умолчанию ищет колонки;
--   - типы: UUID -> uuid, String -> text, LocalDate -> date, Int -> int, Instant -> timestamptz;
--   - status (enum) храним текстом: 'PENDING', 'CONFIRMED', 'CANCELLED';
--   - все поля обязательные -> not null; id -> primary key;
--   - инварианты держит база (вывод дня 4): добавь check-ограничения
--     «гостей от 1 до 20» и «выезд позже заезда».
-- Как читать: create table <имя> ( <колонка> <тип> <ограничения>, ... , <ограничения таблицы> );
create table bookings (
    id          uuid        primary key,          -- primary key = not null + уникальность + индекс
    guest_name  text        not null,
    room_id     text        not null,
    check_in    date        not null,
    check_out   date        not null,
    -- check у колонки: база отклонит INSERT/UPDATE, если условие ложно
    guests      int         not null check (guests between 1 and 20),
    status      text        not null,
    created_at  timestamptz not null,
    -- check уровня таблицы: сравнивает две колонки, поэтому пишется отдельной строкой после колонок
    check (check_out > check_in)
);
