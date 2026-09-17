package com.alexswd.torrent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Immutable data class representing torrent metadata.
 *
 * <p>
 * This class contains all the information extracted from a torrent file, including name, file
 * information, piece details, and announce URLs.
 */
public final class TorrentInfo {

  private final String name;
  private final long length;
  private final long pieceLength;
  private final byte[] pieces;
  private final byte[] infoHash;
  private final List<FileEntry> files;
  private final String announceUrl;
  private final List<List<String>> announceList;
  private final Date creationDate;
  private final String comment;
  private final String createdBy;
  private final String encoding;

  /**
   * Private constructor used by the builder.
   */
  private TorrentInfo(Builder builder) {
    this.name = builder.name;
    this.length = builder.length;
    this.pieceLength = builder.pieceLength;
    this.pieces = builder.pieces != null ? builder.pieces.clone() : new byte[0];
    this.infoHash = builder.infoHash != null ? builder.infoHash.clone() : new byte[0];
    this.files = builder.files != null ? Collections.unmodifiableList(builder.files)
        : Collections.emptyList();
    this.announceUrl = builder.announceUrl;
    this.announceList =
        builder.announceList != null ? Collections.unmodifiableList(builder.announceList)
            : Collections.emptyList();
    this.creationDate =
        builder.creationDate != null ? new Date(builder.creationDate.getTime()) : null;
    this.comment = builder.comment;
    this.createdBy = builder.createdBy;
    this.encoding = builder.encoding;
  }

  /**
   * Gets the name of the torrent.
   *
   * @return the torrent name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the total length of the torrent in bytes.
   *
   * @return the total length
   */
  public long getLength() {
    return length;
  }

  /**
   * Gets the length of each piece in bytes.
   *
   * @return the piece length
   */
  public long getPieceLength() {
    return pieceLength;
  }

  /**
   * Gets the SHA-1 hashes of all pieces.
   *
   * <p>
   * The returned array is a copy and should not be modified.
   *
   * @return a copy of the pieces array
   */
  public byte[] getPieces() {
    return pieces.clone();
  }

  /**
   * Gets the info hash of the torrent.
   *
   * <p>
   * The returned array is a copy and should not be modified.
   *
   * @return a copy of the info hash
   */
  public byte[] getInfoHash() {
    return infoHash.clone();
  }

  /**
   * Gets the list of files in this torrent.
   *
   * @return an immutable list of file entries
   */
  public List<FileEntry> getFiles() {
    return files;
  }

  /**
   * Gets the announce URL for the tracker.
   *
   * @return the announce URL
   */
  public String getAnnounceUrl() {
    return announceUrl;
  }

  /**
   * Gets the list of announce URLs organized by tier.
   *
   * @return an immutable list of URL lists (tiers)
   */
  public List<List<String>> getAnnounceList() {
    return announceList;
  }

  /**
   * Gets the creation date of the torrent.
   *
   * @return the creation date, or null if not specified
   */
  public Date getCreationDate() {
    return creationDate != null ? new Date(creationDate.getTime()) : null;
  }

  /**
   * Gets the comment associated with the torrent.
   *
   * @return the comment, or null if not specified
   */
  public String getComment() {
    return comment;
  }

  /**
   * Gets the creator of the torrent.
   *
   * @return the creator name, or null if not specified
   */
  public String getCreatedBy() {
    return createdBy;
  }

