package com.gsekercisoy.urlshortener.service;

import com.gsekercisoy.urlshortener.model.ShortUrl;
import com.gsekercisoy.urlshortener.repository.ShortUrlRepository;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UrlService {

  private static final int MAX_COLLISION_RETRIES = 10;

  private final ShortUrlRepository repository;
  private final CodeGenerator codeGenerator;

  public UrlService(ShortUrlRepository repository, CodeGenerator codeGenerator) {
    this.repository = repository;
    this.codeGenerator = codeGenerator;
  }

  @Transactional
  public ShortUrl shorten(String longUrl) {
    String normalized = longUrl.trim();
    return repository.findByLongUrl(normalized).orElseGet(() -> {
      String code = generateUniqueCode();
      return repository.save(new ShortUrl(code, normalized));
    });
  }

  @Transactional
  @CacheEvict(value = "urlStats", key = "#code")
  public String resolveAndCount(String code) {
    ShortUrl entity = repository.findByCode(code)
        .orElseThrow(() -> new UrlNotFoundException(code));
    entity.setClickCount(entity.getClickCount() + 1);
    repository.save(entity);
    return entity.getLongUrl();
  }

  @Transactional(readOnly = true)
  @Cacheable(value = "urlStats", key = "#code")
  public ShortUrl getStats(String code) {
    return repository.findByCode(code)
        .orElseThrow(() -> new UrlNotFoundException(code));
  }

  @Transactional(readOnly = true)
  public List<ShortUrl> recent() {
    return repository.findTop50ByOrderByCreatedAtDesc();
  }

  private String generateUniqueCode() {
    for (int i = 0; i < MAX_COLLISION_RETRIES; i++) {
      String candidate = codeGenerator.generate();
      if (!repository.existsByCode(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Could not generate a unique short code");
  }
}
