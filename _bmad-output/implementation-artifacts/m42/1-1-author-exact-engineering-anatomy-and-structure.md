---
story_key: 1-1-author-exact-engineering-anatomy-and-structure
epic: 1
story: 1
status: ready-for-dev
created: 2026-08-05
---

# Story 1.1: Author Exact Engineering Anatomy And Structure

Status: ready-for-dev

## Story

As a multidisciplinary engineer,
I want to author exact values, Entities, Functions, Ports, and structure context,
so that design intent has stable identity and domain-neutral meaning before drawing or vendor implementation.

## Acceptance Criteria

1. Quantity, Integer, Boolean, Text, Symbol, and Reference values lower to exact typed contracts. Rational conversion and comparison lose no precision; incompatible dimensions fail closed with exact expected/actual dimensions and source Provenance.
2. One Entity may own main and auxiliary Functions and exact Entity- or Function-owned Ports. Ownership, stable identity, direction, admitted Flow references, cardinality, and optional package-defined terminal/interface designation remain inspectable. Function ownership does not prevent later cross-Entity Relationship participation.
3. Functional, installation, and product/device Structure Assignments are independent from identity. Changing assignment preserves Entity, Concept, Function, Port, and semantic Relationship identity. `engineering-model` owns assignment/reference mechanics only.
4. Every active `EngineeringComponent` consumer migrates directly to the anatomy contracts. Superseded Component authority, tests, fixtures, docs, and examples touched by this story are deleted. No alias, adapter, fallback, or dual model remains. M42 and inherited M41 regression gates pass.

## Tasks / Subtasks

- [ ] Task 1: Install exact EngineeringValue contracts (AC: 1)
  - [ ] Add red tests for Quantity rational precision, dimensions, Integer, Boolean, Text, Symbol, and Reference.
  - [ ] Implement immutable domain-neutral values with deterministic equality/order and source Provenance.
  - [ ] Reject incompatible dimensions and malformed values without coercion or guessed units.
- [ ] Task 2: Install Entity, Function, and Port anatomy (AC: 2)
  - [ ] Add red source/compiler/model tests for stable Entity identity, Entity-owned Functions, exact Port ownership, direction, cardinality, Flow references, and optional package references.
  - [ ] Implement anatomy contracts without electrical constants, terminal enums, or built-in Flow meanings.
  - [ ] Preserve Function ownership while allowing later cross-Entity participation.
- [ ] Task 3: Install Structure Assignment authority (AC: 3)
  - [ ] Add tests proving functional, installation, and product/device assignments are independent and permutation-stable.
  - [ ] Implement assignment/reference mechanics in `engineering-model`; do not move Concept or knowledge facts into project reality.
- [ ] Task 4: Replace Component authority directly (AC: 4)
  - [ ] Use CodeGraph blast-radius and compiler failures to migrate active compiler, language, Projection, Spatial, runtime, plugin, LSP, CLI, and test consumers.
  - [ ] Delete `component-model` production/test/docs/examples and stale Component payloads/settings where no current responsibility remains.
  - [ ] Add executable absence scan for Component aliases, adapters, fallbacks, and copied DTOs. Keep closed M0-M41 artifacts immutable.
- [ ] Task 5: Verify and complete records
  - [ ] Run affected Gradle tests sequentially on Windows, then full repository tests.
  - [ ] Run frontend tests, source-set hygiene, encoding audit, active legacy scan, and `git diff --check`.
  - [ ] Fill Debug Log, Completion Notes, File List, Change Log; mark `review` only after all AC tests pass.

## Dev Notes

### Architecture guardrails

- Chain: source meaning -> Engineering facts -> Projection -> Spatial -> Presentation/Theia.
- `engineering-model` owns project Entity, Function, Port, typed value, Structure Assignment, stable identity, authored properties, and Provenance only.
- Knowledge definitions, capabilities, relationship rules, formulas, validation judgements, routing geometry, and paint remain outside this story.
- Kernel contracts stay domain-neutral: no electrical units, terminal names, built-in relationship verbs, or rule constants.
- ANTLR remains sole semantic parser. Tree-sitter is editor syntax/highlighting only.
- No backward compatibility. Delete obsolete authority instead of shims or migration adapters.
- Production `src/main` contains no proof/demo/sample/milestone architecture.

### Expected source areas

- `kernel/engineering-model`: new anatomy/value/assignment contracts and tests.
- `kernel/language`: entity/function/port/value/assignment syntax and spans.
- `kernel/compiler`: direct lowering and deterministic diagnostics.
- Active Projection/Spatial/runtime/plugin/LSP/CLI consumers: direct migration only.
- `settings.gradle.kts`: remove `:kernel:component-model` when no responsibility remains.
- `_bmad-output/implementation-artifacts/m42`: story evidence only.

### Testing requirements

- Assert literal IDs, values, dimensions, ownership, directions, cardinalities, assignments, Provenance, and exact diagnostics.
- Cover permutation identity, invalid dimensions, duplicate identity, missing references, and cross-domain neutrality.
- Do not use count-only, self-comparison, reflection-only, or source-text-only proof.
- Gradle verification is strictly sequential; never run two Gradle commands concurrently.

### References

- `_bmad-output/planning-artifacts/m42/epics.md` Epic 1 Story 1.1
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-04-m42/prd.md` FR-1, FR-2, FR-3, FR-6
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-04-m42/m42-engineering-knowledge-system-design.md`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-04-m42/ARCHITECTURE-SPINE.md` AD-20 through AD-22, AD-26, AD-35
- `AGENTS.md` source-set, encoding, pre-1.0, and BMad story rules

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log

- 2026-08-05: Snapshot of pre-rebuild dirty M42 tree stored on `backup/m42-pre-rebuild-2026-08-05` and external bundle. Main worktree reset to committed M42 planning baseline.
- 2026-08-05: BMad create-story context loaded from sprint status, Epic 1, M42 PRD, architecture spine, CodeGraph blast radius, and current source.

### Completion Notes

- Pending implementation.

### File List

- Pending implementation.

### Change Log

- 2026-08-05: Created through BMad story workflow for clean M42 rebuild.
