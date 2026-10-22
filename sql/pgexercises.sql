-- День 4. Решения задач с https://pgexercises.com — разделы Basic, Joins, Aggregates.
--
-- Как загрузить их базу к себе (один раз). Скачай файл https://pgexercises.com/dbfiles/clubdata.sql
-- (например, в Загрузки), потом в терминале из той папки, где он лежит:
--   docker cp clubdata.sql booking-postgres:/tmp/clubdata.sql
--   docker exec booking-postgres psql -U booking -d postgres -q -f /tmp/clubdata.sql
-- Скрипт сам создаст базу exercises и схему cd: 31 участник, 9 объектов, 4044 брони
-- (проверено на Postgres 18.6: дамп 2013 года грузится без ошибок).
--
-- В IntelliJ: в панели Database добавь второй Data Source PostgreSQL с database = exercises
-- (остальное как у booking). Выполнить запрос под курсором — Ctrl+Enter.
--
-- Схема (три таблицы, всё в схеме cd):
--   cd.members    — участники клуба: memid, surname, firstname, joindate, recommendedby (-> members.memid)
--   cd.facilities — объекты: facid, name, membercost, guestcost, monthlymaintenance
--   cd.bookings   — брони: bookid, facid (-> facilities), memid (-> members), starttime, slots (1 слот = 30 минут)
--
-- Правила:
-- 1. Сначала решаешь сам и проверяешь на сайте (кнопка проверки там есть), только потом смотришь ответ.
-- 2. Сюда записываешь СВОЁ решение. Если подсмотрел — пометь «-- подсмотрел» и почему не вышло.
-- 3. Сортировка у тебя может отличаться от сайта: у них C-локаль, у тебя — нет (так пишут сами авторы).

-- ===== Basic =====

-- 1. Retrieve everything from a table
select * from cd.facilities;
-- 2. Retrieve specific columns from a table
select name, membercost from cd.facilities;
-- 3. Control which rows are retrieved
select * from cd.facilities where membercost>0;
-- 4. Control which rows are retrieved - part 2
select facid, name, membercost, monthlymaintenance from cd.facilities where membercost> 0 AND membercost<monthlymaintenance/50;
-- 5. Basic string searches
select * from cd.facilities where name LIKE '%Tennis%';
-- 6. Matching against multiple possible values
select * from cd.facilities where facid in ('1', '5');
-- 7. Classify results into buckets
select name, CASE
                 WHEN monthlymaintenance>100 THEN 'expensive'
                 ELSE 'cheap'
END as cost
from cd.facilities
-- 8. Working with dates
select memid, surname, firstname, joindate from cd.members where joindate>='2012-09-01';
-- 9. Removing duplicates, and ordering results
select DISTINCT surname from cd.members  order by  surname  LIMIT 10;
-- 10. Combining results from multiple queries
select surname FROM cd.members UNION select name from cd.facilities
-- 11. Simple aggregation
select max(joindate) from cd.members
-- 12. More aggregation
select firstname, surname, joindate from cd.members where joindate = (select max(joindate) from cd.members)
-- ===== Joins =====

-- ===== Aggregates =====
