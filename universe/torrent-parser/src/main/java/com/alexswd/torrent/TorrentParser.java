package com.alexswd.torrent;

import com.alexswd.bencode.BencodeDecoder;
import com.alexswd.bencode.BencodeDict;
import com.alexswd.bencode.BencodeException;
import com.alexswd.bencode.BencodeInteger;
import com.alexswd.bencode.BencodeList;
import com.alexswd.bencode.BencodeString;
import com.alexswd.bencode.BencodeValue;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parser for torrent files using bencode format.
 *
 * <p>
 * This class provides static methods to parse torrent files and extract their metadata, including
 * announce URLs, piece information, file lists, and other metadata.
 */
public final class TorrentParser {

  private static final Logger logger = LoggerFactory.getLogger(TorrentParser.class);

  private TorrentParser() {
    // Utility class, not instantiable
  }

  /**
   * Parses a torrent file from the specified file path.
   *
   * @param filepath the path to the torrent file
   * @return the parsed TorrentInfo
   * @throws TorrentException if parsing fails
   */
  public static TorrentInfo parse(String filepath) throws TorrentException {
    if (filepath == null || filepath.isEmpty()) {
      throw new TorrentException("Filepath cannot be null or empty");
    }
    return parse(new File(filepath));
  }

  /**
   * Parses a torrent file from the specified File object.
   *
   * @param file the torrent file
   * @return the parsed TorrentInfo
   * @throws TorrentException if parsing fails
   */
  public static TorrentInfo parse(File file) throws TorrentException {
    if (file == null) {
      throw new TorrentException("File cannot be null");
    }
    if (!file.exists()) {
      throw new TorrentException("File does not exist: " + file.getAbsolutePath());
    }
    if (!file.isFile()) {
      throw new TorrentException("Path is not a file: " + file.getAbsolutePath());
    }

    try (FileInputStream stream = new FileInputStream(file)) {
      return parse(stream);
    } catch (IOException e) {
      throw new TorrentException("Failed to read torrent file: " + file.getAbsolutePath(), e);
    }
  }

  /**
   * Parses a torrent file from the specified input stream.
   *
   * @param stream the input stream containing torrent data
   * @return the parsed TorrentInfo
   * @throws TorrentException if parsing fails
   */
  public static TorrentInfo parse(InputStream stream) throws TorrentException {
    if (stream == null) {
      throw new TorrentException("InputStream cannot be null");
    }

    try {
      byte[] data = stream.readAllBytes();
      String bencodedContent = new String(data, StandardCharsets.UTF_8);

      BencodeDecoder decoder = new BencodeDecoder();
      BencodeValue decoded = decoder.decode(bencodedContent);

      if (!(decoded instanceof BencodeDict)) {
        throw new TorrentException("Root element of torrent file must be a dictionary");
      }

      BencodeDict rootDict = (BencodeDict) decoded;
      return parseTorrentDict(rootDict);

    } catch (IOException e) {
      throw new TorrentException("Failed to read from input stream", e);
    } catch (BencodeException e) {
      throw new TorrentException("Failed to decode bencode content", e);
    }
  }

