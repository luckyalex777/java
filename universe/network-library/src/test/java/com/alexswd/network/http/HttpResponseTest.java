package com.alexswd.network.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for HttpResponse class.
 */
@DisplayName("HttpResponse")
class HttpResponseTest {

  @Test
  @DisplayName("Should create HttpResponse with status and body")
  void testHttpResponseCreation() {
    // Arrange
    int status = 200;
    byte[] body = "Hello World".getBytes();

    // Act
    HttpResponse response = new HttpResponse(status, body);

    // Assert
    assertThat(response.getStatus(), equalTo(200));
    assertThat(response.getBody(), notNullValue());
    assertThat(response.getBody().length, equalTo(11));
  }

  @Test
  @DisplayName("Should store status code correctly")
  void testStatusCodeStorage() {
    // Arrange
    int[] statusCodes = {200, 301, 404, 500, 503};

    // Act & Assert
    for (int status : statusCodes) {
      HttpResponse response = new HttpResponse(status, new byte[0]);
      assertThat(response.getStatus(), equalTo(status));
    }
  }

  @Test
  @DisplayName("Should store body correctly")
  void testBodyStorage() {
    // Arrange
    byte[] body = "test response body".getBytes();
    HttpResponse response = new HttpResponse(200, body);

    // Act
    byte[] retrievedBody = response.getBody();

    // Assert
    assertThat(retrievedBody.length, equalTo(body.length));
    for (int i = 0; i < body.length; i++) {
      assertThat(retrievedBody[i], equalTo(body[i]));
    }
  }

  @Test
  @DisplayName("Should handle empty body")
  void testEmptyBody() {
    // Arrange
    byte[] emptyBody = new byte[0];

    // Act
    HttpResponse response = new HttpResponse(204, emptyBody);

    // Assert
    assertThat(response.getStatus(), equalTo(204));
    assertThat(response.getBody().length, equalTo(0));
  }

  @Test
  @DisplayName("Should handle large body")
  void testLargeBody() {
    // Arrange
    byte[] largeBody = new byte[10000];
    for (int i = 0; i < largeBody.length; i++) {
      largeBody[i] = (byte) (i % 256);
    }

    // Act
    HttpResponse response = new HttpResponse(200, largeBody);

    // Assert
    assertThat(response.getBody().length, equalTo(10000));
  }
}
