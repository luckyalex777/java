---
applyTo: "**/*.java"
---

# Java source rules

- Use Java 21+ syntax, APIs, and idioms.
- Prefer `final` for fields, parameters, and local variables when it improves clarity.
- Prefer records for immutable data carriers.
- Prefer `Optional` only for return values where it improves the API; do not use it for fields or parameters by default.
- Do not catch `Exception`, `Throwable`, or broad runtime exceptions unless there is a specific recovery boundary.
- Preserve exception causes when translating exceptions.
- Never silently swallow exceptions.
- Use try-with-resources for `AutoCloseable` resources.
- Avoid `null` when a meaningful type or empty collection is available.
- Use `java.time` instead of legacy date/time APIs.
- Use `java.net.http.HttpClient` for HTTP unless the project already standardizes on another client.
- Prefer `List`, `Set`, `Map`, and interfaces at API boundaries.
- Keep methods focused; extract logic when complexity becomes difficult to reason about.
- Follow the configured formatter rather than manually aligning whitespace.