  /**
   * Parses the root bencode dictionary to extract torrent information.
   *
   * @param rootDict the root bencode dictionary
   * @return the parsed TorrentInfo
   * @throws TorrentException if parsing fails
   */
  private static TorrentInfo parseTorrentDict(BencodeDict rootDict) throws TorrentException {
    TorrentInfo.Builder builder = TorrentInfo.builder();

    // Parse announce URL
    BencodeValue announceValue = rootDict.get("announce");
    if (announceValue instanceof BencodeString) {
      builder.announceUrl(((BencodeString) announceValue).getValue());
    }

    // Parse announce list (multiple trackers)
    BencodeValue announceListValue = rootDict.get("announce-list");
    if (announceListValue instanceof BencodeList) {
      List<List<String>> tierList = parseAnnounceList((BencodeList) announceListValue);
      builder.announceList(tierList);
    }

    // Parse info dictionary
    BencodeValue infoValue = rootDict.get("info");
    if (!(infoValue instanceof BencodeDict)) {
      throw new TorrentException("Missing or invalid 'info' dictionary in torrent file");
    }

    BencodeDict infoDict = (BencodeDict) infoValue;
    parseInfoDict(infoDict, builder, rootDict);

    // Parse optional metadata fields
    BencodeValue commentValue = rootDict.get("comment");
    if (commentValue instanceof BencodeString) {
      builder.comment(((BencodeString) commentValue).getValue());
    }

    BencodeValue createdByValue = rootDict.get("created by");
    if (createdByValue instanceof BencodeString) {
      builder.createdBy(((BencodeString) createdByValue).getValue());
    }

    BencodeValue creationDateValue = rootDict.get("creation date");
    if (creationDateValue instanceof BencodeInteger) {
      long timestamp = ((BencodeInteger) creationDateValue).getValue();
      builder.creationDate(new Date(timestamp * 1000));
    }

    BencodeValue encodingValue = rootDict.get("encoding");
    if (encodingValue instanceof BencodeString) {
      builder.encoding(((BencodeString) encodingValue).getValue());
    }

    return builder.build();
  }

  /**
   * Parses the info dictionary to extract name, file information, and piece data.
   *
   * @param infoDict the info dictionary
   * @param builder the TorrentInfo builder
   * @param rootDict the root dictionary for info hash calculation
   * @throws TorrentException if parsing fails
   */
  private static void parseInfoDict(BencodeDict infoDict, TorrentInfo.Builder builder,
      BencodeDict rootDict) throws TorrentException {
    // Parse name
    BencodeValue nameValue = infoDict.get("name");
    if (!(nameValue instanceof BencodeString)) {
      throw new TorrentException("Missing or invalid 'name' in info dictionary");
    }
    String name = ((BencodeString) nameValue).getValue();
    builder.name(name);

    // Parse piece length
    BencodeValue pieceLengthValue = infoDict.get("piece length");
    if (!(pieceLengthValue instanceof BencodeInteger)) {
      throw new TorrentException("Missing or invalid 'piece length' in info dictionary");
    }
    long pieceLength = ((BencodeInteger) pieceLengthValue).getValue();
    if (pieceLength <= 0) {
      throw new TorrentException("Piece length must be positive: " + pieceLength);
    }
    builder.pieceLength(pieceLength);

    // Parse pieces (SHA-1 hashes)
    BencodeValue piecesValue = infoDict.get("pieces");
    if (!(piecesValue instanceof BencodeString)) {
      throw new TorrentException("Missing or invalid 'pieces' in info dictionary");
    }
    byte[] pieces = ((BencodeString) piecesValue).getByteValue();
    if (pieces == null || pieces.length % 20 != 0) {
      throw new TorrentException("Pieces data length must be a multiple of 20 (SHA-1 hash size)");
    }
    builder.pieces(pieces.clone());

    // Calculate info hash
    try {
      byte[] infoHash = calculateInfoHash(rootDict);
      if (infoHash != null) {
        builder.infoHash(infoHash.clone());
      }
    } catch (Exception e) {
      logger.warn("Failed to calculate info hash", e);
    }

    // Parse files
    long totalLength = 0;
    BencodeValue filesValue = infoDict.get("files");

    if (filesValue instanceof BencodeList) {
      // Multi-file torrent
      List<TorrentInfo.FileEntry> fileList = parseFileList((BencodeList) filesValue);
      for (TorrentInfo.FileEntry file : fileList) {
        totalLength += file.getLength();
      }
      builder.files(fileList);
    } else {
      // Single-file torrent
      BencodeValue lengthValue = infoDict.get("length");
      if (!(lengthValue instanceof BencodeInteger)) {
        throw new TorrentException("Missing or invalid 'length' for single-file torrent");
      }
      totalLength = ((BencodeInteger) lengthValue).getValue();
      if (totalLength < 0) {
        throw new TorrentException("File length cannot be negative: " + totalLength);
      }

      List<TorrentInfo.FileEntry> singleFileList = new ArrayList<>();
      singleFileList.add(new TorrentInfo.FileEntry(List.of(name), totalLength));
      builder.files(singleFileList);
    }

    builder.length(totalLength);
  }

