package com.alexswd.userservice.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexswd.userservice.entity.User;
import com.alexswd.userservice.repository.UserRepository;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Service
public class UserService {

  @SuppressFBWarnings("EI_EXPOSE_REP2")
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public List<User> findAll() {
    return userRepository.findAll();
  }

  @Transactional(readOnly = true)
  public Optional<User> findById(Long id) {
    return userRepository.findById(id);
  }

  @Transactional
  public User create(String loginName, String password) {
    validate(loginName, password);
    User user = new User();
    user.setLoginName(loginName);
    user.setPassword(password);
    return userRepository.save(user);
  }

  @Transactional
  public Optional<User> update(Long id, String loginName, String password) {
    validate(loginName, password);
    return userRepository.findById(id).map(user -> {
      user.setLoginName(loginName);
      user.setPassword(password);
      return userRepository.update(user);
    });
  }

  @Transactional
  public boolean delete(Long id) {
    return userRepository.findById(id).map(user -> {
      userRepository.delete(user);
      return true;
    }).orElse(false);
  }

  private void validate(String loginName, String password) {
    if (loginName == null || loginName.isBlank() || password == null || password.isBlank()) {
      throw new IllegalArgumentException("loginName and password are required");
    }
  }
}
