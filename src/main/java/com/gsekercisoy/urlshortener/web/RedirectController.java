package com.gsekercisoy.urlshortener.web;

import com.gsekercisoy.urlshortener.service.UrlService;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RedirectController {

  private final UrlService urlService;

  public RedirectController(UrlService urlService) {
    this.urlService = urlService;
  }

  @GetMapping("/{code:[A-Za-z0-9]{6,8}}")
  public ResponseEntity<Void> redirect(@PathVariable String code) {
    String target = urlService.resolveAndCount(code);
    return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(target)).build();
  }
}
