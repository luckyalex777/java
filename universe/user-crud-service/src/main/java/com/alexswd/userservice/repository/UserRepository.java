package com.alexswd.userservice.repository;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import com.alexswd.userservice.entity.User;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Repository
public class UserRepository {

  @SuppressFBWarnings("EI_EXPOSE_REP2")
  private final SessionFactory sessionFactory;

  public UserRepository(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  public List<User> findAll() {
    return sessionFactory.getCurrentSession()
        .createQuery("from User order by loginName", User.class).getResultList();
  }

  public Optional<User> findById(Long id) {
    return Optional.ofNullable(sessionFactory.getCurrentSession().get(User.class, id));
  }

  public User save(User user) {
    sessionFactory.getCurrentSession().persist(user);
    return user;
  }

  public User update(User user) {
    return (User) sessionFactory.getCurrentSession().merge(user);
  }

  public void delete(User user) {
    sessionFactory.getCurrentSession().remove(user);
  }
}
