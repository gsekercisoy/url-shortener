package com.gsekercisoy.urlshortener.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "short_urls")
public class ShortUrl {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 8)
  private String code;

  @Column(nullable = false, length = 2048)
  private String longUrl;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private long clickCount;

  protected ShortUrl() {
  }

  public ShortUrl(String code, String longUrl) {
    this.code = code;
    this.longUrl = longUrl;
    this.createdAt = Instant.now();
    this.clickCount = 0;
  }

  public Long getId() {
    return id;
  }

  public String getCode() {
    return code;
  }

  public String getLongUrl() {
    return longUrl;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public long getClickCount() {
    return clickCount;
  }

  public void setClickCount(long clickCount) {
    this.clickCount = clickCount;
  }
}
