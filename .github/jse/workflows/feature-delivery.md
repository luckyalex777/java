# Feature delivery workflow

## Inputs
- User requirement
- Existing repository
- Existing Maven build and quality configuration

## Stage 1: Plan
Owner: `jse-planner`

Output:
`.github/jse/work/PLAN.md`

Exit criteria:
- architecture understood
- impacted files identified
- acceptance criteria measurable
- test strategy defined
- risks identified

## Stage 2: Implement
Owner: `jse-implementer`

Output:
`.github/jse/work/IMPLEMENTATION.md`

Exit criteria:
- implementation complete
- tests added/updated
- formatter applied
- Maven verification attempted

## Stage 3: Test
Owner: `jse-tester`

Output:
`.github/jse/work/TEST-REPORT.md`

Exit criteria:
- acceptance criteria checked
- tests executed
- quality gates checked
- defects documented

## Stage 4: Iterate
If defects exist, return to Stage 2. If a design change is required, update Stage 1 artifacts before implementing the changed design.

## Stage 5: Complete
`jse-engineer` produces the final status from observed evidence only.
