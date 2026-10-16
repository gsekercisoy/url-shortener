# URL Kısaltıcı

> 🌐 **Dil:** [English](README.md) | [Türkçe](README_tr.md)

**Java / Spring Boot işe alım vitrini** olarak hazırlanmış, minimum bağımlılıklı bir URL kısaltma servisi. Harici altyapı gerekmez: dosya tabanlı H2 + işlem-içi Caffeine önbellek.

## Özellikler

- Uzun URL'leri 7 karakterli Base62 kodlara kısaltma (çakışma durumunda yeniden deneme)
- Tıklama sayaçlı `302` yönlendirme
- Kod bazında istatistik + son 50 kayıt listesi
- Idempotent kısaltma (aynı URL → aynı kod)
- İstatistik okumalarında Caffeine önbellek (redirect'te invalidate)
- `@RestControllerAdvice` ile `400` / `404` JSON hata gövdeleri
- MockMvc entegrasyon testleri (`mvn test`)

Kapsam dışı: rate limiting, kimlik doğrulama, özel alias (bkz. "Yol Haritası").

## Teknolojiler

| Katman     | Seçim                                                          |
|------------|----------------------------------------------------------------|
| Dil        | Java 17                                                        |
| Framework  | Spring Boot 3.2.x, Spring Web, Spring Data JPA, Validation     |
| Veritabanı | H2 (dev'de dosya `./data`, testte in-memory)                   |
| Önbellek   | Caffeine (işlem-içi; Redis geçiş notu aşağıda)                 |
| Derleme    | Maven 3.9.x                                                    |
| Test       | JUnit 5, MockMvc, H2                                           |
| Paketleme  | Dockerfile (`eclipse-temurin:17-jre`), docker-compose           |

## Hızlı Başlatma

### Lokal (Maven)

```bash
mvn spring-boot:run
# uygulama http://localhost:8080 adresinde
```

Testler:

```bash
mvn test
```

H2 konsolu (dev): http://localhost:8080/h2-console — JDBC URL `jdbc:h2:file:./data/urlshortener`, kullanıcı `sa`, şifre boş.

### Docker

```bash
docker compose up --build
# uygulama http://localhost:8080, veri ./data altında saklanır
```

## API

| Metot | Uç Nokta             | Açıklama                        |
|-------|----------------------|---------------------------------|
| POST  | `/api/shorten`       | URL kısalt                      |
| GET   | `/{code}`            | 302 yönlendirme (sayaç artar)    |
| GET   | `/api/stats/{code}`  | Tek kodun istatistiği            |
| GET   | `/api/urls`          | Son 50 kısaltılmış URL           |

```bash
# Kısalt
curl -s -X POST localhost:8080/api/shorten \
  -H 'Content-Type: application/json' \
  -d '{"url":"https://example.com/cok/uzun/yol"}'
# -> {"code":"aB3xK9q","shortUrl":"http://localhost:8080/aB3xK9q","longUrl":"https://example.com/..."}

# Yönlendirme (302)
curl -i localhost:8080/aB3xK9q

# İstatistik
curl -s localhost:8080/api/stats/aB3xK9q

# Son kayıtlar
curl -s localhost:8080/api/urls

# Geçersiz URL -> 400
curl -s -X POST localhost:8080/api/shorten \
  -H 'Content-Type: application/json' -d '{"url":"not-a-url"}'

# Bilinmeyen kod -> 404
curl -s localhost:8080/api/stats/ZZZZZZZ
```

## Klasör Yapısı

```
src/main/java/com/gsekercisoy/urlshortener/
  UrlShortenerApplication.java
  config/CacheConfig.java        # Caffeine CacheManager ("urlStats")
  model/ShortUrl.java            # id, code (unique), longUrl, createdAt, clickCount
  repository/ShortUrlRepository.java
  service/CodeGenerator.java     # Base62 SecureRandom, 7 karakter
  service/UrlService.java        # shorten / resolveAndCount / getStats / recent
  service/UrlNotFoundException.java
  web/UrlController.java         # POST /api/shorten, GET /api/stats/{code}, GET /api/urls
  web/RedirectController.java    # GET /{code} -> 302
  web/GlobalExceptionHandler.java# 400/404 JSON gövdeler
  web/ShortenRequest.java / ShortenResponse.java / StatsResponse.java
src/test/java/.../UrlShortenerIntegrationTests.java  # 7 MockMvc testi
src/test/java/.../CodeGeneratorTest.java             # 2 birim testi
```

## Redis'e Geçiş

Uygulama sıfır altyapıyla çalışsın diye Caffeine kullanır. Redis'e geçmek için:

1. `pom.xml`'e `spring-boot-starter-data-redis` ekleyin.
2. `CacheConfig`'teki `CaffeineCacheManager` yerine `RedisCacheManager` kullanın (`urlStats` için 30 dk TTL).
3. Redis'i başlatın: `docker compose --profile redis up` (`redis` servisi `redis` profili altında hazır).
4. Compose ile çalışırken `spring.data.redis.host=redis` / `spring.data.redis.port=6379` tanımlayın.

Servis katmanında değişiklik gerekmez — `UrlService` Spring Cache anotasyonları kullanır (`@Cacheable` / `@CacheEvict`).

## Yol Haritası (kapsam dışı)

- Rate limiting (örn. Bucket4j / gateway seviyesi)
- Özel alias + TTL/süre sonu
- Auth + kullanıcı bazında URL yönetimi
