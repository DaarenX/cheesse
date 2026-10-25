---
name: arc42-documentation-maintenance
description: Keep docs/arc42 in sync with Cheesse code. Use after changing REST endpoints, WebSocket messages, domain/engine code, persistence schema, UI screens/ScreenModels, or build/deployment config.
---

# arc42 Documentation Maintenance

## Code-to-chapter mapping

| Changed code | Chapters to check |
|---|---|
| `shared/.../api/GameRequests.kt`, `GameResponses.kt` | `03_context_and_scope`, `05_building_block_view` |
| `shared/.../api/GameMoveMessage.kt` (`ClientMessage`, `ServerMessage`) | `03_context_and_scope`, `08_cross_cutting_concepts` |
| `shared/.../domain/chess/` (`ChessEngineAdapter`, `DefaultChessEngineAdapter`) | `04_solution_strategy`, `05_building_block_view`, `06_runtime_view` |
| `server/.../api/game/` (`GameController`, `GameExceptionHandler`) | `03_context_and_scope`, `05_building_block_view`, `08_cross_cutting_concepts` |
| `server/.../websocket/game/GameSessionWebSocketHandler.kt` | `03_context_and_scope`, `05_building_block_view`, `06_runtime_view` |
| `server/.../domain/game/GameService.kt` | `05_building_block_view`, `06_runtime_view` |
| `server/.../persistence/game/`, `schema.sql` | `05_building_block_view`, `08_cross_cutting_concepts` |
| `composeApp/.../ui/`, `composeApp/.../data/` | `04_solution_strategy`, `05_building_block_view`, `08_cross_cutting_concepts` |
| `docker-compose.yml`, `*.gradle.kts`, `libs.versions.toml`, `.github/workflows/` | `02_architecture_constraints`, `07_deployment_view` |
| Architectural trade-offs | `09_architecture_decisions` (see `adr-management` skill), `11_risks_and_technical_debt` |

## Procedure

1. Compare the changed files (`git diff --stat`) against the table and update the matching chapters in `docs/arc42/`.
2. Update Mermaid diagrams if structure or flows changed: components in `05`, sequences in `06`, context/deployment in `03`/`07`. Quote labels containing parentheses or brackets (`Node["Name (Details)"]`) and avoid HTML in labels.
3. Never edit `cheesse-architecture-arc42.md` by hand. Regenerate it with `./gradlew arc42Assemble`; `./gradlew check` (and so CI) fails via `arc42Check` if it is stale.
