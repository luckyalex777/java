package com.alexswd.userservice.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.stereotype.Component;

@Component
public class PasswordHasher {

  private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
  private static final String FORMAT_ALGORITHM = "pbkdf2-sha256";
  private static final String VERSION = "v1";
  private static final int ITERATIONS = 310_000;
  private static final int SALT_LENGTH = 16;
  private static final int KEY_LENGTH = 256;
  private static final int MIN_ITERATIONS = 10_000;
  private static final int MAX_ITERATIONS = 10_000_000;

  private final SecureRandom secureRandom;

  /** Creates a password hasher backed by a cryptographically secure random source. */
  public PasswordHasher() {
    this(new SecureRandom());
  }

  PasswordHasher(SecureRandom secureRandom) {
    this.secureRandom = secureRandom;
  }

  /**
   * Hashes a plaintext password using a fresh cryptographic salt.
   *
   * @param password plaintext password
   * @return encoded algorithm, version, parameters, salt, and derived key
   */
  public String hash(String password) {
    if (password == null) {
      throw new IllegalArgumentException("password is required");
    }

    byte[] salt = new byte[SALT_LENGTH];
    secureRandom.nextBytes(salt);
    byte[] derivedKey = deriveKey(password, salt, ITERATIONS);
    return String.join("$", FORMAT_ALGORITHM, VERSION, Integer.toString(ITERATIONS),
      Base64.getEncoder().encodeToString(salt),
      Base64.getEncoder().encodeToString(derivedKey));
  }

  /**
   * Verifies a plaintext password against an encoded password hash.
   *
   * @param password plaintext password
   * @param encodedHash encoded password hash
   * @return whether the password matches, or false for malformed hashes
   */
  public boolean matches(String password, String encodedHash) {
    if (password == null || encodedHash == null) {
      return false;
    }

    try {
      String[] parts = encodedHash.split("\\$", -1);
      if (parts.length != 5 || !FORMAT_ALGORITHM.equals(parts[0]) || !VERSION.equals(parts[1])) {
        return false;
      }
      int iterations = Integer.parseInt(parts[2]);
      if (iterations < MIN_ITERATIONS || iterations > MAX_ITERATIONS) {
        return false;
      }
      byte[] salt = Base64.getDecoder().decode(parts[3]);
      byte[] expectedKey = Base64.getDecoder().decode(parts[4]);
      if (salt.length != SALT_LENGTH || expectedKey.length != KEY_LENGTH / Byte.SIZE) {
        return false;
      }
      byte[] actualKey = deriveKey(password, salt, iterations);
      return MessageDigest.isEqual(expectedKey, actualKey);
    } catch (IllegalArgumentException | GeneralSecurityException exception) {
      return false;
    }
  }

  private byte[] deriveKey(String password, byte[] salt, int iterations) {
    char[] passwordChars = password.toCharArray();
    PBEKeySpec keySpec = new PBEKeySpec(passwordChars, salt, iterations, KEY_LENGTH);
    try {
      return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(keySpec).getEncoded();
    } catch (GeneralSecurityException exception) {
      throw new IllegalStateException("PBKDF2 is unavailable", exception);
    } finally {
      keySpec.clearPassword();
    }
  }
}