  /**
   * Gets the encoding used for the torrent file.
   *
   * @return the encoding, or null if not specified
   */
  public String getEncoding() {
    return encoding;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TorrentInfo)) {
      return false;
    }
    TorrentInfo that = (TorrentInfo) o;
    return length == that.length && pieceLength == that.pieceLength
        && Objects.equals(name, that.name) && Arrays.equals(pieces, that.pieces)
        && Arrays.equals(infoHash, that.infoHash) && Objects.equals(files, that.files)
        && Objects.equals(announceUrl, that.announceUrl)
        && Objects.equals(announceList, that.announceList)
        && Objects.equals(creationDate, that.creationDate) && Objects.equals(comment, that.comment)
        && Objects.equals(createdBy, that.createdBy) && Objects.equals(encoding, that.encoding);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(name, length, pieceLength, announceUrl, announceList, creationDate,
        comment, createdBy, encoding);
    result = 31 * result + Arrays.hashCode(pieces);
    result = 31 * result + Arrays.hashCode(infoHash);
    return result;
  }

  @Override
  public String toString() {
    return "TorrentInfo{" + "name='" + name + '\'' + ", length=" + length + ", pieceLength="
        + pieceLength + ", announceUrl='" + announceUrl + '\'' + ", creationDate=" + creationDate
        + ", comment='" + comment + '\'' + ", createdBy='" + createdBy + '\'' + ", encoding='"
        + encoding + '\'' + '}';
  }

  /**
   * Creates a new builder for constructing TorrentInfo instances.
   *
   * @return a new Builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder class for constructing immutable TorrentInfo instances.
   */
  public static final class Builder {
    private String name;
    private long length;
    private long pieceLength;
    private byte[] pieces;
    private byte[] infoHash;
    private List<FileEntry> files;
    private String announceUrl;
    private List<List<String>> announceList;
    private Date creationDate;
    private String comment;
    private String createdBy;
    private String encoding;

    /**
     * Sets the name of the torrent.
     *
     * @param name the torrent name
     * @return this builder instance
     */
    public Builder name(String name) {
      this.name = name;
      return this;
    }

    /**
     * Sets the total length of the torrent.
     *
     * @param length the total length in bytes
     * @return this builder instance
     */
    public Builder length(long length) {
      this.length = length;
      return this;
    }

    /**
     * Sets the piece length.
     *
     * @param pieceLength the piece length in bytes
     * @return this builder instance
     */
    public Builder pieceLength(long pieceLength) {
      this.pieceLength = pieceLength;
      return this;
    }

    /**
     * Sets the pieces (SHA-1 hashes).
     *
     * @param pieces the pieces array
     * @return this builder instance
     */
    public Builder pieces(byte[] pieces) {
      this.pieces = pieces != null ? pieces.clone() : null;
      return this;
    }

    /**
     * Sets the info hash.
     *
     * @param infoHash the info hash array
     * @return this builder instance
     */
    public Builder infoHash(byte[] infoHash) {
      this.infoHash = infoHash != null ? infoHash.clone() : null;
      return this;
    }

    /**
     * Sets the list of files.
     *
     * @param files the list of file entries
     * @return this builder instance
     */
    public Builder files(List<FileEntry> files) {
      this.files = files != null ? new ArrayList<>(files) : null;
      return this;
    }

    /**
     * Sets the announce URL.
     *
     * @param announceUrl the tracker announce URL
     * @return this builder instance
     */
    public Builder announceUrl(String announceUrl) {
      this.announceUrl = announceUrl;
      return this;
    }

    /**
     * Sets the announce list (by tier).
     *
     * @param announceList the list of URL lists
     * @return this builder instance
     */
    public Builder announceList(List<List<String>> announceList) {
      this.announceList = announceList != null ? new ArrayList<>(announceList) : null;
      return this;
    }

    /**
     * Sets the creation date.
     *
     * @param creationDate the creation date
     * @return this builder instance
     */
    public Builder creationDate(Date creationDate) {
      this.creationDate = creationDate != null ? new Date(creationDate.getTime()) : null;
      return this;
    }

    /**
     * Sets the comment.
     *
     * @param comment the torrent comment
     * @return this builder instance
     */
    public Builder comment(String comment) {
      this.comment = comment;
      return this;
    }

    /**
     * Sets the creator information.
     *
     * @param createdBy the creator name
     * @return this builder instance
     */
    public Builder createdBy(String createdBy) {
      this.createdBy = createdBy;
      return this;
    }

    /**
     * Sets the encoding.
     *
     * @param encoding the encoding used
     * @return this builder instance
     */
    public Builder encoding(String encoding) {
      this.encoding = encoding;
      return this;
    }

    /**
     * Builds and returns the TorrentInfo instance.
     *
     * @return a new immutable TorrentInfo instance
     */
    public TorrentInfo build() {
      return new TorrentInfo(this);
    }
  }

  /**
   * Represents a single file entry in a torrent.
   */
  public static final class FileEntry {
    private final List<String> path;
    private final long length;

    /**
     * Constructs a FileEntry with the given path and length.
     *
     * @param path the file path components
     * @param length the file length in bytes
     */
    public FileEntry(List<String> path, long length) {
      this.path = Collections
          .unmodifiableList(new ArrayList<>(Objects.requireNonNull(path, "path cannot be null")));
      this.length = length;
    }

    /**
     * Gets the file path components.
     *
     * @return an immutable list of path components
     */
    public List<String> getPath() {
      return path;
    }

    /**
     * Gets the file length.
     *
     * @return the file length in bytes
     */
    public long getLength() {
      return length;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof FileEntry)) {
        return false;
      }
      FileEntry that = (FileEntry) o;
      return length == that.length && Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
      return Objects.hash(path, length);
    }

    @Override
    public String toString() {
      return "FileEntry{" + "path=" + path + ", length=" + length + '}';
    }
  }
}
