# URL Shortener

> 🌐 **Language:** [English](README.md) | [Türkçe](README_tr.md)

A minimal, dependency-light URL shortener built as a **Java / Spring Boot hiring showcase**. No external infrastructure required: file-based H2 + in-process Caffeine cache.

## Features

- Shorten long URLs into 7-char Base62 codes (collision retry)
- `302` redirect with click counting
- Per-code stats + last-50 listing
- Idempotent shortening (same long URL → same code)
- Caffeine cache on stats reads (evicted on redirect)
- JSON error bodies for `400` / `404` via `@RestControllerAdvice`
- MockMvc integration tests (`mvn test`)

Out of scope: rate limiting, authentication, custom aliases (see "Roadmap").

## Tech Stack

| Layer      | Choice                                                        |
|------------|---------------------------------------------------------------|
| Language   | Java 17                                                       |
| Framework  | Spring Boot 3.2.x, Spring Web, Spring Data JPA, Validation    |
| Database   | H2 (file `./data` in dev, in-memory in tests)                  |
| Cache      | Caffeine (in-process; Redis migration notes below)            |
| Build      | Maven 3.9.x                                                   |
| Tests      | JUnit 5, MockMvc, H2                                          |
| Packaging  | Dockerfile (`eclipse-temurin:17-jre`), docker-compose          |

## Quick Start

### Local (Maven)

```bash
mvn spring-boot:run
# app on http://localhost:8080
```

Run tests:

```bash
mvn test
```

H2 console (dev): http://localhost:8080/h2-console — JDBC URL `jdbc:h2:file:./data/urlshortener`, user `sa`, empty password.

### Docker

```bash
docker compose up --build
# app on http://localhost:8080, data persisted in ./data
```

## API

| Method | Endpoint           | Description                        |
|--------|--------------------|------------------------------------|
| POST   | `/api/shorten`     | Shorten a URL                      |
| GET    | `/{code}`          | 302 redirect (increments counter)  |
| GET    | `/api/stats/{code}`| Stats for one code                 |
| GET    | `/api/urls`        | Last 50 shortened URLs             |

```bash
# Shorten
curl -s -X POST localhost:8080/api/shorten \
  -H 'Content-Type: application/json' \
  -d '{"url":"https://example.com/some/long/path"}'
# -> {"code":"aB3xK9q","shortUrl":"http://localhost:8080/aB3xK9q","longUrl":"https://example.com/..."}

# Redirect (302)
curl -i localhost:8080/aB3xK9q

# Stats
curl -s localhost:8080/api/stats/aB3xK9q
# -> {"code":"aB3xK9q","longUrl":"https://...","clickCount":1,"createdAt":"..."}

# Recent
curl -s localhost:8080/api/urls

# Invalid URL -> 400
curl -s -X POST localhost:8080/api/shorten \
  -H 'Content-Type: application/json' -d '{"url":"not-a-url"}'
# -> {"error":"bad_request","message":"url: must be a valid URL ..."}

# Unknown code -> 404
curl -s localhost:8080/api/stats/ZZZZZZZ
# -> {"error":"not_found","message":"Short URL not found: ZZZZZZZ"}
```

## Project Structure

```
src/main/java/com/gsekercisoy/urlshortener/
  UrlShortenerApplication.java
  config/CacheConfig.java        # Caffeine CacheManager ("urlStats")
  model/ShortUrl.java            # id, code (unique), longUrl, createdAt, clickCount
  repository/ShortUrlRepository.java
  service/CodeGenerator.java     # Base62 SecureRandom, 7 chars
  service/UrlService.java        # shorten / resolveAndCount / getStats / recent
  service/UrlNotFoundException.java
  web/UrlController.java         # POST /api/shorten, GET /api/stats/{code}, GET /api/urls
  web/RedirectController.java    # GET /{code} -> 302
  web/GlobalExceptionHandler.java# 400/404 JSON bodies
  web/ShortenRequest.java / ShortenResponse.java / StatsResponse.java
src/test/java/.../UrlShortenerIntegrationTests.java  # 7 MockMvc tests
src/test/java/.../CodeGeneratorTest.java             # 2 unit tests
```

## Switching to Redis

The app uses Caffeine so it runs with zero infra. To move to Redis:

1. Add `spring-boot-starter-data-redis` to `pom.xml`.
2. Replace `CacheConfig`'s `CaffeineCacheManager` with a `RedisCacheManager` (configure TTL 30 min for `urlStats`).
3. Start Redis: `docker compose --profile redis up` (a `redis` service is already defined under the `redis` profile).
4. Set `spring.data.redis.host=redis` / `spring.data.redis.port=6379` when running via compose.

No service-layer changes needed — `UrlService` uses Spring Cache annotations (`@Cacheable` / `@CacheEvict`).

## Roadmap (out of scope)

- Rate limiting (e.g. Bucket4j / gateway-level)
- Custom aliases + TTL/expiry
- Auth + per-user URL management
