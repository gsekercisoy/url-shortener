package com.gsekercisoy.urlshortener.repository;

import com.gsekercisoy.urlshortener.model.ShortUrl;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
  Optional<ShortUrl> findByCode(String code);

  Optional<ShortUrl> findByLongUrl(String longUrl);

  boolean existsByCode(String code);

  List<ShortUrl> findTop50ByOrderByCreatedAtDesc();
}
