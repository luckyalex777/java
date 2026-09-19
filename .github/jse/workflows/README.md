# JSE workflow definitions

These Markdown workflow definitions describe the durable multi-agent process.

The native Copilot customization directories are:
- `.github/agents/` — custom agent profiles
- `.github/prompts/` — reusable prompt files
- `.github/skills/` — on-demand skills
- `.github/instructions/` — path-specific instructions
- `.github/copilot-instructions.md` — repository-wide rules

This `workflows` directory is intentionally a human-readable orchestration layer. It is not itself a special Copilot configuration directory.

## Workflow

```text
User request
    |
    v
jse-engineer
    |
    +--> jse-planner --> PLAN.md
    |
    +--> jse-implementer --> IMPLEMENTATION.md
    |
    +--> jse-tester --> TEST-REPORT.md
    |
    +--> defects? --yes--> implement --> test
    |
    v
Final verification
```
