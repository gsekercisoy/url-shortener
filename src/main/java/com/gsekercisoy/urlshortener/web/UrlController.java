package com.gsekercisoy.urlshortener.web;

import com.gsekercisoy.urlshortener.model.ShortUrl;
import com.gsekercisoy.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api")
public class UrlController {

  private final UrlService urlService;

  public UrlController(UrlService urlService) {
    this.urlService = urlService;
  }

  @PostMapping("/shorten")
  public ResponseEntity<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest request) {
    ShortUrl saved = urlService.shorten(request.url());
    String shortUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
        .path("/{code}")
        .buildAndExpand(saved.getCode())
        .toUriString();
    return ResponseEntity.ok(new ShortenResponse(saved.getCode(), shortUrl, saved.getLongUrl()));
  }

  @GetMapping("/stats/{code}")
  public ResponseEntity<StatsResponse> stats(@PathVariable String code) {
    return ResponseEntity.ok(StatsResponse.from(urlService.getStats(code)));
  }

  @GetMapping("/urls")
  public ResponseEntity<List<StatsResponse>> recent() {
    List<StatsResponse> body = urlService.recent().stream().map(StatsResponse::from).toList();
    return ResponseEntity.ok(body);
  }
}
