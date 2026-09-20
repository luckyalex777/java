package com.alexswd.torrent.tracker;

import com.alexswd.torrent.bencode.Decoder;
import com.alexswd.torrent.network.http.HttpClient;
import com.alexswd.torrent.network.http.HttpRequest;
import com.alexswd.torrent.network.socket.ISocket;
import com.alexswd.torrent.network.socket.ProxySocket;
import com.alexswd.torrent.parser.TorrentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

// import com.alexswd.torrent.network.socket.FlatSocket;

public final class TrackerAgent {

  private final static Logger logger = LoggerFactory.getLogger(TrackerAgent.class);

  private final String announce;
  private final long totalLength;
  private final String infoHash;
  private final String filePath;
  private final String logMessagePrefix;
  private String trackerId;
  private boolean firstRequest;

  public static TrackerAgent create(TorrentInfo torrentInfo) {
    return new TrackerAgent(torrentInfo.getFilePath(), torrentInfo.getAnnounce(),
        torrentInfo.getTotalLength(), torrentInfo.getInfoHash());
  }

  private TrackerAgent(String filePath, String announce, long totalLength, String infoHash) {
    this.announce = announce;
    this.totalLength = totalLength;
    this.infoHash = infoHash;
    this.filePath = filePath;
    this.logMessagePrefix = getLogMessagePrefix();
    this.trackerId = null;
    this.firstRequest = true;
  }

  public List<Peer> loadPeers() {
    ISocket proxySocket = null;
    try {
      proxySocket = new ProxySocket("localhost", 9050);
      // proxySocket = new FlatSocket();
      var httpClient = new HttpClient(proxySocket);
      // Set timeouts
      httpClient.setConnectionTimeout(10000);
      httpClient.setReadTimeout(10000);
      var announceRequest = buildRequest();
      var response = httpClient.execute(announceRequest);
      if (response.isSuccess()) {
        var responseBody = response.getBody();
        if (logger.isDebugEnabled()) {
          logger.debug("Tracker response body: '{}'", responseBody);
          logger.debug("Tracker response body as string: '{}'",
              new String(HexFormat.of().parseHex(responseBody), StandardCharsets.UTF_8));
        }
        return parseTrackerResponse(responseBody);
      } else {
        if (logger.isErrorEnabled()) {
          logger.error("HttpClient failure: {}", response.getStatusMessage());
        }
        var responseBody = response.getBody();
        if (responseBody != null) {
          if (logger.isErrorEnabled()) {
            logger.error("HttpClient failure: response body: '{}'", responseBody);
            logger.error("Tracker response body as string: '{}'",
                new String(HexFormat.of().parseHex(responseBody), StandardCharsets.UTF_8));
          }
        }
      }
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("TrackerAgent:loadPeers failure", e);
      }
    } finally {
      if (proxySocket != null) {
        try {
          proxySocket.close();
        } catch (IOException e) {
          if (logger.isWarnEnabled()) {
            logger.warn("Socket closing failure", e);
          }
        }
      }
    }