  /**
   * Parses the announce-list field containing multiple tracker URLs organized by tier.
   *
   * @param announceList the announce list bencode value
   * @return a list of tiers, each containing tracker URLs
   * @throws TorrentException if parsing fails
   */
  private static List<List<String>> parseAnnounceList(BencodeList announceList)
      throws TorrentException {
    List<List<String>> tierList = new ArrayList<>();

    for (BencodeValue tierValue : announceList.getValues()) {
      if (!(tierValue instanceof BencodeList)) {
        throw new TorrentException("Each announce list tier must be a list");
      }

      List<String> tier = new ArrayList<>();
      BencodeList tierListInner = (BencodeList) tierValue;

      for (BencodeValue urlValue : tierListInner.getValues()) {
        if (!(urlValue instanceof BencodeString)) {
          throw new TorrentException("Each announce URL must be a string");
        }
        tier.add(((BencodeString) urlValue).getValue());
      }

      tierList.add(Collections.unmodifiableList(tier));
    }

    return Collections.unmodifiableList(tierList);
  }

  /**
   * Parses the files list for multi-file torrents.
   *
   * @param filesList the files list bencode value
   * @return a list of FileEntry objects
   * @throws TorrentException if parsing fails
   */
  private static List<TorrentInfo.FileEntry> parseFileList(BencodeList filesList)
      throws TorrentException {
    List<TorrentInfo.FileEntry> files = new ArrayList<>();

    for (BencodeValue fileValue : filesList.getValues()) {
      if (!(fileValue instanceof BencodeDict)) {
        throw new TorrentException("Each file entry must be a dictionary");
      }

      BencodeDict fileDict = (BencodeDict) fileValue;

      // Parse file path
      BencodeValue pathValue = fileDict.get("path");
      if (!(pathValue instanceof BencodeList)) {
        throw new TorrentException("File path must be a list");
      }

      List<String> pathComponents = new ArrayList<>();
      BencodeList pathList = (BencodeList) pathValue;
      for (BencodeValue component : pathList.getValues()) {
        if (!(component instanceof BencodeString)) {
          throw new TorrentException("File path components must be strings");
        }
        pathComponents.add(((BencodeString) component).getValue());
      }

      // Parse file length
      BencodeValue lengthValue = fileDict.get("length");
      if (!(lengthValue instanceof BencodeInteger)) {
        throw new TorrentException("File length must be an integer");
      }
      long length = ((BencodeInteger) lengthValue).getValue();
      if (length < 0) {
        throw new TorrentException("File length cannot be negative: " + length);
      }

      files.add(new TorrentInfo.FileEntry(Collections.unmodifiableList(pathComponents), length));
    }

    return Collections.unmodifiableList(files);
  }

  /**
   * Calculates the SHA-1 info hash from the root torrent dictionary.
   *
   * @param rootDict the root bencode dictionary
   * @return the SHA-1 hash of the info dictionary
   * @throws NoSuchAlgorithmException if SHA-1 algorithm is not available
   * @throws TorrentException if the info dictionary is missing or invalid
   */
  private static byte[] calculateInfoHash(BencodeDict rootDict)
      throws NoSuchAlgorithmException, TorrentException {
    BencodeValue infoValue = rootDict.get("info");
    if (!(infoValue instanceof BencodeDict)) {
      throw new TorrentException("Missing info dictionary");
    }

    BencodeDict infoDict = (BencodeDict) infoValue;
    String encoded = infoDict.encode();
    byte[] encodedBytes = encoded.getBytes(StandardCharsets.UTF_8);

    MessageDigest md = MessageDigest.getInstance("SHA-1");
    return md.digest(encodedBytes);
  }
}
