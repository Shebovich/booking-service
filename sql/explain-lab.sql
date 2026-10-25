-- День 4, шаг 4. Индексы и EXPLAIN на миллионе строк.
--
-- Где запускать: в IntelliJ выбери вверху справа источник данных booking (НЕ exercises).
-- Запуск запроса под курсором — Ctrl+Enter. План EXPLAIN появится внизу таблицей, по строке на узел.
--
-- Как работать с каждым опытом:
--   1) прочитай вопрос «ПРЕДСКАЖИ» и запиши ответ в комментарий ДО запуска;
--   2) запусти EXPLAIN ANALYZE;
--   3) там, где TODO, — напиши индекс сам и запусти EXPLAIN ANALYZE ещё раз;
--   4) запиши в комментарий, что увидел: тип узла (Seq Scan / Index Scan / ...) и Execution Time.
-- Числа времени у тебя будут другие, чем у меня, — важны тип узла и порядок величин.

-- ============================================================================
-- 0. Подготовка: таблица на 1 млн броней (генерация ~4 секунды, таблица ~110 МБ)
-- ============================================================================

drop table if exists lab_bookings;

create table lab_bookings (
    id          bigint generated always as identity primary key,  -- первичный ключ = индекс создаётся сам
    guest_email text        not null,
    room_id     int         not null,   -- 500 номеров
    check_in    date        not null,   -- случайная дата 2026 года
    status      text        not null,   -- ~90% CONFIRMED, ~5% PENDING, ~5% CANCELLED
    created_at  timestamptz not null
);

-- generate_series(1, 1000000) — «цикл» на миллион строк прямо в SQL. Аналог (1..1_000_000).map { } в Kotlin.
insert into lab_bookings (guest_email, room_id, check_in, status, created_at)
select 'guest' || g || '@mail.test',
       1 + (random() * 499)::int,
       date '2026-01-01' + (random() * 364)::int,
       case when random() < 0.90 then 'CONFIRMED' when random() < 0.5 then 'PENDING' else 'CANCELLED' end,
       now() - random() * interval '365 days'
from generate_series(1, 1000000) g;

-- Собрать статистику: сколько строк, какие значения частые. По ней планировщик выбирает план.
analyze lab_bookings;

-- Проверка: должно быть ~900 тыс. CONFIRMED и по ~50 тыс. остальных.
select status, count(*) from lab_bookings group by status;

-- ============================================================================
-- Опыт 1. Поиск по email: без индекса и с индексом
-- ============================================================================
-- ПРЕДСКАЖИ: какой узел будет в плане сейчас и почему?
-- Ответ:

explain analyze select * from lab_bookings where guest_email = 'guest777777@mail.test';

-- TODO: создай обычный B-tree индекс на guest_email (create index <имя> on <таблица> (<колонка>);)
--       и запусти EXPLAIN ANALYZE выше ещё раз. Во сколько раз изменилось Execution Time?
-- Индекс:
create index zakladka on lab_bookings(guest_email);
-- Что увидел:
    -- До индекса
-- Gather  (cost=1000.00..17457.43 rows=1 width=54) (actual time=14.207..20.141 rows=1.00 loops=1)
--   Workers Planned: 2
--   Workers Launched: 2
--   Buffers: shared hit=11249
--   ->  Parallel Seq Scan on lab_bookings  (cost=0.00..16457.33 rows=1 width=54) (actual time=15.252..16.576 rows=0.33 loops=3)
--         Filter: (guest_email = 'guest777777@mail.test'::text)
--         Rows Removed by Filter: 333333
--         Buffers: shared hit=11249
-- Planning:
--   Buffers: shared hit=6
-- Planning Time: 0.069 ms
-- Execution Time: 20.155 ms
    -- после индекса
-- Index Scan using zakladka on lab_bookings  (cost=0.42..8.44 rows=1 width=54) (actual time=0.017..0.018 rows=1.00 loops=1)
--   Index Cond: (guest_email = 'guest777777@mail.test'::text)
--   Index Searches: 1
--   Buffers: shared hit=4
-- Planning Time: 0.068 ms
-- Execution Time: 0.026 ms

