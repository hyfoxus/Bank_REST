# Bank REST API

REST-сервис для управления банковскими картами и пользователями.  
Реализованы роли **ADMIN** и **USER**, CRUD-операции, переводы между своими картами, заявки на блокировку, фильтрация и пагинация.

---

## 🚀 Возможности
- **ADMIN**:
  - создавать/удалять карты и пользователей
  - блокировать/активировать карты
  - управлять балансом
  - видеть все карты (с фильтрацией и пагинацией)
- **USER**:
  - просматривать свои карты (с фильтрацией и пагинацией)
  - просматривать детали карты
  - переводить деньги между своими картами
  - создавать заявки на блокировку карты
- **Общее**:
  - шифрование номеров карт
  - ролевой доступ (Spring Security + JWT)
  - глобальная обработка ошибок (`ProblemDetail`)
  - документация через Swagger UI (springdoc-openapi)
  - миграции базы через Liquibase

---

## 🛠️ Технологии
- Java 21
- Spring Boot 3 (Web, Data JPA, Security)
- PostgreSQL
- Liquibase
- Docker / Docker Compose
- Swagger (springdoc-openapi)

---

## ⚙️ Запуск локально

### 1. Поднять базу данных (PostgreSQL)
```bash
docker run --name bank-postgres -e POSTGRES_DB=bank \
  -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 -d postgres:16-alpine
```

### 2. Настроить `application.yml`
Пример (по умолчанию уже есть в проекте):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bank
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: none
  liquibase:
    change-log: classpath:/db/changelog/db.changelog-master.yaml
```

### 3. Собрать и запустить
```bash
./mvnw clean package -DskipTests
java -jar target/*.jar
```

---

## 🐳 Запуск через Docker Compose

В проекте есть `docker-compose.yml`. Запускаем одной командой:

```bash
docker compose up --build
```

Сервисы:
- **app** → http://localhost:8080  
- **db (Postgres)** → порт 5432 (логин/пароль: postgres/postgres)

---

## 📖 Документация API

После запуска доступна Swagger UI:

- UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)  
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)  
- OpenAPI YAML: [http://localhost:8080/v3/api-docs.yaml](http://localhost:8080/v3/api-docs.yaml)


---

## 🧩 Миграции базы

- Все изменения схемы находятся в `src/main/resources/db/changelog/`.  
- Применяются автоматически при старте (Liquibase).  
- Для ручного применения можно использовать:
  ```bash
  ./mvnw liquibase:update
  ```

---

## ✅ Тесты

Запуск юнит- и интеграционных тестов:
```bash
./mvnw test
```

---

## 📂 Структура проекта
```
src/main/java/com/gnemirko/bank_rest
 ├── controller/        # REST-контроллеры (Admin, User)
 ├── dto/              # DTO классы (Create*, Response*)
 ├── entity/           # JPA сущности (User, Card, Request)
 ├── repository/       # Spring Data репозитории
 ├── service/          # Бизнес-логика
 └── security/         # Конфигурация Spring Security (JWT)
```

---

## 👨‍💻 Автор
Gleb Nemirko  
