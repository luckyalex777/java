CREATE TABLE users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  login_name VARCHAR(100) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  created_when TIMESTAMP(6) NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  updated_when TIMESTAMP(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_login_name (login_name)
);