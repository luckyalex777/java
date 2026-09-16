package com.alexswd.userservice.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

  private final PasswordHasher passwordHasher = new PasswordHasher();

  @Test
  void hashesWithSaltAndMatchesOriginalPassword() {
    String firstHash = passwordHasher.hash("secret");
    String secondHash = passwordHasher.hash("secret");

    assertNotEquals(firstHash, secondHash);
    assertTrue(firstHash.startsWith("pbkdf2-sha256$v1$"));
    assertTrue(passwordHasher.matches("secret", firstHash));
    assertFalse(passwordHasher.matches("wrong", firstHash));
  }

  @Test
  void rejectsMalformedHashes() {
    assertFalse(passwordHasher.matches("secret", ""));
    assertFalse(passwordHasher.matches("secret", "pbkdf2-sha256$v1$not-a-number$salt$key"));
    assertFalse(passwordHasher.matches("secret", "pbkdf2-sha256$v2$310000$c2FsdA$a2V5"));
    assertFalse(passwordHasher.matches("secret", "pbkdf2-sha256$v1$310000$%%%$a2V5"));
    assertFalse(passwordHasher.matches("secret", "pbkdf2-sha256$v1$310000$c2FsdA$"));
  }
}
