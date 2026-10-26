# План обучения: Android → Backend за 30 дней

Выгрузка с доски-артефакта (https://claude.ai/artifact/AdexrCZWNdHTP4vjjRhS3D), старт плана — 2026-10-20.
Доска остаётся главным источником: этот файл — снимок для работы с другой машины и для истории в git.
Где остановились и как продолжить — в [progress.md](progress.md).

## Спринт 1

### День 1. Окружение и первый сервис на Kotlin + Spring Boot — готово

*Теория, Практика*. Поднять рабочее окружение и понять, чем Spring Boot отличается от Android-приложения: жизненный цикл, сервер, конфиги.

Задачи:
- [x] Поставить JDK 21 (LTS), IntelliJ IDEA, Docker Desktop
- [x] Сгенерировать проект booking-service: Kotlin, Gradle Kotlin DSL, Web, Actuator
- [x] Сделать GET /health и GET /api/hello с параметром
- [x] Завести GitHub-репозиторий, README с целью проекта
- [x] Разобрать application.yml и профили dev/test

Вопросы к собеседованию:
- Что делает @SpringBootApplication?
- Как Spring Boot запускает встроенный Tomcat?
- Зачем плагин kotlin-spring (all-open)?

Теория:
- [Spring Boot + Kotlin: официальный туториал](https://spring.io/guides/tutorials/spring-boot-kotlin)
- [Kotlin docs: Spring Boot](https://kotlinlang.org/docs/jvm-get-started-spring-boot.html)
- [Spring Initializr](https://start.spring.io/)

Заметки:

> Мини-собес 19.10, средняя ~7.2/10.
> Оценки: прокси/kotlin-spring 7 · синглтоны и потоки 7 · component scan 8.5 · приоритет конфигов 8 · старт Tomcat 5 · @RequestParam 9 · встроенный сервер 6.
>
> Подтянуть к ревью (день 7):
> - Отвечать механизмом, а не только выводом: «почему» и «что будет иначе».
> - Автоконфигурация: AutoConfiguration.imports + @ConditionalOnClass / @ConditionalOnMissingBean → TomcatServletWebServerFactory → ServletWebServerApplicationContext.
> - Главный тезис: бины stateless; атомики/synchronized — запасной вариант (synchronized сериализует запросы).
> - application.yml — базовый конфиг, не «продовый»; prod = отдельный профиль.
> - Встроенный сервер: один артефакт, изоляция, Docker/K8s, масштабирование репликами.
>
> Практика дня: гонка на счётчике — потеряно 54/11/10 из 10000; после AtomicLong — 0.

### День 2. Spring Core: IoC, DI, бины, прокси — готово

*Теория, Собес*. Разобраться в контейнере так, чтобы спокойно отвечать на «расскажите жизненный цикл бина». Это частый вопрос на собеседованиях.

Задачи:
- [x] Сравнить DI в Spring и Dagger/Hilt: таблица в README
- [x] Написать BeanPostProcessor, который логирует время вызова методов
- [x] Проверить на практике singleton vs prototype scope
- [x] Воспроизвести проблему self-invocation с @Transactional/@Async
- [x] Сделать @ConfigurationProperties для настроек сервиса

Вопросы к собеседованию:
- Жизненный цикл бина
- JDK dynamic proxy vs CGLIB
- Почему вызов this.method() не проходит через прокси?
- Как работает autoconfiguration и @Conditional?

Теория:
- [Spring Framework Reference: IoC container](https://docs.spring.io/spring-framework/reference/core/beans.html)
- [Spring AOP и прокси](https://docs.spring.io/spring-framework/reference/core/aop.html)
- [Baeldung: Spring Bean Scopes](https://www.baeldung.com/spring-bean-scopes)

Заметки:

> Мини-собес 20.10, средняя ~7.3/10 (день 1 был 7.2).
> Оценки: жизненный цикл 8 · JDK proxy vs CGLIB 7 · prototype в singleton 6.5 · BFPP vs BPP 8 · @ConfigurationProperties vs @Value 8 · автоконфигурация 6.5.
>
> Подтянуть к ревью (день 7):
> - Короткие ответы без механизма (Q3, Q6): сначала «что происходит внутри», потом «как чинить».
> - CGLIB: называть final (Kotlin + kotlin-spring); self-invocation — общая проблема прокси, не CGLIB.
> - Валидация конфига — при старте, не при компиляции; Duration, не DateTime; нужен @Validated.
> - Автоконфигурация: цепочка @EnableAutoConfiguration → AutoConfiguration.imports → @ConditionalOnClass (classpath) → @ConditionalOnMissingBean (бины) → фабрика; автоконфиги идут после пользовательских; отладка — --debug / /actuator/conditions.
> - Первый вызов медленный = прогрев JVM (загрузка классов, JIT), а не ленивый прокси.
>
> Практика: lab/ (жц, scope, @Async self-invocation + фикс @Lazy self, прокси руками, @LogTime BPP), BookingProperties, таблица DI в README. Коммит eed8dc2.

### День 3. REST API на Spring MVC — готово

*Практика*. Собрать CRUD-эндпоинты бронирований с валидацией, обработкой ошибок и документацией.

Задачи:
- [x] CRUD /api/bookings (пока in-memory)
- [x] DTO + Bean Validation (@Valid, @NotBlank, кастомный валидатор)
- [x] @RestControllerAdvice + ProblemDetail для ошибок
- [x] Пагинация и сортировка через query-параметры
- [x] Swagger UI через springdoc

Вопросы к собеседованию:
- Идемпотентность HTTP-методов
- PUT vs PATCH
- Какие статусы вернуть при конфликте и при ошибке валидации?
- Как устроен DispatcherServlet?

Теория:
- [Spring Web MVC Reference](https://docs.spring.io/spring-framework/reference/web/webmvc.html)
- [Error Responses / ProblemDetail](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html)
- [springdoc-openapi](https://springdoc.org/)

Заметки:

> Коммит 64e09fa, запушен.
> Шаг 4: page/size/sortBy(enum)/direction. Баг @Min(0) на size -> деление на 0 -> 500, исправлено на @Min(1). Бонус: handleHandlerMethodValidationException с errors по параметрам. Урок: формат timestamp/error/path = упал сам обработчик.
> Шаг 5: springdoc 3.1.1 на Boot 4.1.1. @Tag/@Operation/@ApiResponse, paths-to-match, @OpenAPIDefinition. Урок: начал перечислять @ApiResponse — перечисли все. DELETE ≠ «отменить».
> Мини-собес: идемпотентность 5.5, PUT/PATCH 7, статусы 5.5, DispatcherServlet 5.5, Filter/Interceptor/AOP 4.5; среднее 5.6.
> Повторить: определение идемпотентности (N запросов = 1, не «не меняет»), PATCH «не обязан» быть идемпотентным, Idempotency-Key, НЕ GET для изменений; 409 Conflict (тест: успех при другом состоянии сервера), 403 vs 404/IDOR, 422 vs 400; цепочка Filter -> HandlerMapping -> ArgumentResolver (+@Valid) -> метод -> MessageConverter -> HandlerExceptionResolver -> /error; отвечать на все подвопросы.

### День 4. PostgreSQL: SQL, индексы, транзакции — готово

*Теория, Практика*. SQL и Postgres проверяют на любом бэкенд-собеседовании. Цель: писать JOIN и GROUP BY без раздумий, читать EXPLAIN и знать уровни изоляции.

Задачи:
- [x] Поднять Postgres в Docker, подключиться из IDE
- [x] pgexercises.com: разделы Basic, Joins, Aggregates
- [ ] LeetCode SQL 50: первые 15 задач (отложено под live coding)
- [x] На таблице с 1 млн строк сравнить EXPLAIN ANALYZE до и после индекса
- [x] Воспроизвести non-repeatable read на Read Committed в двух сессиях

Вопросы к собеседованию:
- Как устроен B-tree индекс и когда он не используется?
- Уровни изоляции и аномалии
- Что такое MVCC и VACUUM?
- Составной индекс: важен ли порядок колонок?

Теория:
- [PostgreSQL Tutorial (официальный)](https://www.postgresql.org/docs/current/tutorial.html)
- [Use The Index, Luke](https://use-the-index-luke.com/)
- [Уровни изоляции транзакций](https://www.postgresql.org/docs/current/transaction-iso.html)
- [MVCC](https://www.postgresql.org/docs/current/mvcc-intro.html)

Заметки:

> Postgres 18.6 в Docker (compose.yaml). Лабы: sql/explain-lab.sql (индексы, EXPLAIN, skip scan PG18, text_pattern_ops, Index Only Scan + VACUUM), sql/transactions-lab.sql (RC/RR, could not serialize, lost update). Конспект: docs/theory/day-04-postgres.md. Коммиты fee422a, 0e43cf6, 40297eb.
> pgexercises Joins/Aggregates — решения на другой машине, не запушены.
>
> Мини-собес 5.4/10: JOIN ON/WHERE+count 7; функция над колонкой 5; составной индекс 4; селективность/частичный индекс 4; MVCC/VACUUM 7; изоляция/аномалии 8; lost update 3; «запрос тормозит» 5.
> Подтянуть к дню 7: order by по 2-й колонке составного индекса (сортировки нет), порядок колонок (= потом диапазон); unique (дубликаты) vs атомарный UPDATE/for update/@Version (lost update); частичный индекс; pg_stat_statements = статистика запросов, размеры = pg_relation_size; create index concurrently; оптимистичная блокировка для долгих форм; отвечать механизмом и на все пункты.

### День 5. Spring Data JPA, Hibernate, миграции — не начат

*Теория, Практика, Собес*. Перевести сервис на Postgres через JPA и разобрать типичные ловушки: N+1, lazy loading, границы транзакций.

Задачи:
- [ ] Сущности Booking, Room, User со связями
- [ ] Flyway-миграции вместо ddl-auto
- [ ] Воспроизвести N+1 и починить через JOIN FETCH или @EntityGraph
- [ ] Проверить propagation REQUIRED vs REQUIRES_NEW
- [ ] Optimistic locking (@Version) от двойного бронирования

Вопросы к собеседованию:
- Как решить N+1?
- Что такое persistence context и dirty checking?
- Почему LazyInitializationException?
- Propagation и isolation в @Transactional
- Optimistic vs pessimistic locking

Теория:
- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/reference/)
- [Vlad Mihalcea: Hibernate tutorials](https://vladmihalcea.com/tutorials/hibernate/)
- [Spring Boot: миграции БД (Flyway/Liquibase)](https://docs.spring.io/spring-boot/how-to/data-initialization.html)
- [Transaction management](https://docs.spring.io/spring-framework/reference/data-access/transaction.html)

### День 6. Тесты: JUnit 5, MockK, Testcontainers — не начат

*Практика*. Покрыть сервис тестами так, как принято в бэкенде: интеграционные тесты на настоящей базе в контейнере.

Задачи:
- [ ] Unit-тесты сервисного слоя на MockK
- [ ] @WebMvcTest для контроллера, включая ошибки валидации
- [ ] @DataJpaTest и Testcontainers Postgres
- [ ] Один сквозной @SpringBootTest сценарий бронирования
- [ ] Подключить Jacoco и посмотреть покрытие

Вопросы к собеседованию:
- Пирамида тестирования в бэкенде
- Чем @SpringBootTest отличается от тест-слайсов?
- Зачем Testcontainers, если есть H2?

Теория:
- [Testcontainers Guides](https://testcontainers.com/guides/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/reference/testing/index.html)
- [MockK](https://mockk.io/)

### День 7. Ревью спринта 1 и мини-собес — не начат

*Собес*. Закрепить пройденное и проверить себя вопросами по Spring, JPA и SQL.

Задачи:
- [ ] Ответить вслух на все вопросы карточек 1–6
- [ ] Попросить Claude провести 30-минутное собеседование по Spring/JPA
- [ ] Рефакторинг проекта, README с описанием API
- [ ] Записать слабые места в заметки

Вопросы к собеседованию:
- Расскажите, как обрабатывается HTTP-запрос от сокета до репозитория

Теория:
- [Spring Boot Reference (оглавление для повторения)](https://docs.spring.io/spring-boot/reference/)

## Спринт 2

### День 8. Docker и Docker Compose — не начат

*Теория, Практика*. Упаковать сервис в образ и поднимать всё окружение одной командой.

Задачи:
- [ ] Multi-stage Dockerfile для booking-service
- [ ] Сравнить с bootBuildImage (Buildpacks): размер и слои
- [ ] docker-compose.yml: сервис + Postgres + Adminer
- [ ] Healthcheck и depends_on с условием
- [ ] Передать конфиг через переменные окружения

Вопросы к собеседованию:
- Образ vs контейнер, слои и кеширование
- Почему контейнер не должен хранить состояние?
- CMD vs ENTRYPOINT

Теория:
- [Docker: Get started](https://docs.docker.com/get-started/)
- [Docker Compose](https://docs.docker.com/compose/)
- [Spring Boot: контейнерные образы](https://docs.spring.io/spring-boot/reference/packaging/container-images/index.html)

### День 9. Kafka: теория — не начат

*Теория, Собес*. Kafka упоминается в большинстве Java/Kotlin-вакансий. Разобраться в устройстве на уровне, достаточном для собеседования.

Задачи:
- [ ] Пройти курс Kafka 101 на Confluent Developer
- [ ] Поднять Kafka (KRaft) в Docker Compose
- [ ] Через CLI создать топик с 3 партициями, писать и читать в двух consumer group
- [ ] Нарисовать схему: брокер, топик, партиция, offset, consumer group

Вопросы к собеседованию:
- Как гарантируется порядок сообщений?
- At-most / at-least / exactly-once
- Что такое rebalance и чем он плох?
- Идемпотентный продюсер, acks=all, min.insync.replicas
- Kafka vs RabbitMQ

Теория:
- [Confluent Developer: курсы по Kafka](https://developer.confluent.io/courses/)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring for Apache Kafka](https://docs.spring.io/spring-kafka/reference/)

### День 10. Spring Kafka: второй сервис — не начат

*Практика*. Создать payment-service, который общается с booking-service через события.

Задачи:
- [ ] booking-service публикует BookingCreated
- [ ] payment-service слушает и публикует PaymentCompleted/Failed
- [ ] Retry и dead-letter topic для ошибок
- [ ] Ключ сообщения = bookingId (порядок по брони)
- [ ] Интеграционный тест с Testcontainers Kafka

Вопросы к собеседованию:
- Что будет, если consumer упал после обработки, но до коммита offset?
- Как обрабатывать «ядовитые» сообщения?

Теория:
- [Spring Kafka: receiving messages](https://docs.spring.io/spring-kafka/reference/kafka/receiving-messages.html)
- [Spring Kafka: error handling и DLT](https://docs.spring.io/spring-kafka/reference/retrytopic.html)

### День 11. Паттерны: outbox, saga, идемпотентность — не начат

*Теория, Практика, Собес*. Решить проблему dual write и научиться объяснять eventual consistency. Это частая тема на собеседованиях Middle+.

Задачи:
- [ ] Таблица outbox и шедулер-релей в Kafka
- [ ] Идемпотентный consumer: таблица processed_events
- [ ] Хореографическая сага: бронь → оплата → подтверждение или отмена
- [ ] Убить сервис посередине и убедиться, что данные согласованы

Вопросы к собеседованию:
- Что такое dual write и почему он опасен?
- Оркестрация vs хореография саги
- Как сделать операцию идемпотентной?

Теория:
- [Transactional Outbox](https://microservices.io/patterns/data/transactional-outbox.html)
- [Saga](https://microservices.io/patterns/data/saga.html)
- [Idempotent Consumer](https://microservices.io/patterns/communication-style/idempotent-consumer.html)

### День 12. Redis и кеширование — не начат

*Теория, Практика*. Добавить кеш и понять его проблемы: инвалидацию, stampede, TTL.

Задачи:
- [ ] Redis в compose, @Cacheable для каталога номеров
- [ ] Инвалидация через @CacheEvict при изменении
- [ ] Rate limiter на Redis (fixed window) для POST /bookings
- [ ] Замерить latency с кешем и без

Вопросы к собеседованию:
- Стратегии: cache-aside, write-through, write-behind
- Cache stampede и как его избежать
- Почему распределённый лок на Redis — это непросто?

Теория:
- [Spring Boot: Caching](https://docs.spring.io/spring-boot/reference/io/caching.html)
- [Redis docs](https://redis.io/docs/latest/)

### День 13. Spring Security, JWT, OAuth2 — не начат

*Теория, Практика*. Закрыть API авторизацией так, как делают в продакшене: resource server и внешний identity provider.

Задачи:
- [ ] Keycloak в compose, realm и клиент
- [ ] booking-service как OAuth2 resource server
- [ ] Роли USER/ADMIN и @PreAuthorize
- [ ] Тесты с @WithMockUser и jwt()

Вопросы к собеседованию:
- Как устроен SecurityFilterChain?
- Сессии vs JWT: плюсы и минусы
- Как отозвать JWT?
- Authentication vs authorization

Теория:
- [Spring Security: Architecture](https://docs.spring.io/spring-security/reference/servlet/architecture.html)
- [OAuth2 Resource Server JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [Keycloak: Getting started (Docker)](https://www.keycloak.org/getting-started/getting-started-docker)

### День 14. Межсервисные вызовы и отказоустойчивость — не начат

*Теория, Практика*. Синхронные вызовы между сервисами с таймаутами, ретраями и circuit breaker.

Задачи:
- [ ] booking-service вызывает pricing-заглушку через RestClient
- [ ] Таймауты, retry с backoff, circuit breaker
- [ ] Fallback-ответ при открытом breaker
- [ ] Ревью спринта 2: ответить вслух на вопросы карточек 8–13

Вопросы к собеседованию:
- Почему ретраи без идемпотентности опасны?
- Состояния circuit breaker
- REST vs gRPC vs события

Теория:
- [Spring: RestClient](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html)
- [Resilience4j](https://resilience4j.readme.io/docs)

## Спринт 3

### День 15. Kubernetes: основы и деплой — не начат

*Теория, Практика*. Задеплоить систему в локальный кластер. Разработчику k8s нужен на уровне «разворачиваю свой сервис и читаю манифесты».

Задачи:
- [ ] Поднять кластер kind
- [ ] Deployment + Service + ConfigMap + Secret для booking-service
- [ ] Liveness/readiness через Actuator
- [ ] requests/limits, посмотреть OOMKilled при маленьком limit
- [ ] Rolling update без даунтайма

Вопросы к собеседованию:
- Pod vs Deployment vs Service
- Liveness vs readiness
- Как JVM видит лимиты памяти контейнера?

Теория:
- [Kubernetes Basics (официальный туториал)](https://kubernetes.io/docs/tutorials/kubernetes-basics/)
- [kind: локальный кластер](https://kind.sigs.k8s.io/)
- [Probes](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/)

### День 16. Наблюдаемость: метрики, логи, трейсинг — не начат

*Теория, Практика*. Видеть, что происходит с сервисами: метрики в Grafana и сквозной trace через Kafka.

Задачи:
- [ ] Prometheus и Grafana в compose, дашборд RPS/latency/errors
- [ ] Кастомная бизнес-метрика: bookings_created_total
- [ ] Structured JSON-логи с traceId
- [ ] Трейсинг booking → kafka → payment в Zipkin или Jaeger

Вопросы к собеседованию:
- RED и USE метрики
- Зачем correlation/trace id?
- Counter vs Gauge vs Histogram

Теория:
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/reference/actuator/index.html)
- [Micrometer](https://docs.micrometer.io/micrometer/reference/)
- [Prometheus: Overview](https://prometheus.io/docs/introduction/overview/)
- [OpenTelemetry Java](https://opentelemetry.io/docs/languages/java/)

### День 17. CI/CD на GitHub Actions — не начат

*Практика*. Каждый push собирает, тестирует и публикует образ. Это хорошо смотрится в портфолио.

Задачи:
- [ ] Workflow: build + test (Testcontainers работают на ubuntu-runner)
- [ ] Сборка Docker-образа и push в GHCR
- [ ] Бейдж статуса в README
- [ ] Ktlint или detekt как обязательная проверка

Вопросы к собеседованию:
- Что такое blue-green и canary деплой?
- Как откатить неудачный релиз?

Теория:
- [GitHub Actions docs](https://docs.github.com/en/actions)
- [Gradle на GitHub Actions](https://docs.github.com/en/actions/use-cases-and-examples/building-and-testing/building-and-testing-java-with-gradle)

### День 18. JVM и многопоточность — не начат

*Теория, Собес*. Закрыть то, что на Android было не так критично: JMM, пулы потоков, GC и virtual threads.

Задачи:
- [ ] Включить virtual threads в Spring Boot и замерить под нагрузкой
- [ ] Написать и починить race condition (AtomicInteger, synchronized, ConcurrentHashMap)
- [ ] Снять heap dump и thread dump у работающего сервиса
- [ ] Сравнить G1 и ZGC на нагрузке

Вопросы к собеседованию:
- happens-before, volatile, synchronized
- Как устроен ConcurrentHashMap?
- ThreadPoolExecutor: параметры и очередь
- Virtual threads: когда помогают, что такое pinning?
- Поколения в GC, stop-the-world

Теория:
- [JEP 444: Virtual Threads](https://openjdk.org/jeps/444)
- [JLS: Java Memory Model (глава 17)](https://docs.oracle.com/javase/specs/jls/se21/html/jls-17.html)
- [Garbage Collection Tuning Guide (Java 21)](https://docs.oracle.com/en/java/javase/21/gctuning/)

### День 19. Корутины на сервере и реактивный стек — не начат

*Теория, Практика*. Использовать свой опыт с корутинами в бэкенде и понять, когда WebFlux оправдан.

Задачи:
- [ ] notification-service на WebFlux и suspend-контроллерах
- [ ] Параллельные вызовы через coroutineScope и async
- [ ] Сравнить с MVC + virtual threads: код и метрики
- [ ] Минимальный эндпоинт на Ktor для сравнения

Вопросы к собеседованию:
- Reactive vs virtual threads vs coroutines
- Почему нельзя блокировать event loop?
- Как корутины интегрируются с Reactor?

Теория:
- [Spring: Kotlin Coroutines](https://docs.spring.io/spring-framework/reference/languages/kotlin/coroutines.html)
- [Spring WebFlux](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Ktor (альтернатива Spring)](https://ktor.io/docs/)

### День 20. Продвинутый PostgreSQL — не начат

*Теория, Практика*. Вопросы для Middle+: блокировки, типы индексов, пагинация, пул соединений.

Задачи:
- [ ] Partial и GIN индексы на реальных запросах проекта
- [ ] Keyset-пагинация вместо OFFSET и замер разницы
- [ ] Воспроизвести deadlock и прочитать лог
- [ ] SELECT … FOR UPDATE SKIP LOCKED для outbox-релея
- [ ] LeetCode SQL 50: добить до 35 задач

Вопросы к собеседованию:
- Почему OFFSET медленный?
- Как найти медленные запросы (pg_stat_statements)?
- Репликация и read replicas
- Партиционирование: когда нужно

Теория:
- [Index Types](https://www.postgresql.org/docs/current/indexes-types.html)
- [Explicit Locking](https://www.postgresql.org/docs/current/explicit-locking.html)
- [HikariCP: About Pool Sizing](https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing)

### День 21. Нагрузочное тестирование и ревью спринта 3 — не начат

*Практика, Собес*. Найти узкое место системы под нагрузкой и уметь рассказать, как его нашёл.

Задачи:
- [ ] Скрипт k6 на сценарий бронирования
- [ ] Найти bottleneck по метрикам и трейсам
- [ ] Одна оптимизация с замером до и после, описать в README
- [ ] Ответить вслух на вопросы карточек 15–20

Вопросы к собеседованию:
- Расскажите о проблеме производительности, которую вы нашли и решили

Теория:
- [Grafana k6 docs](https://grafana.com/docs/k6/latest/)

## Спринт 4

### День 22. System design: фундамент — не начат

*Теория*. Выучить базовые блоки и выработать порядок ответа на system design интервью.

Задачи:
- [ ] Конспект: масштабирование, балансировка, репликация, шардирование, CAP
- [ ] DDIA: главы 5–7 (репликация, партиционирование, транзакции)
- [ ] Шаблон ответа: требования → оценки → API → данные → схема → узкие места
- [ ] Упражнение на back-of-the-envelope оценки

Вопросы к собеседованию:
- CAP и PACELC
- Шардирование: выбор ключа и решардинг
- Strong vs eventual consistency

Теория:
- [System Design Primer](https://github.com/donnemartin/system-design-primer)
- [Hello Interview: System Design in a Hurry](https://www.hellointerview.com/learn/system-design/in-a-hurry/introduction)
- [Designing Data-Intensive Applications (книга)](https://dataintensive.net/)

### День 23. System design: практика 1 — не начат

*Практика, Собес*. Решить классические задачи по шаблону, рисуя схему за 45 минут.

Задачи:
- [ ] Design: URL shortener
- [ ] Design: rate limiter
- [ ] Design: notification system (push/email, ретраи)
- [ ] Каждый раз засекать 45 минут и рисовать в Excalidraw

Вопросы к собеседованию:
- Как гарантировать уникальность коротких ссылок?
- Token bucket vs sliding window

Теория:
- [System Design Primer: solutions](https://github.com/donnemartin/system-design-primer#system-design-interview-questions-with-solutions)

### День 24. System design: практика 2 (свой домен) — не начат

*Практика, Собес*. Задачи, где можно использовать опыт мобильной разработки: чаты, ленты, синхронизация.

Задачи:
- [ ] Design: мессенджер (WebSocket, доставка, офлайн-синк)
- [ ] Design: лента новостей (fan-out on write/read)
- [ ] Design: бронирование отелей (защита от двойной брони, саги)
- [ ] Mock system design с Claude: попросить роль интервьюера

Вопросы к собеседованию:
- Как сделать бэкенд под офлайн-первый мобильный клиент?
- Push vs pull для ленты

Теория:
- [Hello Interview: разборы задач](https://www.hellointerview.com/learn/system-design/problem-breakdowns/overview)

### День 25. Java/Kotlin Core для собеседования — не начат

*Теория, Собес*. Повторить вопросы по ядру, которые задают на бэкенд-собеседованиях независимо от опыта.

Задачи:
- [ ] HashMap изнутри: бакеты, treeify, resize — пересказать вслух
- [ ] equals/hashCode, immutability, String pool
- [ ] Generics: variance (in/out vs ? extends/super), type erasure
- [ ] Streams vs Sequences, checked exceptions и Kotlin

Вопросы к собеседованию:
- Как устроен HashMap?
- Контракт equals/hashCode
- ArrayList vs LinkedList
- Как Kotlin компилирует data class, object, companion?

Теория:
- [Kotlin docs](https://kotlinlang.org/docs/home.html)
- [Baeldung: Java interview questions](https://www.baeldung.com/java-interview-questions)

### День 26. Spring для собеседования: шпаргалка — не начат

*Собес*. Собрать свою шпаргалку по Spring и прогнать её до автоматизма.

Задачи:
- [ ] Шпаргалка на одну страницу: бины, прокси, транзакции, autoconfig, security
- [ ] Ловушки @Transactional: private-методы, self-invocation, checked exceptions
- [ ] Написать свой spring-boot-starter
- [ ] Mock-собес по Spring с Claude, 40 минут

Вопросы к собеседованию:
- Почему @Transactional не откатывает checked exception?
- Как написать свой starter?
- Что нового в Spring Boot 4 (Jackson 3, Kotlin 2.2)?

Теория:
- [Spring Boot: Auto-configuration](https://docs.spring.io/spring-boot/reference/using/auto-configuration.html)

### День 27. Live coding: алгоритмы и SQL — не начат

*Практика, Собес*. Восстановить скорость на типовых задачах live coding.

Задачи:
- [ ] Arrays & Hashing, Two Pointers: по 5 задач
- [ ] Sliding Window, Stack: по 3 задачи
- [ ] Добить SQL 50 до конца
- [ ] Решать вслух, проговаривая сложность

Вопросы к собеседованию:
- Big-O своего решения и как его улучшить

Теория:
- [NeetCode Roadmap](https://neetcode.io/roadmap)
- [LeetCode SQL 50](https://leetcode.com/studyplan/top-sql-50/)

### День 28. Резюме и упаковка опыта — не начат

*Практика*. Переписать резюме под бэкенд: senior-опыт из Android плюс бэкенд-проект с измеримыми результатами.

Задачи:
- [ ] README проекта: архитектура, схема, как запустить, что измерено
- [ ] Резюме: позиционирование «Kotlin/Java backend» и бэкенд-задачи из прошлых Android-проектов
- [ ] 5 историй в формате STAR (конфликт, провал, лидерство, сложный баг, архитектурное решение)
- [ ] Обновить профили на hh.ru, dev.by, LinkedIn

Вопросы к собеседованию:
- Почему вы уходите из Android?
- Расскажите про свой бэкенд-проект за 3 минуты

Теория:
- [jobs.dev.by](https://jobs.devby.io/)
- [Хабр Карьера](https://career.habr.com/)

### День 29. Mock-собеседования — не начат

*Собес*. Пройти полный цикл собеседования в безопасной обстановке и закрыть пробелы.

Задачи:
- [ ] Mock: техническое интервью (Core + Spring + БД), 60 минут
- [ ] Mock: system design, 45 минут
- [ ] Mock: поведенческое интервью
- [ ] Список пробелов → короткие повторения

### День 30. Отклики и первые собеседования — не начат

*Практика*. Выйти на рынок. Первые собеседования тоже тренировка, разбирай каждое.

Задачи:
- [ ] 15+ откликов (Java/Kotlin backend, Middle+/Senior)
- [ ] Сопроводительное письмо с фокусом на Kotlin и проект
- [ ] После каждого собеседования записывать вопросы в заметки
- [ ] План поддержки: 1 час в день на пробелы

Теория:
- [jobs.dev.by: Java](https://jobs.devby.io/)
- [hh.ru](https://hh.ru/)

