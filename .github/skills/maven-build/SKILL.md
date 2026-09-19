---
name: maven-build
description: Maintain and validate reproducible Maven builds for Java 21+ projects, including plugin configuration, dependency hygiene, tests, packaging, and verify.
---

# Maven build workflow

- Use Maven Wrapper when present.
- Pin plugin versions.
- Keep dependency versions explicit and maintainable.
- Use `maven-enforcer-plugin` when the project standard requires it.
- Use compiler release 21 or higher.
- Keep Surefire and Failsafe configuration explicit.
- Bind quality checks to lifecycle phases when practical.
- Avoid build steps that depend on developer-local state.
- Do not commit generated `target/` output.

Baseline validation:

```bash
./mvnw -B clean verify
```
