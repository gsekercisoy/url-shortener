package com.gsekercisoy.urlshortener.web;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record ShortenRequest(
    @NotBlank(message = "url must not be blank")
    @URL(message = "url must be a valid URL (e.g. https://example.com)")
    String url) {
}
