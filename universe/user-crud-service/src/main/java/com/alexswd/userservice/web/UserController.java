package com.alexswd.userservice.web;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alexswd.userservice.entity.AuditContext;
import com.alexswd.userservice.entity.User;
import com.alexswd.userservice.service.UserService;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@RestController
@RequestMapping("/api/users")
public class UserController {

  @SuppressFBWarnings("EI_EXPOSE_REP2")
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public List<UserResponse> findAll() {
    return userService.findAll().stream().map(UserResponse::from).toList();
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
    return ResponseEntity.of(userService.findById(id).map(UserResponse::from));
  }

  @PostMapping
  public ResponseEntity<UserResponse> create(
      @RequestHeader(value = "X-User", defaultValue = "system") String auditor,
      @RequestBody UserRequest request) {
    try {
      AuditContext.setCurrentUser(auditor);
      User user = userService.create(request.loginName(), request.password());
      return ResponseEntity.created(URI.create("/api/users/" + user.getId()))
          .body(UserResponse.from(user));
    } finally {
      AuditContext.clear();
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserResponse> update(@PathVariable Long id,
      @RequestHeader(value = "X-User", defaultValue = "system") String auditor,
      @RequestBody UserRequest request) {
    try {
      AuditContext.setCurrentUser(auditor);
      return ResponseEntity.of(
          userService.update(id, request.loginName(), request.password()).map(UserResponse::from));
    } finally {
      AuditContext.clear();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    return userService.delete(id) ? ResponseEntity.noContent().build()
        : ResponseEntity.notFound().build();
  }

  public record UserRequest(String loginName, String password) {}

  public record UserResponse(
      Long id,
      String loginName,
      String createdBy,
      java.time.Instant createdWhen,
      String updatedBy,
      java.time.Instant updatedWhen) {
    static UserResponse from(User user) {
      return new UserResponse(
          user.getId(),
          user.getLoginName(),
          user.getCreatedBy(),
          user.getCreatedWhen(),
          user.getUpdatedBy(),
          user.getUpdatedWhen());
    }
  }
}
