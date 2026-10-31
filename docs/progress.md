# Прогресс и как продолжить

Обновлено: 2026-10-31. Полный план по дням — в [learning-plan.md](learning-plan.md).
Доска с живым прогрессом: https://claude.ai/artifact/AdexrCZWNdHTP4vjjRhS3D (открывается с любого устройства под тем же аккаунтом claude.ai).

## Где остановились

**День 5 «Spring Data JPA, Hibernate, миграции» — в процессе.** Конспект: [theory/day-05-jpa.md](theory/day-05-jpa.md).

Сделано в дне 5:
- шаг 1 — Postgres + Flyway: зависимости, `datasource`/`jpa` в `application.yml` (`open-in-view: false`, `ddl-auto: validate`),
  миграция `V1__create_bookings.sql` применена (лабораторные таблицы дня 4 из базы удалены; V1 больше не менять — checksum);
- шаг 2 — `Booking` стал `@Entity`, `plugin.jpa` + `allOpen`, лог SQL, `BookingRepository : JpaRepository<Booking, UUID>`,
  сервис с `@Transactional`, пагинация и сортировка в базе (`PageRequest` + `Sort` + `.and(Sort.by("id"))`). CRUD проверен (коммит `51cd268`).
- **на другой машине** в базе ещё лежат таблицы лабораторных дня 4 — Flyway упадёт с «Found non-empty schema(s)». Перед первым
  запуском: `docker exec booking-postgres psql -U booking -d booking -c "drop table if exists lab_bookings, accounts;"`.
  Файлы из `db/migration` руками не запускать — их выполняет только Flyway.

**Следующее — шаг 2.6, три загадки в логе SQL** (запустить приложение, повторить вызовы в Swagger, ответить своими словами):
1. POST: почему перед `insert` идёт `select ... where id=?` и два способа починить (подсказка: правило isNew);
2. DELETE даёт три запроса (`count`, `select`, `delete`) — откуда каждый;
3. в списке нет `select count(*)` — почему (создать броней больше, чем `size`, и посмотреть снова).

Дальше по плану дня 5: починить SELECT перед INSERT (nullable `@Version` или `Persistable`) → связи Room/User и N+1
(JOIN FETCH / `@EntityGraph`) → propagation REQUIRED vs REQUIRES_NEW → `@Version` и двойная бронь → мини-собес → README/progress/доска.

Итоги дня 4:
- Postgres 18 в Docker (`compose.yaml`). Если после сбоя Docker база не отвечает — `docker compose up -d`
  (можно добавить сервису `restart: unless-stopped`).
- `sql/explain-lab.sql` — индексы и EXPLAIN на 1 млн строк, `sql/transactions-lab.sql` — изоляция и lost update.
- Конспект для повторения: [theory/day-04-postgres.md](theory/day-04-postgres.md).

Хвосты дня 4:
- решения pgexercises Joins и Aggregates лежат на другой машине — закоммитить и запушить в `sql/pgexercises.sql`;
- Basic (из ревью): дописать `;` в задачах 7, 10, 11, 12; задача 6 — `in (1, 5)` вместо строк `('1', '5')`;
- LeetCode SQL 50 отложен под тренировку live coding (из curl отвечал 403 — в браузере не проверено).

## Итоги прошедших дней (кратко)

- День 1 — готово, мини-собес ~7.2/10.
- День 2 — готово, мини-собес ~7.3/10.
- День 3 — готово, мини-собес 5.6/10 (коммит `64e09fa`).
- День 4 — готово, мини-собес 5.4/10.
- Подробные оценки и что подтянуть к ревью на день 7 — в заметках дней в [learning-plan.md](learning-plan.md).

Общие слабые места: отвечать механизмом, а не выводом; отвечать на все подвопросы; 409 Conflict и состояние сервера; цепочка обработки запроса в Spring MVC;
составной индекс и `order by`; не путать `unique` (дубликаты) и защиту от lost update; частичный индекс; `create index concurrently`.

## Как продолжить на другой машине

1. Поставить:
   - JDK 21 (Temurin);
   - IntelliJ IDEA (бесплатная единая версия: HTTP Client и Database-клиент в ней есть, Ultimate не покупаем);
   - Docker Desktop (бесплатен для личного использования);
   - Git.
2. Клонировать проект: `git clone https://github.com/Shebovich/booking-service.git` и открыть папку в IntelliJ.
3. Поднять базу: `docker compose up -d`, дождаться `healthy` в `docker compose ps`.
4. Загрузить базу pgexercises — команды в шапке `sql/pgexercises.sql`. Добавить в IntelliJ два Data Source: базы `booking` и `exercises` (localhost:5432, booking/booking).
5. Запустить приложение: `./gradlew bootRun` (или зелёный треугольник у `main` в `BookingServiceApplication.kt`). Проверка — `http/bookings.http` и Swagger: http://localhost:8080/swagger-ui.html.
6. Для Claude Code: открыть сессию в папке проекта — правила работы подхватятся из `CLAUDE.md` в корне. Сказать: «продолжаем обучение, смотри docs/progress.md».

Что НЕ переедет через git (лежит только на старой машине, в `%USERPROFILE%\.claude\`):
- личный глобальный `CLAUDE.md` (правила про проверку фактов и траты) — скопировать вручную, если нужен;
- автопамять Claude Code. Всё нужное для обучения перенесено в этот файл и в `CLAUDE.md` проекта.
