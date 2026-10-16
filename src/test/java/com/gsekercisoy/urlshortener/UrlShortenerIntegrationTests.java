package com.gsekercisoy.urlshortener;

import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class UrlShortenerIntegrationTests {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Test
  void shortenHappyPath() throws Exception {
    mockMvc.perform(post("/api/shorten")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"url\":\"https://example.com/happy-path\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", matchesPattern("[A-Za-z0-9]{7}")))
        .andExpect(jsonPath("$.shortUrl", notNullValue()))
        .andExpect(jsonPath("$.longUrl").value("https://example.com/happy-path"));
  }

  @Test
  void shortenInvalidUrlReturns400() throws Exception {
    mockMvc.perform(post("/api/shorten")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"url\":\"not-a-url\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("bad_request"));
  }

  @Test
  void redirectReturns302AndIncrementsCounter() throws Exception {
    String code = createCode("https://example.com/redirect-counter");

    mockMvc.perform(get("/{code}", code))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", "https://example.com/redirect-counter"));

    mockMvc.perform(get("/{code}", code)).andExpect(status().isFound());

    mockMvc.perform(get("/api/stats/{code}", code))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.clickCount").value(2));
  }

  @Test
  void unknownCodeReturns404() throws Exception {
    mockMvc.perform(get("/api/stats/{code}", "ZZZZZZZ"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("not_found"));
  }

  @Test
  void statsContainsExpectedFields() throws Exception {
    String code = createCode("https://example.com/stats-fields");

    mockMvc.perform(get("/api/stats/{code}", code))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(code))
        .andExpect(jsonPath("$.longUrl").value("https://example.com/stats-fields"))
        .andExpect(jsonPath("$.clickCount").value(0))
        .andExpect(jsonPath("$.createdAt", notNullValue()));
  }

  @Test
  void duplicateUrlReturnsSameCode() throws Exception {
    String first = createCode("https://example.com/idempotent");
    String second = createCode("https://example.com/idempotent");
    org.junit.jupiter.api.Assertions.assertEquals(first, second);
  }

  @Test
  void recentUrlsListsCreatedEntries() throws Exception {
    createCode("https://example.com/recent-list");
    mockMvc.perform(get("/api/urls"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].code", notNullValue()))
        .andExpect(jsonPath("$[0].longUrl", notNullValue()));
  }

  private String createCode(String longUrl) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/shorten")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"url\":\"" + longUrl + "\"}"))
        .andExpect(status().isOk())
        .andReturn();
    JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
    return node.get("code").asText();
  }
}
