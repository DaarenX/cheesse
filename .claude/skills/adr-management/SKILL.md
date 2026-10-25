---
name: adr-management
description: Add or update an Architecture Decision Record in Cheesse. ADRs live in docs/arc42/09_architecture_decisions.md (not docs/adr/). Use when introducing a new technology, protocol, or architectural pattern, or replacing an existing ADR.
---

# ADR Management

This project keeps ADRs inside arc42 chapter 9: `docs/arc42/09_architecture_decisions.md`. There is no `docs/adr/` directory and none should be created.

## Procedure

1. Take the next number after the highest `## ADR-NNN` heading in the file.
2. Append an entry in this format:

```markdown
## ADR-NNN: [Short, Imperative Title]

- **Status**: Proposed | Accepted | Superseded by ADR-YYY | Rejected
- **Date**: YYYY-MM-DD
- **Context**: [The problem and constraints forcing a decision.]
- **Options Considered**:
  1. Option A: [pros/cons]
  2. Option B: [pros/cons]
- **Decision**: [The chosen option, in active voice.]
- **Consequences**:
  - *Positive*: [...]
  - *Negative*: [...]
```

3. When superseding, change the old ADR's status to `Superseded by ADR-NNN`; don't delete it.
4. Update related chapters where relevant: `04_solution_strategy.md`, `05_building_block_view.md`, `11_risks_and_technical_debt.md`, and the ADR summary row in `docs/arc42/index.md`.
5. Run `./gradlew arc42Assemble`.
