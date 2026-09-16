package com.alexswd.userservice.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.alexswd.userservice.entity.User;
import com.alexswd.userservice.repository.UserRepository;
import com.alexswd.userservice.security.PasswordHasher;

class UserServiceTest {

  private final PasswordHasher passwordHasher = new PasswordHasher();

  @Test
  void createAndUpdateStoreHashes() {
    InMemoryUserRepository repository = new InMemoryUserRepository();
    UserService userService = new UserService(repository, passwordHasher);

    User created = userService.create("alex", "first-secret");
    assertTrue(passwordHasher.matches("first-secret", created.getPasswordHash()));
    assertFalse(created.getPasswordHash().equals("first-secret"));

    User updated = userService.update(1L, "alex-updated", "second-secret").orElseThrow();
    assertTrue(passwordHasher.matches("second-secret", updated.getPasswordHash()));
    assertFalse(passwordHasher.matches("first-secret", updated.getPasswordHash()));
  }

  @Test
  void validationRunsBeforeRepositoryWrite() {
    InMemoryUserRepository repository = new InMemoryUserRepository();
    UserService userService = new UserService(repository, passwordHasher);

    assertThrows(IllegalArgumentException.class, () -> userService.create("alex", ""));
    assertTrue(repository.savedUsers.isEmpty());
  }

  @Test
  void findByNamePasswordReturnsUserForCorrectCredentials() {
    InMemoryUserRepository repository = new InMemoryUserRepository();
    UserService userService = new UserService(repository, passwordHasher);
    User created = userService.create("alex", "secret");

    assertSame(created, userService.findByNamePassword("alex", "secret").orElseThrow());
  }

  @Test
  void findByNamePasswordReturnsEmptyForWrongOrUnknownCredentials() {
    InMemoryUserRepository repository = new InMemoryUserRepository();
    UserService userService = new UserService(repository, passwordHasher);
    userService.create("alex", "secret");

    assertTrue(userService.findByNamePassword("alex", "wrong").isEmpty());
    assertTrue(userService.findByNamePassword("unknown", "secret").isEmpty());
  }

  @Test
  void findByNamePasswordReturnsEmptyForNullOrBlankCredentials() {
    InMemoryUserRepository repository = new InMemoryUserRepository();
    UserService userService = new UserService(repository, passwordHasher);
    userService.create("alex", "secret");

    assertTrue(userService.findByNamePassword(null, "secret").isEmpty());
    assertTrue(userService.findByNamePassword("alex", null).isEmpty());
    assertTrue(userService.findByNamePassword("", "secret").isEmpty());
    assertTrue(userService.findByNamePassword("alex", "").isEmpty());
  }

  @Test
  void findByNamePasswordReturnsEmptyForMalformedStoredHash() {
    InMemoryUserRepository repository = new InMemoryUserRepository();
    UserService userService = new UserService(repository, passwordHasher);
    User user = userService.create("alex", "secret");
    user.setPasswordHash("not-a-password-hash");

    assertTrue(userService.findByNamePassword("alex", "secret").isEmpty());
  }

  private static final class InMemoryUserRepository extends UserRepository {

    private final List<User> savedUsers = new ArrayList<>();

    private InMemoryUserRepository() {
      super(null);
    }

    @Override
    public User save(User user) {
      savedUsers.add(user);
      return user;
    }

    @Override
    public User update(User user) {
      return user;
    }

    @Override
    public Optional<User> findById(Long id) {
      if (id == 1L) {
        return savedUsers.stream().findFirst();
      }
      return Optional.empty();
    }

    @Override
    public Optional<User> findByLoginName(String loginName) {
      return savedUsers.stream()
          .filter(user -> loginName != null && loginName.equals(user.getLoginName()))
          .findFirst();
    }
  }
}
