# JSE Copilot Engineering Standards

This repository is developed as a production-grade Java application.

## Platform
- Use Java 21 as the minimum language/runtime baseline unless the project explicitly requires a newer Java release.
- Prefer modern Java language features and APIs available in the configured Java release.
- Use Maven for dependency management, builds, tests, packaging, and quality checks.
- Prefer standard JDK APIs and mature, well-maintained libraries.
- Do not introduce frameworks or dependencies without a concrete reason.

## Architecture and design
- Favor simple, cohesive, testable designs.
- Apply SOLID principles pragmatically.
- Prefer composition over inheritance.
- Keep public APIs small and intentional.
- Separate domain, application, infrastructure, and presentation concerns when the application size warrants it.
- Make concurrency explicit and safe.
- Avoid global mutable state.
- Avoid premature abstractions and speculative extension points.

## Code quality
- Code must be formatted automatically with the repository formatter.
- Checkstyle, PMD, and SpotBugs are quality gates.
- Treat compiler warnings and static-analysis findings as defects unless there is a documented justification.
- Do not suppress warnings merely to make a build pass.
- Prefer immutable data, records, sealed types, and final fields where appropriate.
- Validate external input at boundaries.
- Handle resources with try-with-resources.
- Use parameterized logging; avoid string concatenation in log calls.
- Do not log secrets, credentials, tokens, private keys, or sensitive payloads.

## Maven
- Keep Maven configuration deterministic and reproducible.
- Pin plugin versions.
- Use Maven Wrapper (`mvnw` / `mvnw.cmd`) for project builds.
- Keep dependency versions centralized where practical.
- Do not use dynamic dependency versions such as `LATEST` or `RELEASE`.
- Prefer dependency scopes that accurately express runtime/build/test requirements.

## Testing
- Every non-trivial production change requires tests.
- Prefer JUnit 5.
- Use unit tests for business logic and focused integration tests for boundaries.
- Tests must be deterministic and independent.
- Do not weaken tests just to make an implementation pass.
- Run the complete verification lifecycle before declaring a task complete.

## Agent collaboration
- Planner produces an implementation plan and acceptance criteria before implementation.
- Implementer changes production code and tests according to an approved plan.
- Tester validates behavior, quality gates, regressions, and edge cases.
- Engineer/orchestrator coordinates the phases and does not silently skip failed validation.
- Agents communicate through durable Markdown artifacts under `.github/jse/work/`.
- Never claim a command passed unless it was actually executed and its result observed.

## Definition of done
A change is complete only when:
1. The requested behavior is implemented.
2. Tests cover the relevant behavior.
3. Formatting passes.
4. Checkstyle passes.
5. PMD passes.
6. SpotBugs passes.
7. Maven verification succeeds, or any unavoidable failure is explicitly reported with the exact command and failure.
8. Documentation/configuration is updated when behavior or public APIs changed.
