---
baseline_commit: c00b416c463d3e18876dced3be6b750d2f0652ff
---

# Story 1.2: Express Typed Relationships And Independent Flows

Status: in-progress

## Story

As a systems engineer,
I want semantic Relationships with named roles and separate Flow meaning,
so that controls, protects, supplies, and drives intent is unambiguous across engineering domains.

## Acceptance Criteria

1. Relationship definitions admit Entity, Function, or exact Port participants. Source role bindings
   compile to stable EngineeringRelationship identity, package-qualified definition ID, typed
   properties, role names, subject levels, and Provenance. Cross-Entity Function roles and
   multi-participant Relationships are first-class.
2. Connectivity Relationships require exact admitted Ports. Dependency Relationships such as protects
   require no invented Port or connector. Package-defined Flow references stay separate from
   Relationship, routing, and geometry.
3. Projection preserves one coordinate-free group for a multi-participant connectivity Relationship.
   Spatial alone derives zero or more route legs and all geometry. Neither layer imports knowledge or
   validation contracts or creates/splits Engineering Relationships.
4. Every active EngineeringConnection, connection-model, routing, plugin, Projection, Spatial,
   runtime, and transport consumer migrates directly. Superseded Connection authority and tests are
   deleted; no alias, adapter, fallback, or dual model remains. M41 regressions and all audits pass.

## Tasks / Subtasks

- [ ] Task 1: Install Relationship and Flow contracts (AC: 1, 2)
  - [ ] Add red tests for stable IDs, package-qualified definitions, named roles, Entity/Function/
        Port subjects, typed properties, Provenance, and cross-Entity Function participation.
  - [ ] Add red tests for independent Flow facts referencing Relationship, Flow definition, source
        role, sink role, optional medium/signal identity, exact values, and Provenance.
  - [ ] Implement EngineeringRelationship, Participant Role binding, exact subject references, and
        EngineeringFlow. Allow arbitrary participant count; keep Function ownership identity-only.
  - [ ] Keep kernel domain-neutral: no electrical Flow enum, terminal constant, built-in verb, generic
        from/to authority, or routing rule.
- [ ] Task 2: Replace source authoring and lowering (AC: 1, 2)
  - [ ] Add red grammar, AST, formatter, span, completion, symbol, token, source-edit, and compiler
        fixtures for role bindings, subject levels, properties, and independent Flows.
  - [ ] Freeze human-first source for PLC1 controls KM1, Q1 protects M1, KM1 supplies switched power
        to M1, and M1 drives CV1. No protocol fields or renderer mechanics in source.
  - [ ] Keep one ANTLR semantic parser and Athena AST/span boundary. No parser fork, alias grammar,
        generic endpoint fallback, or Tree-sitter semantic acceptance.
  - [ ] Lower directly to deterministic Relationship/Flow facts. Identity cannot use declaration
        order, parser offsets, Structure display, Projection occurrence, or geometry.
- [ ] Task 3: Migrate connectivity, Projection, Spatial (AC: 2, 3)
  - [ ] Prove connectivity admits exact Ports; protects emits no connector and invents no Port.
  - [ ] Prove multiple independent Flows can reference one Relationship without becoming route data.
  - [ ] Migrate Projection connector facts to Relationship IDs and role bindings; preserve one group.
  - [ ] Migrate Spatial to derive route legs/geometry from resolved Relationship, roles, and Ports.
        Keep all placement/routing implementation in Spatial.
  - [ ] Add no M43 label, style, grid, export, canvas, or paint behavior.
- [ ] Task 4: Delete Connection authority (AC: 4)
  - [ ] Use CodeGraph and compile failures to migrate every EngineeringConnection,
        EngineeringConnectionNetwork, ConnectionIr, connection-model, generic from/to, connection
        node/reference, and transport consumer across all active modules and product surfaces.
  - [ ] Remove connection-model settings/module, superseded production code, tests, fixtures, docs,
        examples, and payload wording. Closed M0-M41 BMad artifacts remain immutable.
  - [ ] Keep ProjectionConnection only as passive derivative with canonical Relationship ID and trace.
  - [ ] Add executable absence audit rejecting aliases, adapters, fallbacks, dual fields, generic
        endpoint authority, and Connection-to-Relationship compatibility shells.
- [ ] Task 5: Verify and complete records (AC: 1-4)
  - [ ] Run red/green per task, all affected modules and full repository tests sequentially on Windows.
  - [ ] Run frontend tests, source-set hygiene, encoding audit, active legacy scan, and git diff check.
  - [ ] Verify M41 Projection/Spatial fixtures; complete checkboxes, Debug Log, Completion Notes,
        File List, and Change Log before review. Do not claim Epic 2 knowledge/validation behavior.

