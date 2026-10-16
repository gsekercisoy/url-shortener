package com.gsekercisoy.urlshortener.web;

public record ShortenResponse(String code, String shortUrl, String longUrl) {
}
