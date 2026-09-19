---
name: java-testing
description: Design and execute deterministic JUnit 5 tests for Java 21+ Maven applications, including unit, integration, regression, edge-case, and failure-path testing.
---

# Java testing workflow

- Prefer JUnit 5.
- Keep tests deterministic and isolated.
- Test behavior rather than implementation details.
- Cover happy paths, boundary conditions, invalid input, failures, and resource cleanup.
- For concurrency, test lifecycle and race-sensitive behavior without relying on arbitrary sleeps.
- For HTTP/network code, isolate external systems behind interfaces and use controlled test doubles unless an integration test is explicitly required.
- Do not weaken assertions to make a test pass.
- Keep test names descriptive.
- Run targeted tests before the full verification lifecycle.
- Record exact commands and results in `.github/jse/work/TEST-REPORT.md`.