-- ============================================================================
-- Опыт 2. Функция над колонкой
-- ============================================================================
-- Индекс на guest_email из опыта 1 уже есть.
-- ПРЕДСКАЖИ: используется ли он здесь?
-- Ответ: да

explain analyze select * from lab_bookings where lower(guest_email) = 'guest777777@mail.test';

-- TODO: сделай так, чтобы этот запрос стал быстрым. Подсказка: в Postgres индекс можно построить
--       не только по колонке, но и по выражению — create index ... on lab_bookings (<выражение>);
--       Выражение в индексе должно совпадать с выражением в WHERE.
-- Индекс:
create index zakladkatwo on lab_bookings(lower(guest_email));
-- Что увидел:
-- Gather  (cost=1000.00..18999.00 rows=5000 width=54) (actual time=72.794..95.162 rows=1.00 loops=1)
--   Workers Planned: 2
--   Workers Launched: 2
--   Buffers: shared hit=11249
--   ->  Parallel Seq Scan on lab_bookings  (cost=0.00..17499.00 rows=2083 width=54) (actual time=84.623..91.342 rows=0.33 loops=3)
--         Filter: (lower(guest_email) = 'guest777777@mail.test'::text)
--         Rows Removed by Filter: 333333
--         Buffers: shared hit=11249
-- Planning Time: 0.038 ms
-- Execution Time: 95.174 ms До индекса закладка два
-- Gather  (cost=1000.00..18999.00 rows=5000 width=54) (actual time=73.124..95.144 rows=1.00 loops=1)
--   Workers Planned: 2
--   Workers Launched: 2
--   Buffers: shared hit=11249
--   ->  Parallel Seq Scan on lab_bookings  (cost=0.00..17499.00 rows=2083 width=54) (actual time=85.031..91.735 rows=0.33 loops=3)
--         Filter: (lower(guest_email) = 'guest777777@mail.test'::text)
--         Rows Removed by Filter: 333333
--         Buffers: shared hit=11249
-- Planning Time: 0.037 ms
-- Execution Time: 95.158 ms
-- После создания индекса
-- Bitmap Heap Scan on lab_bookings  (cost=139.18..9176.87 rows=5000 width=54) (actual time=0.024..0.025 rows=1.00 loops=1)
--   Recheck Cond: (lower(guest_email) = 'guest777777@mail.test'::text)
--   Heap Blocks: exact=1
--   Buffers: shared hit=1 read=3
--   ->  Bitmap Index Scan on zakladkatwo  (cost=0.00..137.93 rows=5000 width=0) (actual time=0.019..0.019 rows=1.00 loops=1)
--         Index Cond: (lower(guest_email) = 'guest777777@mail.test'::text)
--         Index Searches: 1
--         Buffers: shared read=3
-- Planning:
--   Buffers: shared hit=18 read=1
-- Planning Time: 0.145 ms
-- Execution Time: 0.034 ms
-- ============================================================================
-- Опыт 3. LIKE: поиск по началу и по концу строки
-- ============================================================================
-- Индекс на guest_email из опыта 1 есть. Здесь хватит EXPLAIN без ANALYZE — смотрим только план.
-- ПРЕДСКАЖИ для каждого запроса: Seq Scan или индекс?
-- Ответ (начало строки): seq scan
-- Ответ (конец строки): seq scan

explain select * from lab_bookings where guest_email like 'guest77777%';
explain select * from lab_bookings where guest_email like '%7777@mail.test';

-- Если результат удивил — напиши мне, разберём (тут есть ловушка, связанная с настройками базы).
-- Что увидел: стоимость процента вначале дороже Gather  (cost=1000.00..17467.33 rows=100 width=54)

-- TODO 3б: сделай так, чтобы запрос «по началу строки» пошёл по индексу.
--   Подсказка: у B-tree индекса можно указать «класс операторов» — как сравнивать значения.
--   Пишется после колонки через пробел: create index <имя> on <таблица> (<колонка> <класс>);
--   Нужный класс для text сравнивает строки посимвольно, без правил языка (collation),
--   и называется text_..._ops (середину угадай по смыслу: LIKE — это поиск по шаблону).
--   Потом снова запусти оба EXPLAIN. Какой узел у первого? Изменился ли второй?
-- Индекс:
create index zakladkatext on lab_bookings(guest_email TEXT_PATTERN_OPS);
drop index zakladkatext;
-- Что увидел:
    -- Index Scan using zakladkatext on lab_bookings  (cost=0.42..8.45 rows=100 width=54)