    // return an empty list
    return new ArrayList<>();
  }

  private HttpRequest buildRequest() throws IOException {
    var parameterMap = new HashMap<String, String>();
    parameterMap.put("info_hash", urlEncode(HexFormat.of().parseHex(this.infoHash)));
    parameterMap.put("peer_id", generatePeerId());
    parameterMap.put("port", "6881");
    parameterMap.put("uploaded", "0");
    parameterMap.put("downloaded", "0");
    parameterMap.put("left", String.format("%d", totalLength));
    parameterMap.put("compact", "1");
    if (firstRequest) {
      parameterMap.put("event", "started");
    }
    // parameterMap.put("numwant", "100");
    if (this.trackerId != null) {
      parameterMap.put("trackerid", this.trackerId);
    }

    var url = URI.create(announce).toURL();
    int port = url.getPort();
    if (port < 1 || port > 65535) {
      port = url.getDefaultPort();
      if (logger.isDebugEnabled()) {
        logger.debug("Port configured as default port for URL: port={}", port);
      }
    }
    var headerMap = new HashMap<String, String>();
    headerMap.put("User-Agent", "Transmission/4.1.1");
    headerMap.put("Accept", "*/*");
    headerMap.put("Connection", "close");
    var request = HttpRequest.builder().host(url.getHost()).port(port).path(url.getPath())
        .addParameters(parameterMap).addHeaders(headerMap).build();
    if (logger.isDebugEnabled()) {
      logger.debug(request.toString());
    }
    return request;
  }

  /**
   * URL encode byte array
   */
  private static String urlEncode(byte[] data) throws UnsupportedEncodingException {
    StringBuilder result = new StringBuilder();
    for (byte b : data) {
      if (b >= 'a' && b <= 'z' || b >= 'A' && b <= 'Z' || b >= '0' && b <= '9' || b == '-'
          || b == '_' || b == '.' || b == '~') {
        result.append((char) b);
      } else {
        result.append(String.format("%%%02X", b & 0xFF));
      }
    }
    return result.toString();
  }


  private static String generatePeerId() {
    // mimic to transmission 4.1.1
    // -TR411Z-
    byte[] peerIdBytes = new byte[20];
    peerIdBytes[0] = 0x2D;
    peerIdBytes[1] = 0x54;
    peerIdBytes[2] = 0x52;
    peerIdBytes[3] = 0x34;
    peerIdBytes[4] = 0x31;
    peerIdBytes[5] = 0x31;
    peerIdBytes[6] = 0x5A;
    peerIdBytes[7] = 0x2D;
    for (int i = 8; i < 20; ++i) {
      int n = ThreadLocalRandom.current().nextInt(100) % 10 + 0x30;
      peerIdBytes[i] = (byte) (n & 0x3F);
    }
    return new String(peerIdBytes, StandardCharsets.UTF_8);
  }

  /**
   * Parse tracker response from bencoded data
   */
  private List<Peer> parseTrackerResponse(String responseBody) throws IOException {

    // int interval = 0;
    // int minInterval = 0;
    // String warningMessage = "";
    // int seeders = 0;
    // int leechers = 0;
    List<Peer> peers = new ArrayList<>();
    // String externalIp;

    var responseMap = parseAndValidate(responseBody);

    // // Parse interval
    // if (responseMap.containsKey("interval")) {
    // interval = ((Long) responseMap.get("interval")).intValue();
    // }
    //
    // // Parse min interval
    // if (responseMap.containsKey("min interval")) {
    // minInterval = ((Long) responseMap.get("interval")).intValue();
    // }

    // Parse tracker id
    if (responseMap.containsKey("tracker id")) {
      this.trackerId = (String) responseMap.get("tracker id");
    }

    // // Parse warning message
    // if (responseMap.containsKey("warning message")) {
    // warningMessage = (String) responseMap.get("warning message");
    // }

    // // Parse warning message
    // if (responseMap.containsKey("external ip")) {
    // externalIp = (String) responseMap.get("external ip");
    // }

    // Parse peers
    if (responseMap.containsKey("peers")) {
      Object peersObj = responseMap.get("peers");

      if (peersObj instanceof byte[] peersData) {
        // Compact format - each peer is 6 bytes (4 for IP, 2 for port)
        peers = parseCompactPeers(peersData);
      } else if (peersObj instanceof List) {
        // Dictionary format
        List<Map<String, Object>> peerList = (List<Map<String, Object>>) peersObj;
        peers = parseDictionaryPeers(peerList);
      }
    }
    return peers;
  }

  private Map<String, Object> parseAndValidate(String responseBody) throws IOException {
    // Http client returns response as hex string
    var bytes = HexFormat.of().parseHex(responseBody);
    var decoded = Decoder.bdecode(bytes);
    if (!(decoded instanceof Map<?, ?> map)) {
      if (logger.isDebugEnabled()) {
        logger.debug("{}: Decoding failed: {}", logMessagePrefix, responseBody);
      }
      throw new IOException("Invalid tracker response (decoded is not map)");
    }
    var responseMap = safeCast(map);

    // Check for failure reason
    if (responseMap.containsKey("failure reason")) {
      String failureReason =
          new String((byte[]) responseMap.get("failure reason"), StandardCharsets.UTF_8);
      throw new IOException("Tracker failure: " + failureReason);
    }
    return responseMap;
  }

  private static Map<String, Object> safeCast(Map<?, ?> map) {
    Map<String, Object> result = new HashMap<>();
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      if (entry.getKey() instanceof String && entry.getValue() != null) {
        result.put((String) entry.getKey(), entry.getValue());
      } else {
        throw new ClassCastException("Invalid types in map");
      }
    }
    return result;
  }

  /**
   * Parse compact peer list format
   */
  private static List<Peer> parseCompactPeers(byte[] peersData) {
    List<Peer> peers = new ArrayList<>();

    for (int i = 0; i < peersData.length; i += 6) {
      if (i + 6 <= peersData.length) {
        // Parse IP address (4 bytes)
        String ip = String.format("%d.%d.%d.%d", peersData[i] & 0xFF, peersData[i + 1] & 0xFF,
            peersData[i + 2] & 0xFF, peersData[i + 3] & 0xFF);

        // Parse port (2 bytes, big-endian)
        int port = ((peersData[i + 4] & 0xFF) << 8) | (peersData[i + 5] & 0xFF);

        peers.add(new Peer(ip, port));
      }
    }

    return peers;
  }

  /**
   * Parse dictionary format peer list
   */
  private static List<Peer> parseDictionaryPeers(List<Map<String, Object>> peerList) {
    List<Peer> peers = new ArrayList<>();

    for (Map<String, Object> peerDict : peerList) {
      String ip = new String((byte[]) peerDict.get("ip"), StandardCharsets.UTF_8);
      int port = ((Long) peerDict.get("port")).intValue();
      peers.add(new Peer(ip, port));
    }

    return peers;
  }

  private String getLogMessagePrefix() {
    var fileName = new File(filePath).getName();
    var lastDotPos = fileName.lastIndexOf('.');
    if (lastDotPos != -1) {
      return fileName.substring(0, lastDotPos);
    } else {
      return fileName;
    }
  }
}
