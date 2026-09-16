package com.alexswd.userservice.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AuditContextTest {

  @AfterEach
  void clearContext() {
    AuditContext.clear();
  }

  @Test
  void usesSystemWhenNoUserIsProvided() {
    assertEquals("system", AuditContext.currentUser());
  }

  @Test
  void replacesBlankUserWithSystem() {
    AuditContext.setCurrentUser(" ");
    assertEquals("system", AuditContext.currentUser());
  }
}
