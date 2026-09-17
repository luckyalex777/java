package com.alexswd.torrent;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the TorrentParser class.
 */
@DisplayName("TorrentParser Tests")
class TorrentParserTest {

  @BeforeEach
  void setUp() {
    // Test setup
  }

  @Test
  @DisplayName("Should throw TorrentException for null filepath")
  void testParseNullFilepath() {
    assertThrows(TorrentException.class, () -> TorrentParser.parse((String) null));
  }

  @Test
  @DisplayName("Should throw TorrentException for empty filepath")
  void testParseEmptyFilepath() {
    assertThrows(TorrentException.class, () -> TorrentParser.parse(""));
  }

  @Test
  @DisplayName("Should throw TorrentException for non-existent file")
  void testParseNonExistentFile() {
    String nonExistentPath = System.getProperty("java.io.tmpdir") + File.separator + "torrent-test-"
        + System.nanoTime() + ".torrent";
    assertThrows(TorrentException.class, () -> TorrentParser.parse(nonExistentPath));
  }

  @Test
  @DisplayName("Should throw TorrentException for null File object")
  void testParseNullFile() {
    assertThrows(TorrentException.class, () -> TorrentParser.parse((File) null));
  }

  @Test
  @DisplayName("Should throw TorrentException for null InputStream")
  void testParseNullInputStream() {
    assertThrows(TorrentException.class, () -> TorrentParser.parse((java.io.InputStream) null));
  }
}
