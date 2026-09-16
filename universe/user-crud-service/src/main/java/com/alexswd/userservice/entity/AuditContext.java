package com.alexswd.userservice.entity;

public final class AuditContext {

  private static final String DEFAULT_USER = "system";
  private static final ThreadLocal<String> CURRENT_USER = new ThreadLocal<>();

  private AuditContext() {}

  public static void setCurrentUser(String user) {
    CURRENT_USER.set(user == null || user.isBlank() ? DEFAULT_USER : user);
  }

  public static String currentUser() {
    String user = CURRENT_USER.get();
    return user == null ? DEFAULT_USER : user;
  }

  public static void clear() {
    CURRENT_USER.remove();
  }
}
