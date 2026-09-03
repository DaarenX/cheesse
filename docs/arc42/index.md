# Cheesse Architecture Documentation (arc42)

Welcome to the architectural documentation for **Cheesse**, an open-source cross-platform multiplayer chess application built with Kotlin Multiplatform, Compose Multiplatform, Spring Boot (WebFlux + R2DBC), and PostgreSQL.

This documentation follows the standardized [arc42 template](https://arc42.org/) to communicate, structure, and maintain architectural decisions, system decomposition, runtime behavior, and cross-cutting concepts.

---

## Document Navigation & Table of Contents

| Section | Title | Summary |
|---|---|---|
| [01. Introduction and Goals](01_introduction_and_goals.md) | Introduction, Requirements & Quality Goals | Core functional requirements, quality objectives (low-latency, multiplatform consistency, safety), and stakeholders. |
| [02. Architecture Constraints](02_architecture_constraints.md) | Technical & Organizational Constraints | Technical stack requirements, runtime environments, JVM toolchain, multiplatform targets, coding conventions. |
| [03. Context and Scope](03_context_and_scope.md) | Context & System Boundaries | Business and technical context, external communication protocols (HTTP REST, WebSockets, R2DBC). |
| [04. Solution Strategy](04_solution_strategy.md) | Fundamental Concepts & Approaches | Cross-platform code sharing (KMP), reactive non-blocking concurrency, server-authoritative move validation, MVI UI state. |
| [05. Building Block View](05_building_block_view.md) | Static Component Structure | Whitebox Level 1 (Overall system), Level 2 (`:shared`, `:server`, `:composeApp`), Level 3 (`GameService`, `ChessEngineAdapter`). |
| [06. Runtime View](06_runtime_view.md) | Dynamic Scenarios & Workflows | Matchmaking, game initialization, WebSocket session handshakes, move execution & board broadcast, offline pass-and-play. |
| [07. Deployment View](07_deployment_view.md) | Infrastructure & Distribution | Docker Compose setup, PostgreSQL container, backend execution, client packaging (JVM Uber JAR, Web Wasm, Android APK, iOS). |
| [08. Cross-Cutting Concepts](08_cross_cutting_concepts.md) | Shared Technical Concepts | Polymorphic JSON serialization, reactive coroutines & flow pipelines, anonymous token security, global exception handling, MVI UI. |
| [09. Architecture Decisions](09_architecture_decisions.md) | Architecture Decision Records (ADRs) | ADR-001 through ADR-007 documenting KMP, WebFlux, WebSockets, ChessEngineAdapter, Token auth, Voyager, and move-history-based game state. |
| [10. Quality Requirements](10_quality_requirements.md) | Quality Scenarios & Quality Tree | Detailed ISO/IEC 25010 quality tree with concrete stimulus-response quality scenarios. |
| [11. Risks and Technical Debt](11_risks_and_technical_debt.md) | Risks & Open Technical Debt | In-memory session tracking, full history replay cost, match reconnection, abandoned game session cleanup. |
| [12. Glossary](12_glossary.md) | Terms & Definitions | Domain (FEN, PGN, UCI, Join Token, Player Token) and technical concepts (R2DBC, WebFlux, Voyager ScreenModel, Ktor). |

---

## Complete Single-Document Reference

For offline reading or end-to-end continuous review, the complete documentation is also available as a single unified document:
- [Unified arc42 Architecture Document](cheesse-architecture-arc42.md)

---

## Architectural Maintenance

To maintain this documentation as the codebase evolves, refer to the agent skills:
- [`arc42-documentation-maintenance`](../../.claude/skills/arc42-documentation-maintenance/SKILL.md)
- [`adr-management`](../../.claude/skills/adr-management/SKILL.md)

The unified document is generated from the chapter files with `./gradlew arc42Assemble`; `./gradlew check` fails if it is stale.
