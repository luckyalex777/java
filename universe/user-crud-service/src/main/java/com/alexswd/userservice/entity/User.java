package com.alexswd.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends AuditedEntity {

  @Column(name = "login_name", nullable = false, unique = true, length = 100)
  private String loginName;

  @Column(nullable = false, length = 255)
  private String password;

  public String getLoginName() {
    return loginName;
  }

  public void setLoginName(String loginName) {
    this.loginName = loginName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
