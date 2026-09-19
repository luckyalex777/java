# JSE GitHub Copilot configuration

This repository contains a reusable GitHub Copilot configuration for a production Java 21+ Maven engineering workflow.

## Agents

- `jse-engineer` — orchestrator
- `jse-planner` — planning
- `jse-implementer` — implementation
- `jse-tester` — verification

## Native Copilot customization layout

```text
.github/
├── agents/
│   ├── jse-engineer.md
│   ├── jse-implementer.md
│   ├── jse-planner.md
│   └── jse-tester.md
├── instructions/
│   ├── java.instructions.md
│   └── pom.instructions.md
├── prompts/
│   ├── engineer.prompt.md
│   ├── implement.prompt.md
│   ├── plan.prompt.md
│   ├── review.prompt.md
│   └── test.prompt.md
├── skills/
│   ├── java-quality/SKILL.md
│   ├── java-testing/SKILL.md
│   └── maven-build/SKILL.md
├── jse/
│   ├── workflows/
│   │   ├── README.md
│   │   └── feature-delivery.md
│   └── work/
│       └── .gitkeep
├── workflows/
│   └── jse-quality.yml
└── copilot-instructions.md
```

## How to use

For a normal feature request, select `jse-engineer` and describe the desired result.

For explicit phase control:
- select `jse-planner` to create/update the plan;
- select `jse-implementer` after the plan is approved;
- select `jse-tester` to verify the implementation.

The reusable prompt files provide the same workflow through prompt-driven interactions.

## Important compatibility note

GitHub recognizes `.github/agents`, `.github/prompts`, `.github/skills`, `.github/instructions`, and `.github/copilot-instructions.md` for their respective customization features. The `.github/jse/workflows` directory in this package is a project convention for durable workflow documentation; it is not a native Copilot customization directory.

Custom-agent delegation behavior varies by Copilot surface. `jse-engineer.md` therefore defines the workflow so it can operate both with actual delegation/handoffs and as a single orchestrator following the same phase contracts.

## Recommended repository baseline

Use:
- Java 21+
- Maven Wrapper
- JUnit 5
- a formatter such as Spotless
- Checkstyle
- PMD
- SpotBugs
- JaCoCo
- Maven Enforcer
- Surefire/Failsafe as appropriate

The quality workflow should make `./mvnw -B clean verify` the primary build gate.
