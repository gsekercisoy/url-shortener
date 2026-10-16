package com.gsekercisoy.urlshortener.web;

import com.gsekercisoy.urlshortener.model.ShortUrl;
import java.time.Instant;

public record StatsResponse(String code, String longUrl, long clickCount, Instant createdAt) {

  public static StatsResponse from(ShortUrl entity) {
    return new StatsResponse(
        entity.getCode(), entity.getLongUrl(), entity.getClickCount(), entity.getCreatedAt());
  }
}
