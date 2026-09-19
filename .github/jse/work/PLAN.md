# PLAN: HTTP Client Console Application Demo

## Goal

Create a new console application in `uja/demo-apps/http-client-demo` that:
1. Connects to a website identified via command-line argument
2. Uses the `HttpClient` from `uja/network-library`
3. Follows the same structure, patterns, and quality standards as `torrent-parser-demo`
4. Targets Java 21+ (override parent's Java 25 to Java 21)
5. Uses SLF4J for logging
6. Handles errors gracefully with appropriate exit codes
7. Passes all quality gates: Checkstyle, PMD, SpotBugs, formatting, and Maven verify

## Current State

### Repository Structure
- Multi-module Maven project at `uja/` with parent POM
- Four modules: `bencode-library`, `torrent-parser`, `network-library`, `demo-apps`
- `demo-apps` is a parent POM containing `torrent-parser-demo` as its only child module
- Parent POM (root `uja/pom.xml`): Java 25, SLF4J 2.0.13, Log4j2 2.26.0, JUnit 5.10.2, Hamcrest 3.0, SpotBugs 4.9.7

### Existing Demo Application
- **torrent-parser-demo**:
  - Package: `com.alexswd.bencodedemo.TorrentParserDemo`
  - Structure: Main class with private utility methods for display logic
  - Error handling: Validation of file existence/readability, exception catching with error message + logger, System.exit(1)
  - Logging: SLF4J with static logger
  - Testing: JUnit 5 with placeholder test
  - Build: Assembly plugin for fat JAR (`jar-with-dependencies`), JAR plugin for regular JAR
  - Java version: Explicitly targets Java 11 (overrides parent Java 25)
  - Javadoc: Comprehensive class, method, and parameter documentation
  - Code style: Follows Google Java Style (checkstyle-checker.xml in project)

### Network Library
- **HttpClient** (`com.alexswd.network.http.HttpClient`):
  - Constructor: No-arg or takes `ISocket` parameter
  - Method: `get(String url)` throws `IOException`
  - Returns: `HttpResponse` object with `getStatus()` and `getBody()` methods
  - Currently used only by `network-library` tests (if any)

### Quality Gates & Configuration
- Parent POM configures: Checkstyle, PMD, SpotBugs, formatter
- Build plugins inherited by child modules
- All compiler warnings treated as defects unless suppressed with `@SuppressFBWarnings` or similar
- Tests required for non-trivial functionality
- Maven verify must pass before declaring work complete

## Proposed Design

### Module: `http-client-demo`

**Artifact**: `com.alexswd:http-client-demo:jar:1.0-SNAPSHOT`

**Package Structure**:
```
com.alexswd.httpclientdemo
├── HttpClientDemo.java (main class)
├── WebsiteDataFormatter.java (display formatting utility, similar to TorrentDataFormatter)
```

**Main Class: HttpClientDemo**
- Entry point for console application
- Validates command-line arguments
- Creates `HttpClient` instance from `network-library`
- Invokes HTTP GET request to the provided URL
- Handles and displays HTTP response (status code, response size, sample content)
- Logs key events and errors
- Exit codes:
  - `0`: Success
  - `1`: Invalid arguments or HTTP error (4xx/5xx)
  - `1`: Exception during HTTP request (IOException, MalformedURLException)

**Main Class Logic**:
1. Check args.length == 1; print usage and exit(1) if not
2. Validate URL format (can attempt to create new URL() to detect malformed URLs)
3. Create `HttpClient` (no-arg constructor)
4. Invoke `httpClient.get(url)` wrapped in try-catch for IOException
5. Check response status: log warning if 4xx/5xx, log success if 2xx/3xx
6. Display response details: status, body size, sample of body content (first 200 chars)
7. Exit with code 0 on success, 1 on any error
8. Log all significant operations at INFO level; errors at ERROR level

**Helper Class: WebsiteDataFormatter**
- Static methods similar to `TorrentDataFormatter`
- Methods: `printSeparator()`, `printHeader(String)`, `formatStatusLine(int)`, `formatBodyPreview(byte[])`
- Provides clean, consistent console output formatting

**Dependencies**:
- Internal: `network-library` (1.0-SNAPSHOT)
- Logging: SLF4J API 2.0.13 (from parent dependencyManagement), SLF4J Simple 2.0.13
- Annotations: SpotBugs annotations 4.9.7 (optional)
- Testing: JUnit 5.10.2, Hamcrest 3.0

**Logging Configuration**:
- Use SLF4J Simple as in torrent-parser-demo (provides default console output)
- Static logger per class: `private static final Logger logger = LoggerFactory.getLogger(ClassName.class);`
- Log at INFO level for key events, ERROR for exceptions
- No logging of sensitive data (URLs beyond host, request/response payloads)

**Build Configuration**:
- Assembly plugin: Create fat JAR with `jar-with-dependencies` descriptor
- JAR plugin: Regular JAR with manifest
- Manifest main-class: `com.alexswd.httpclientdemo.HttpClientDemo`
- Target Java 21 (parent property: maven.compiler.source=21, maven.compiler.target=21)

**Testing**:
- `HttpClientDemoTest.java`: Minimal placeholder test (like torrent-parser-demo)
- Can be expanded later for integration testing (e.g., mock HttpClient with mock HttpResponse)
- Test class in `com.alexswd.httpclientdemo` package

## Files to Create

1. **uja/demo-apps/http-client-demo/pom.xml**
   - Parent: `demo-apps` (1.0-SNAPSHOT)
   - Artifact: `http-client-demo`
   - Java version: 21
   - Dependencies: `network-library`, SLF4J, SpotBugs annotations, JUnit 5
   - Build plugins: Assembly, JAR

2. **uja/demo-apps/http-client-demo/src/main/java/com/alexswd/httpclientdemo/HttpClientDemo.java**
   - Main entry point
   - ~100 lines

3. **uja/demo-apps/http-client-demo/src/main/java/com/alexswd/httpclientdemo/WebsiteDataFormatter.java**
   - Display formatting helper
   - ~50 lines

4. **uja/demo-apps/http-client-demo/src/test/java/com/alexswd/httpclientdemo/HttpClientDemoTest.java**
   - Placeholder test
   - ~10 lines

5. **uja/demo-apps/http-client-demo/eclipse-java-google-style.xml**
   - Copy from existing module (used by Checkstyle formatter)

## Files to Modify

1. **uja/demo-apps/pom.xml**
   - Add `<module>http-client-demo</module>` to the `<modules>` section

## Files to Delete

None.

## Data/API Changes

None. No existing APIs or data structures are modified.

## Dependencies

### New Internal Dependencies
- `network-library:1.0-SNAPSHOT` (transitive to HTTP client)

### New External Dependencies (from parent dependencyManagement)
- `org.slf4j:slf4j-api:2.0.13`
- `org.slf4j:slf4j-simple:2.0.13`
- `com.github.spotbugs:spotbugs-annotations:4.9.7`
- `org.junit.jupiter:junit-jupiter:5.10.2` (scope: test)
- `org.hamcrest:hamcrest:3.0` (scope: test)

All versions are already defined in parent POM; no new version properties needed.

## Implementation Steps

### Phase 1: Scaffold Module
1. Create directory `uja/demo-apps/http-client-demo`
2. Create `pom.xml` with parent reference, artifact config, dependencies, build plugins
3. Create Java package directories: `src/main/java/com/alexswd/httpclientdemo`, `src/test/java/com/alexswd/httpclientdemo`
4. Copy `eclipse-java-google-style.xml` from torrent-parser-demo
5. Add module reference to `uja/demo-apps/pom.xml`

### Phase 2: Implement Main Class
1. Create `HttpClientDemo.java` with main method
2. Implement argument validation (1 argument required)
3. Implement URL parsing and validation
4. Implement HTTP client initialization and GET request
5. Implement response handling (status check, body display)
6. Implement error handling (IOException, MalformedURLException) with appropriate exit codes
7. Add comprehensive Javadoc

### Phase 3: Implement Helper Class
1. Create `WebsiteDataFormatter.java` with static formatting methods
2. Implement `printSeparator()` for visual output separation
3. Implement `printHeader(String)` for section headers
4. Implement `formatStatusLine(int)` to format HTTP status
5. Implement `formatBodyPreview(byte[])` to show first N characters of response body

### Phase 4: Create Tests
1. Create `HttpClientDemoTest.java` with placeholder test
2. Verify test file structure matches package layout

### Phase 5: Quality Gates
1. Run `mvn clean verify` from `uja/` to:
   - Compile all modules
   - Run formatters
   - Run Checkstyle checks
   - Run PMD analysis
   - Run SpotBugs
   - Run unit tests
   - Package JARs
2. Fix any violations
3. Verify clean compile with no warnings

## Test Strategy

### Unit Tests
- **HttpClientDemoTest.java**: Placeholder test (minimal scope)
  - Can be expanded in future with:
    - Mock HttpClient tests (behavior verification)
    - Response parsing tests (e.g., different status codes)
    - Argument parsing tests (valid/invalid URLs)
  - For now: single @Test method that passes

### Integration Tests
- Manual testing (not automated in this plan):
  - Run against real website: `java -jar http-client-demo-jar-with-dependencies.jar https://www.example.com`
  - Verify output displays status 200, response size, body preview
  - Test error cases: missing argument, malformed URL, connection refused

### Quality Gate Tests
- Maven verify includes:
  - Checkstyle formatting checks
  - PMD code analysis
  - SpotBugs byte-code analysis
  - Compiler warnings (treated as errors unless suppressed)

## Quality Gates

1. **Formatting**: Automated formatter must pass (enforced by parent POM `maven-fmt-plugin` or similar)
2. **Checkstyle**: Google Java Style compliance (google_checks.xml)
   - All violations must be fixed or explicitly suppressed with `@SuppressWarnings`
3. **PMD**: Code smell detection
   - No violations in new code
   - Parent POM configures PMD plugin
4. **SpotBugs**: Byte-code bug detection
   - No violations in new code
   - Use `@SuppressFBWarnings` for unavoidable issues with justification
5. **Compiler**: Zero warnings as defects
   - No unchecked casts, raw types, deprecation warnings, etc.
6. **Tests**: Must pass without modification
   - New code must not break existing tests
   - New test placeholder must compile and execute
7. **Maven Verify**: Complete build pipeline must succeed
   - `mvn clean verify` from `uja/` must exit with code 0

## Security Considerations

1. **URL Validation**: Validate URL format before passing to HttpClient to prevent malformed input
2. **Response Handling**: Display only limited preview of response body (first 200 chars) to avoid memory issues with large responses
3. **Logging**: Do not log full URLs or response bodies to prevent exposure of sensitive data
4. **Exception Handling**: Catch all IOExceptions generically; don't expose internal stack traces to console (log only the error message type)

## Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|-----------|
| HttpClient API incompatibility | Low | High | Review HttpClient.get() signature and test with existing network-library tests |
| Network timeouts during manual testing | Medium | Low | Document that manual tests require internet connectivity; use public URLs (example.com) |
| Quality gate failures (Checkstyle/PMD/SpotBugs) | Medium | Medium | Follow existing code patterns from torrent-parser-demo; run verify early and often |
| Java 21 compatibility issues | Low | Medium | Parent module uses Java 25; explicitly override to Java 21 and test compile |
| Unused imports or code inspection warnings | Medium | Low | Use IDE cleanup and formatter before submission; verify `mvn clean verify` passes |

## Acceptance Criteria

All acceptance criteria must be verified by running `mvn clean verify` from the `uja/` directory.

1. ✓ **Module Created**: `uja/demo-apps/http-client-demo` directory exists with proper Maven structure
2. ✓ **POM Structure**: `pom.xml` references parent `demo-apps`, configures dependencies, and build plugins correctly
3. ✓ **Main Class Exists**: `HttpClientDemo.java` exists at `com.alexswd.httpclientdemo.HttpClientDemo`
4. ✓ **Command-Line Arguments**: Main method validates single argument; prints usage and exits with code 1 if missing
5. ✓ **HTTP Connection**: Successfully creates HttpClient and invokes `get(url)` for valid URLs
6. ✓ **Response Display**: Displays HTTP status code and response body preview to console
7. ✓ **Error Handling**: Exits with code 1 on MalformedURLException, IOException, or HTTP error status
8. ✓ **Logging**: All operations logged via SLF4J at appropriate levels (INFO, ERROR)
9. ✓ **Formatting**: Code passes automated formatter checks (zero violations)
10. ✓ **Checkstyle**: Code passes Checkstyle checks (Google Java Style) with zero violations
11. ✓ **PMD**: Code passes PMD analysis with zero violations
12. ✓ **SpotBugs**: Code passes SpotBugs analysis with zero violations
13. ✓ **Compiler**: Zero warnings during `mvn clean verify`
14. ✓ **Tests**: Test placeholder compiles and executes without failure
15. ✓ **Maven Verify**: `mvn clean verify` from `uja/` exits with code 0 (all modules build successfully)
16. ✓ **Module Registration**: `uja/demo-apps/pom.xml` lists `http-client-demo` in `<modules>`
17. ✓ **Javadoc**: All public classes and methods include comprehensive Javadoc comments
18. ✓ **JAR Packaging**: Fat JAR and regular JAR both created with correct manifest main-class

## Open Questions

1. **HttpClient Socket Initialization**: Should the demo instantiate HttpClient with no-arg constructor (which creates default socket) or with explicit socket? 
   - *Resolution*: Use no-arg constructor for simplicity; if HttpClient(ISocket) is needed for testing, that can be addressed in Phase 4.

2. **Response Body Display Limit**: What is an appropriate limit for displaying response body preview (first N characters)?
   - *Resolution*: Use 200 characters as default; sufficient to show sample content without overwhelming output.

3. **HTTP Error Handling**: Should 3xx redirects be treated as success or error?
   - *Resolution*: Treat 2xx as success, 3xx as handled redirect (not error), 4xx/5xx as error. Exit code 0 for 2xx/3xx, exit code 1 for 4xx/5xx.

4. **Java Version Override**: Should we target Java 21 (between parent 25 and torrent-parser-demo 11) or align with one of the existing modules?
   - *Resolution*: Target Java 21 as per requirements. This is modern Java with stable features and better than the existing Java 11 in torrent-parser-demo.

5. **Log Output Format**: Should we use default SLF4J Simple output or provide custom log4j2 configuration?
   - *Resolution*: Use default SLF4J Simple configuration (as in torrent-parser-demo) for consistency and simplicity.

---

## IMPLEMENTER HANDOFF

**Module to Create**: `uja/demo-apps/http-client-demo`  
**Main Class**: `com.alexswd.httpclientdemo.HttpClientDemo`  
**Supporting Class**: `com.alexswd.httpclientdemo.WebsiteDataFormatter`  

### Exact Sequence for Implementation

1. **Create Module Structure**
   ```
   mkdir -p uja/demo-apps/http-client-demo/src/main/java/com/alexswd/httpclientdemo
   mkdir -p uja/demo-apps/http-client-demo/src/test/java/com/alexswd/httpclientdemo
   cp uja/demo-apps/torrent-parser-demo/eclipse-java-google-style.xml uja/demo-apps/http-client-demo/
   ```

2. **Create pom.xml** for http-client-demo
   - Parent: `demo-apps` (1.0-SNAPSHOT)
   - Artifact: `http-client-demo`
   - Properties: `maven.compiler.source=21`, `maven.compiler.target=21`, `main.class=com.alexswd.httpclientdemo.HttpClientDemo`
   - Dependencies: `network-library` (internal), `slf4j-api`, `slf4j-simple`, `spotbugs-annotations`, `junit-jupiter`, `hamcrest`
   - Build plugins: `maven-assembly-plugin`, `maven-jar-plugin` (inherit compiler and other plugins from parent)
   - Copy exact structure from torrent-parser-demo/pom.xml with adjusted module name and main class

3. **Register Module** in uja/demo-apps/pom.xml
   - Add `<module>http-client-demo</module>` after torrent-parser-demo

4. **Create WebsiteDataFormatter.java**
   - Static utility class
   - Methods: `printSeparator()`, `printHeader(String)`, `formatStatusLine(int)`, `formatBodyPreview(byte[])`
   - Reference: Study TorrentDataFormatter from torrent-parser-demo for style

5. **Create HttpClientDemo.java** (Main Class)
   - Package: `com.alexswd.httpclientdemo`
   - Private constructor (utility class pattern)
   - Public static void main(String[] args)
   - Flow:
     a. Validate args.length == 1; else print usage and System.exit(1)
     b. Catch MalformedURLException, validate URL
     c. Create HttpClient instance (no-arg)
     d. Invoke httpClient.get(url) in try-catch IOException
     e. Check response.getStatus(); log INFO if 2xx/3xx, log WARN if 4xx/5xx
     f. Display status, body size, body preview using WebsiteDataFormatter
     g. System.exit(0) on success, System.exit(1) on any error
   - Logging: Static Logger, log key events at INFO, exceptions at ERROR
   - Javadoc: Comprehensive for class and main method

6. **Create HttpClientDemoTest.java**
   - Package: `com.alexswd.httpclientdemo`
   - Single placeholder @Test method that passes
   - Comment: "Placeholder test for future enhancements"

7. **Verify Quality Gates** - Run from `uja/` directory
   ```
   mvn clean verify
   ```
   - Must exit with code 0
   - All modules compile without warnings
   - No Checkstyle, PMD, or SpotBugs violations
   - All tests pass
   - Fat JAR created at `uja/demo-apps/http-client-demo/target/http-client-demo-jar-with-dependencies.jar`

8. **Manual Smoke Test** (Optional but recommended)
   ```
   cd uja/demo-apps/http-client-demo
   mvn clean package
   java -jar target/http-client-demo-jar-with-dependencies.jar https://www.example.com
   ```
   - Expected: Status 200, HTML response preview displayed