--   Index Cond: ((guest_email ~>=~ 'guest77777'::text) AND (guest_email ~<~ 'guest77778'::text))
--   Filter: (guest_email ~~ 'guest77777%'::text)
-- Gather  (cost=1000.00..17467.33 rows=100 width=54)
--   Workers Planned: 2
--   ->  Parallel Seq Scan on lab_bookings  (cost=0.00..16457.33 rows=42 width=54)
--         Filter: (guest_email ~~ '%7777@mail.test'::text)
-- ============================================================================
-- Опыт 4. Составной индекс и порядок колонок
-- ============================================================================
-- TODO: создай ОДИН индекс сразу на две колонки: сначала room_id, потом check_in.
-- Индекс:
create index room_check_in on lab_bookings(room_id, check_in);
-- ПРЕДСКАЖИ для каждого запроса: будет ли использован этот индекс?
-- Ответ (обе колонки): Да
-- Ответ (только check_in, вторая колонка индекса): Нет

explain analyze select * from lab_bookings where room_id = 42 and check_in between '2026-03-01' and '2026-03-31';
-- Bitmap Heap Scan on lab_bookings  (cost=6.48..592.05 rows=161 width=54) (actual time=0.203..0.302 rows=149.00 loops=1)
--   Recheck Cond: ((room_id = 42) AND (check_in >= '2026-03-01'::date) AND (check_in <= '2026-03-31'::date))
--   Heap Blocks: exact=149
--   Buffers: shared hit=149 read=3
--   ->  Bitmap Index Scan on room_check_in  (cost=0.00..6.44 rows=161 width=0) (actual time=0.177..0.177 rows=149.00 loops=1)
--         Index Cond: ((room_id = 42) AND (check_in >= '2026-03-01'::date) AND (check_in <= '2026-03-31'::date))
--         Index Searches: 1
--         Buffers: shared read=3
-- Planning:
--   Buffers: shared hit=31 read=1
-- Planning Time: 0.175 ms
-- Execution Time: 0.329 ms
explain analyze select * from lab_bookings where check_in = '2026-03-15';
 -- Bitmap Heap Scan on lab_bookings  (cost=1810.76..8178.50 rows=2725 width=54) (actual time=5.568..6.840 rows=2632.00 loops=1)
 --   Recheck Cond: (check_in = '2026-03-15'::date)
 --   Heap Blocks: exact=2364
 --   Buffers: shared hit=3366 read=504
 --   ->  Bitmap Index Scan on room_check_in  (cost=0.00..1810.08 rows=2725 width=0) (actual time=5.363..5.363 rows=2632.00 loops=1)
 --         Index Cond: (check_in = '2026-03-15'::date)
 --         Index Searches: 501
 --         Buffers: shared hit=1002 read=504
 -- Planning:
 --   Buffers: shared hit=6
 -- Planning Time: 0.116 ms
 -- Execution Time: 6.929 ms
-- Во втором плане найди строку «Index Searches». Что она значит и почему там такое число?
-- Что увидел: 501 без понятия

-- ============================================================================
-- Опыт 5. Индекс есть, а база его не берёт
-- ============================================================================
-- TODO: создай индекс на status.
-- Индекс:
create index status_index on lab_bookings(status);
-- ПРЕДСКАЖИ: где индекс будет использован, а где нет? (CANCELLED ~5% строк, CONFIRMED ~90%)
-- Ответ: CANCELLED

explain analyze select * from lab_bookings where status = 'CANCELLED';
explain analyze select * from lab_bookings where status = 'CONFIRMED';

