plugins {
	kotlin("jvm") version "2.3.21"
	kotlin("plugin.spring") version "2.3.21"
	// TODO День 5, шаг 2: добавь плагин kotlin("plugin.jpa") той же версии, что и остальные kotlin-плагины.
	//   Он генерирует пустой конструктор для классов с @Entity: Hibernate создаёт объект пустым, а поля заполняет потом.
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "dev.pavel"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

// В проекте два файла с fun main(): само приложение и учебный lab/HandMadeProxy.kt.
// Явно говорим плагину Spring Boot, какой из них запускать в bootRun и класть в jar.
springBoot {
	mainClass = "dev.pavel.booking.BookingServiceApplicationKt"
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.1")

	// TODO День 5, шаг 1: добавь четыре зависимости (версии не пиши — их задаёт Spring Boot):
	implementation  ("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation  ("org.springframework.boot:spring-boot-starter-flyway")
	//   3) поддержка Postgres во Flyway (с Flyway 10 базы вынесены в отдельные модули):
	implementation  ("org.flywaydb:flyway-database-postgresql")
	runtimeOnly     ("org.postgresql:postgresql")
	//      Почему runtimeOnly: наш код не вызывает классы драйвера напрямую, он нужен только при запуске.
	//      Аналог в Android — зависимость, которая есть в APK, но не видна при компиляции.
	// Проверено 2026-10-26 на Maven Central: Boot 4.1.1 задаёт Hibernate 7.4.5, Flyway 12.4.0, драйвер 42.7.13.
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("tools.jackson.module:jackson-module-kotlin")
	testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

// TODO День 5, шаг 2: блок allOpen { annotation("...") } — открыть (снять final) классы с аннотациями
//   jakarta.persistence.Entity, jakarta.persistence.MappedSuperclass, jakarta.persistence.Embeddable.
//   Зачем: для LAZY-связей Hibernate создаёт НАСЛЕДНИКА сущности (прокси), а от final-класса наследоваться нельзя.
//   plugin.spring открывает только Spring-аннотации (@Service, @Component...), про @Entity он не знает.

tasks.withType<Test> {
	useJUnitPlatform()
}
