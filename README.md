# Skam Bet

Веб-приложение букмекерской конторы (учебный проект).

## Стек

- Java 17, Gradle
- Spring Boot 3: Web, Security, Data JPA, Thymeleaf
- PostgreSQL 16, миграции Flyway

## Запуск

Всё в Docker:

```bash
docker compose up --build
```

Локально (в Docker только база):

```bash
docker compose up -d db
./gradlew bootRun
```

Приложение откроется на http://localhost:8080, PostgreSQL из docker-compose доступна на порту `5433`.

При старте создаются демо-события и пользователь **demo / demo12345** с балансом 5000 ₽.