-- Что увидел и почему база так решила:
-- Bitmap Heap Scan on lab_bookings  (cost=551.40..12411.65 rows=48900 width=54) (actual time=3.029..32.991 rows=49960.00 loops=1)
--   Recheck Cond: (status = 'CANCELLED'::text)
--   Heap Blocks: exact=11126
--   Buffers: shared hit=185 read=10986
--   ->  Bitmap Index Scan on status_index  (cost=0.00..539.17 rows=48900 width=0) (actual time=1.564..1.565 rows=49960.00 loops=1)
--         Index Cond: (status = 'CANCELLED'::text)
--         Index Searches: 1
--         Buffers: shared read=45
-- Planning:
--   Buffers: shared hit=77 read=10
-- Planning Time: 4.262 ms
-- Execution Time: 34.365 ms
-- Seq Scan on lab_bookings  (cost=0.00..23749.00 rows=900400 width=54) (actual time=0.013..100.080 rows=899966.00 loops=1)
--   Filter: (status = 'CONFIRMED'::text)
--   Rows Removed by Filter: 100034
--   Buffers: shared hit=11129 read=120
-- Planning Time: 0.054 ms
-- Execution Time: 125.327 ms
-- ============================================================================
-- Опыт 6 (бонус). Index Only Scan
-- ============================================================================
-- Запрос просит ТОЛЬКО колонки, которые есть в индексе из опыта 4.
explain analyze select room_id, check_in from lab_bookings where room_id = 42;

-- Теперь выполни команду ниже и запусти тот же EXPLAIN ещё раз. Какой стал узел и что изменилось?
vacuum lab_bookings;

-- Что увидел: не понял что делает vacuum поэтому ничего не увидел

-- TODO 6б: повтори опыт, зная теорию из чата. vacuum ты уже запускал, поэтому начнём с «испорченной» карты видимости.
--   1) Запусти EXPLAIN ANALYZE из опыта 6 и запиши узел и строку Heap Fetches.
--   2) Создай новые версии строк номера 42 (значение не меняется, но для MVCC это всё равно UPDATE = новая версия):
-- Index Only Scan using room_check_in on lab_bookings  (cost=0.42..47.35 rows=1996 width=8) (actual time=0.019..0.154 rows=1991.00 loops=1)
--   Index Cond: (room_id = 42)
--   Heap Fetches: 0
--   Index Searches: 1
--   Buffers: shared hit=7
-- Planning Time: 0.056 ms
-- Execution Time: 0.213 ms
-- Index Only Scan using room_check_in on lab_bookings  (cost=0.42..63.42 rows=2000 width=8) (actual time=0.030..5.928 rows=1991.00 loops=1)
--   Index Cond: (room_id = 42)
--   Heap Fetches: 3977
--   Index Searches: 1
--   Buffers: shared hit=3770
-- Planning Time: 0.045 ms
-- Execution Time: 5.994 ms
-- Index Only Scan using room_check_in on lab_bookings  (cost=0.42..47.35 rows=1996 width=8) (actual time=0.016..0.129 rows=1991.00 loops=1)
--   Index Cond: (room_id = 42)
--   Heap Fetches: 0
--   Index Searches: 1
--   Buffers: shared hit=9
-- Planning:
--   Buffers: shared hit=20
-- Planning Time: 0.089 ms
-- Execution Time: 0.186 ms
update lab_bookings set status = status where room_id = 42;
--   3) Снова EXPLAIN ANALYZE опыта 6. Сколько теперь Heap Fetches? Сравни с числом rows. Почему примерно вдвое больше?
--   4) Запусти vacuum lab_bookings; и EXPLAIN ещё раз. Что стало с Heap Fetches и Execution Time?
-- Что увидел:

-- ============================================================================
-- Опыт 7. Цена индексов
-- ============================================================================
-- Сколько места занимают таблица и каждый индекс. Сравни сумму индексов с размером таблицы.
select pg_size_pretty(pg_relation_size('lab_bookings')) as table_size;
select indexrelname, pg_size_pretty(pg_relation_size(indexrelid)) as index_size
from pg_stat_user_indexes
where relname = 'lab_bookings';
-- 88 MB
-- Вывод: чем платим за каждый индекс (подумай про место И про insert/update):
-- lab_bookings_pkey,21 MB
-- status_index,6904 kB
-- room_check_in,11 MB
-- Платим памятью и уменьшением скорости вставки и обновления. Поэтому надо проектировать индексы таким образом чтоб их было меньше и качественнее вплане скорости
