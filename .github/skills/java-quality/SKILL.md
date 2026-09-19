---
name: java-quality
description: Apply and verify Java 21+ formatting, Checkstyle, PMD, SpotBugs, compiler, tests, and Maven quality gates. Use when implementing or reviewing Java code.
---

# Java quality workflow

1. Detect the project's configured Java release.
2. Detect the configured formatter and static-analysis plugins.
3. Never replace an existing project-standard formatter with another formatter without an explicit request.
4. Prefer the Maven Wrapper.
5. Run formatting check before final validation.
6. Run tests.
7. Run Maven `verify`.
8. Inspect Checkstyle, PMD, and SpotBugs findings.
9. Fix real findings instead of suppressing them.
10. If suppression is genuinely necessary, document:
   - rule
   - affected code
   - reason
   - why the suppression is safe

Typical commands:

```bash
./mvnw -B spotless:check
./mvnw -B test
./mvnw -B verify
```

If the project uses a different formatter, invoke its configured Maven goal.

## Completion rule

A quality claim must be backed by an executed command and observed result.
