package com.alexswd.userservice.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class UserResponseTest {

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Test
  void serializationDoesNotExposePlaintextOrPasswordHash() throws JsonProcessingException {
    String plaintext = "secret";
    String passwordHash = "pbkdf2-sha256$v1$310000$c2FsdC1ieXRlcw==$a2V5";
    UserController.UserResponse response = new UserController.UserResponse(1L, "alex", "admin",
        Instant.parse("2026-01-01T00:00:00Z"), "admin", Instant.parse("2026-01-01T00:00:00Z"));

    String json = objectMapper.writeValueAsString(response);

    assertTrue(json.contains("\"loginName\":\"alex\""));
    assertFalse(json.contains(plaintext));
    assertFalse(json.contains(passwordHash));
    assertFalse(json.contains("password"));
    assertFalse(json.contains("passwordHash"));
  }
}
