---
applyTo: "**/pom.xml"
---

# Maven POM rules

- Java release must be 21 or higher.
- All Maven plugin versions must be explicit.
- Keep compiler, Surefire, Failsafe, formatter, Checkstyle, PMD, SpotBugs, and JaCoCo configuration centralized where possible.
- Prefer Maven Wrapper.
- Do not add dependencies merely because Copilot knows a library; justify each new dependency.
- Check for dependency scope correctness.
- Avoid duplicate dependencies and plugins.
- Preserve reproducible builds.
- Quality plugins should be bound to `verify` where practical so `./mvnw verify` is the main quality gate.
