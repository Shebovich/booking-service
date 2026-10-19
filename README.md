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
