# Прогресс и как продолжить

Обновлено: 2026-10-22. Полный план по дням — в [learning-plan.md](learning-plan.md).
Доска с живым прогрессом: https://claude.ai/artifact/AdexrCZWNdHTP4vjjRhS3D (открывается с любого устройства под тем же аккаунтом claude.ai).

## Где остановились

**День 4 «PostgreSQL: SQL, индексы, транзакции»**, практика.

- [x] Шаг 1. Postgres 18 в Docker (`compose.yaml`), подключение из IntelliJ — готово.
- [ ] Шаг 2. pgexercises.com: Basic — **пройден** (12 задач, решения в `sql/pgexercises.sql`); **дальше Joins**, потом Aggregates.
- [ ] Шаг 3. LeetCode SQL 50, первые 15 задач. Не проверено, открывается ли LeetCode в браузере (на запрос из curl ответил 403 — это может быть защита от ботов, а не блокировка).
- [ ] Шаг 4. Таблица на 1 млн строк, `EXPLAIN ANALYZE` до и после индекса.
- [ ] Шаг 5. Воспроизвести non-repeatable read на Read Committed в двух сессиях.
- [ ] Мини-собеседование по дню 4, коммит, отметка на доске.

Хвосты по разделу Basic (из ревью):
- дописать `;` в задачах 7, 10, 11, 12;
- задача 6: `in (1, 5)` вместо строк `('1', '5')`.

Уже объяснено в теории дня 4: порядок выполнения SELECT (FROM → WHERE → GROUP BY → HAVING → SELECT → ORDER BY → LIMIT), JOIN (INNER / LEFT / self-join), GROUP BY и агрегаты, NULL, скалярный и коррелированный подзапрос, UNION против UNION ALL, целочисленное деление, `LIMIT 1` при одинаковых значениях.

## Итоги прошедших дней (кратко)

- День 1 — готово, мини-собес ~7.2/10.
- День 2 — готово, мини-собес ~7.3/10.
- День 3 — готово, мини-собес 5.6/10 (коммит `64e09fa`).
- Подробные оценки и что подтянуть к ревью на день 7 — в заметках дней в [learning-plan.md](learning-plan.md).

Общие слабые места: отвечать механизмом, а не выводом; отвечать на все подвопросы; 409 Conflict и состояние сервера; цепочка обработки запроса в Spring MVC.

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
