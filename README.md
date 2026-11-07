# Skam Bet

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-6db33f)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791)
![Gradle](https://img.shields.io/badge/Gradle-8.14-02303a)
![Cucumber](https://img.shields.io/badge/Cucumber-7-23d96c)
![Selenium](https://img.shields.io/badge/Selenium-4-43b02a)
![Serenity BDD](https://img.shields.io/badge/Serenity%20BDD-4-3ba4d8)

Веб-приложение букмекерской конторы (учебный проект) и UI-автотесты к нему на Cucumber + Selenium WebDriver.
Ход выполнения тестов визуализируется отчётом Serenity BDD (живая документация из книги «BDD в действии»).

![Линия событий](.github/images/line.png)

## Возможности

| № | Функция | Что умеет |
|---|---|---|
| 1 | Регистрация | логин, email, дата рождения (только 18+), пароль, согласие с правилами |
| 2 | Авторизация | вход и выход, возврат на запрошенную страницу после входа |
| 3 | Линия событий | предстоящие матчи с коэффициентами П1 / X / П2, фильтр по виду спорта |
| 4 | Пополнение баланса | оплата картой, проверка номера по алгоритму Луна, история пополнений |
| 5 | Ставки | купон с расчётом выигрыша на лету, списание с баланса, раздел «Мои ставки» |
| 6 | Расчёт ставок | администратор вносит результат события, выигрыши зачисляются на баланс |
| 7 | Вывод средств | вывод на карту, проверка номера по алгоритму Луна, история выводов |
| 8 | Экспресс | купон из 2–10 событий, коэффициенты перемножаются |
| 9 | Личный кабинет | данные пользователя, смена email и пароля |
| 10 | Отмена ставки | отмена до начала события с возвратом денег на баланс |
| 11 | Поиск событий | поиск по части названия команды в линии, вместе с фильтром по виду спорта |

Документация:

- [docs/features.md](docs/features.md) — UI-фичи и их Gherkin-сценарии
- [docs/scenarios.md](docs/scenarios.md) — сценарии выполнения прецедентов

| Купон | Мои ставки | Пополнение |
|---|---|---|
| ![Купон](.github/images/coupon.png) | ![Мои ставки](.github/images/my-bets.png) | ![Пополнение](.github/images/deposit.png) |

## Стек

- Java 17, Gradle
- Spring Boot 3: Web, Security, Data JPA, Thymeleaf
- PostgreSQL 16, миграции Flyway
- Docker, Docker Compose
- Cucumber 7, Selenium WebDriver 4, JUnit 5, AssertJ, Testcontainers
- Serenity BDD 4 — отчёт о выполнении тестов со скриншотами каждого шага

## Запуск

### Всё в Docker

```bash
docker compose up --build
```

Приложение: http://localhost:8080, PostgreSQL доступна с хоста на порту `5433`.

### Локально

```bash
docker compose up -d db
./gradlew bootRun
```

Подключение к БД можно поменять переменными `DB_URL`, `DB_USER`, `DB_PASSWORD`.

При старте создаются демо-события и пользователь **demo / demo12345** с балансом 5000 ₽
(отключается переменной `DEMO_DATA=false`).
Администратор для раздела «Расчёт» — **admin / admin12345**, среди демо-событий есть уже начавшийся матч для расчёта.

## Автотесты

Feature-файлы лежат в `src/test/resources/features`, шаги и page objects — в `src/test/java/ru/skambet/bdd`.

- Тесты сами поднимают приложение на случайном порту и отдельный PostgreSQL в Docker через Testcontainers.
  Docker должен быть запущен, база из `docker-compose.yml` не используется и не меняется.
- Перед каждым сценарием база очищается, нужные данные создаются шагами `Дано`.
- Драйвер браузера скачивается автоматически (Selenium Manager), нужен установленный Chrome или Edge.
- Кроме UI-сценариев есть `BalanceConcurrencyTest`: он отправляет по два запроса одновременно (отмена ставки,
  расчёт события, пополнение) и проверяет, что баланс не теряется и событие не рассчитывается дважды.

```bash
./gradlew test
```

| Параметр | Описание |
|---|---|
| `-Dheadless=true` | без окна браузера |
| `-Dbrowser=edge` | Edge вместо Chrome |
| `-Dstep.delay=1500` | пауза после каждого шага в мс, чтобы было видно, что делает тест |
| `-Dcucumber.filter.tags=@bet` | одна фича: `@registration`, `@login`, `@events`, `@deposit`, `@bet`, `@settlement`, `@withdrawal`, `@express`, `@profile`, `@cancel`, `@search` |

Например, посмотреть оформление ставки в замедленном режиме:

```bash
./gradlew test -Dcucumber.filter.tags=@bet -Dstep.delay=1500
```

В PowerShell (терминал IntelliJ IDEA на Windows) параметры `-D...` нужно брать в кавычки:
`./gradlew test "-Dcucumber.filter.tags=@bet"`. Если русский текст в консоли выглядит как `╨║╨░`,
перед запуском выполните `chcp 65001`.

### Отчёт Serenity BDD

После каждого `./gradlew test` (даже если что-то упало) Gradle собирает отчёт Serenity:

```bash
start build/serenity/index.html
```

- **Overall Test Results** — сколько сценариев прошло, упало, сколько длились;
- **Requirements / Features** — фичи из `src/test/resources/features` как живая документация: какие сценарии
  есть у каждой функции и в каком они состоянии;
- в каждом сценарии — все шаги Gherkin со статусом, временем и **скриншотом браузера после каждого шага**.

Отчёт можно пересобрать без запуска тестов: `./gradlew aggregate`.
Браузером управляет Serenity (открывает на сценарий и закрывает после него), а создаётся он нашим
`DriverFactory` через `SkamBetDriverSource` — поэтому параметры `-Dheadless` и `-Dbrowser` работают как раньше.

Ещё есть отчёт Cucumber `build/cucumber-reports/cucumber.html` (при падении к сценарию прикладывается скриншот)
и отчёт Gradle `build/reports/tests/test/index.html`.

## Структура

```
src/main/java/ru/skambet
    config/        безопасность, демо-данные
    domain/        сущности: User, Event, Bet, Deposit, Withdrawal, ExpressBet
    repository/    Spring Data репозитории
    service/       бизнес-логика
    web/           контроллеры и формы
src/main/resources
    db/migration/  миграции Flyway
    templates/     страницы Thymeleaf
src/test/java/ru/skambet/bdd
    pages/         page objects
    steps/         step definitions
    support/       браузер (через Serenity), хуки, тестовые данные, Testcontainers
src/test/resources/features   feature-файлы
docs/                         фичи и сценарии прецедентов
```