## Dev Notes

### Architecture Guardrails

- Chain remains source meaning -> Engineering facts -> Projection -> Spatial -> Presentation/Theia.
- Apply AD-21, AD-28, AD-34, AD-35 plus M39/M40 boundaries.
- engineering-model owns project Relationships, Participant Roles, Flows, exact subject references,
  authored properties, stable IDs, and resolved definition references. It owns no reusable knowledge
  definitions, capability rules, judgements, routing geometry, or domain constants.
- Knowledge admission rules belong Epic 2. No knowledge-model/evaluator early. No compatibility code.

### Contracts

- EngineeringRelationship: stable ID, package-qualified definition reference, named Participant
  Roles, typed properties, Provenance.
- Participant Role: role name, exact Entity/Function/Port level, subject reference, authored span.
  Functions remain Entity-owned but may participate across Entities.
- EngineeringFlow: Relationship ID, package-qualified Flow definition, source role, sink role,
  optional medium/signal identity, typed properties, Provenance. Flow is not wire/route/geometry.
- Connectivity binds exact Ports; dependency Relationships may bind Entities/Functions without
  inventing Ports. Role names, Flow meanings, and admission are package-owned data.

### Projection And Spatial

- Projection consumes resolved Relationship facts, role bindings, Port identities, and connectivity
  admission only. Multi-participant Relationship stays one coordinate-free group.
- Spatial alone derives zero or more route legs and geometry. It never imports knowledge/validation.
- Existing ProjectionConnection terminology may survive only as passive derivative with canonical
  Relationship identity and source trace. M43 owns visual changes.

### Previous Story Intelligence

- Story 1.1 installed exact EngineeringValue, Entity/Function/Port anatomy, Structure Assignments,
  direct Entity lowering, and Component-authority deletion.
- Story 1.1 kept generic EngineeringConnection temporarily for this story. Replace directly; do not
  restore Component or create parallel model. Sequential Gradle, frontend, hygiene, and encoding
  checks are mandatory. Dirty manifesto submodule is unrelated.

### Expected Implementation Areas

- engineering-model, language, compiler, projection-model, spatial-model, validation, runtime,
  semantic-scm, plugin API/host, domain extensions, ide/lsp, Theia transport.
- Remove connection-model settings/module and stale active tests/docs/examples. Preserve closed artifacts.

### Testing Requirements

- Assert literal stable IDs, role names, subject levels, Flow references, connector admission, and
  exact diagnostics. No self-comparison, count-only, reflection-only, or source-text-only proof.
- Cover cross-Entity Function roles, multi-participant Relationships, exact Port requirement,
  no-Port protects, independent Flows, permutation identity, spans, Projection groups, Spatial legs,
  and Connection absence.
- Required sequential commands:
  .\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test
  .\gradlew.bat --no-daemon --console=plain :kernel:language:test
  .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
  .\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test
  .\gradlew.bat --no-daemon --console=plain :kernel:spatial-model:test
  .\gradlew.bat --no-daemon --console=plain :kernel:validation:test
  .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test
  .\gradlew.bat --no-daemon --console=plain :kernel:semantic-scm:test
  .\gradlew.bat --no-daemon --console=plain :ide:lsp:test
  .\gradlew.bat --no-daemon --console=plain test
  powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
  powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
  git diff --check

### References

- _bmad-output/planning-artifacts/m42/epics.md Epic 1 Story 1.2
- _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-04-m42/prd.md FR-4, FR-5, FR-6, FR-24, FR-25
- _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-04-m42/m42-engineering-knowledge-system-design.md Relationship And Flow Contract
- _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-04-m42/ARCHITECTURE-SPINE.md AD-21, AD-28, AD-34, AD-35
- _bmad-output/implementation-artifacts/m42/1-1-author-exact-engineering-anatomy-and-structure.md

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Context loaded from complete M42 sprint status, Epic 1, PRD, architecture, previous Story 1.1,
  current Connection blast radius, and recent git history.

### Completion Notes List

- BMad story context created; implementation pending.

### File List

- To be populated during implementation with every changed, added, and deleted path.

### Change Log

- 2026-08-05: Created through BMad create-story from M42 Epic 1, PRD, architecture, previous-story
  intelligence, CodeGraph blast radius, current code, and git context.
