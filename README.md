# booking-service

Учебный backend-сервис бронирования: мой переход из Android в backend за 30 дней.

## Цель проекта

Шаг за шагом собрать сервис уровня production, который покрывает стек из вакансий Kotlin/Java backend:

- REST API на Spring Boot (Web MVC, Validation, Actuator)
- PostgreSQL + Spring Data JPA, миграции Flyway/Liquibase
- Spring Security (JWT)
- Kafka для событий бронирования
- Тесты: JUnit 5, MockK, Testcontainers
- Docker, docker-compose, CI на GitHub Actions
- Метрики Prometheus/Grafana

## Стек

- Kotlin 2.3, Java 21
- Spring Boot 4.1
- Gradle (Kotlin DSL)

## Запуск

```bash
./gradlew bootRun
```

С профилем dev:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Проверка:

- http://localhost:8080/actuator/health
- http://localhost:8080/api/hello?name=Pavel

## Прогресс

- [x] День 1: окружение, первый эндпоинт, конфиги и профили
- [x] День 2: Spring Core — жизненный цикл бинов, scope, прокси, self-invocation, @ConfigurationProperties

## DI: Spring vs Dagger/Hilt

| Вопрос                                                         | Dagger / Hilt (Android)                                                                        | Spring                                                        |
|----------------------------------------------------------------|------------------------------------------------------------------------------------------------|---------------------------------------------------------------|
| Когда строится граф зависимостей                               | На этапе компиляции                                                                            | при запуске                                                   |
| Когда видна ошибка «нет зависимости»                           | На этапе компиляции                                                                            | При запуске                                                   |
| Как объявить свой класс-зависимость                            | @Inject constructor                                                                            | @Service, @Repository, @RestController                        |
| Как предоставить чужой класс (из библиотеки)                   | @Provides внутри @Module + @InstallIn                                                          | @Bean внутри класса с @Configuration                          |
| Синглтон и другие scope                                        | без аннотации каждый раз создаётся новый объект; @Singleton, @ActivityScoped, @ViewModelScoped | без аннотации синглтон; @Scope("prototype"), request, session |
| Получить новый экземпляр на каждый вызов / ленивая зависимость | Provider<T>                                                                                    | ObjectProvider<T>                                             |
| Прокси и AOP (перехват вызовов методов)                        | В андроиде нет                                                                                 | есть                                                          |
| Подмена зависимостей в тестах                                  | @HiltAndroidTest                                                                               | @MockitoBean                                                  |
