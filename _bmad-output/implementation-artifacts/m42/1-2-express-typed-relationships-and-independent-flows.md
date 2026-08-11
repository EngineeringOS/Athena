---
baseline_commit: c00b416c463d3e18876dced3be6b750d2f0652ff
---

# Story 1.2: Express Typed Relationships And Independent Flows

Status: done

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

- [x] Task 1: Install Relationship and Flow contracts (AC: 1, 2)
  - [x] Stable IDs, package-qualified definitions, named roles, exact Entity/Function/Port subjects,
        typed properties, Provenance, cross-Entity Function participation.
  - [x] Independent EngineeringFlow facts reference Relationships and named roles without route data.
  - [x] EngineeringRelationship, Participant Role, exact subject references, and EngineeringFlow contracts.
  - [x] Kernel remains domain-neutral; no electrical constants or generic endpoint authority.
- [x] Task 2: Replace source authoring and lowering (AC: 1, 2)
  - [x] Relation AST/parser/span/compiler fixtures cover role bindings and multi-participant subjects.
  - [x] Human-first relation source remains PLC/control/protection meaning, without renderer mechanics.
  - [x] ANTLR remains sole semantic parser; no parser fork or generic endpoint fallback.
  - [x] Lowering is deterministic and never uses declaration order, projection occurrence, or geometry.
- [x] Task 3: Migrate connectivity, Projection, Spatial (AC: 2, 3)
  - [x] Exact Port connectivity; dependency relationships preserve no invented Port or connector.
  - [x] Projection preserves one named-participant group per Relationship.
  - [x] Spatial consumes participant endpoints and owns route-leg/geometry derivation.
  - [x] No M43 label, style, grid, export, canvas, or paint behavior added.
- [x] Task 4: Delete Connection authority (AC: 4)
  - [x] Active compiler, Projection, Spatial, runtime, LSP, and transport consumers migrated directly.
  - [x] Superseded Connection, graph-glsp, reuse, Semantic Macro, stale examples/scripts/tests removed.
  - [x] ProjectionConnection is passive derivative with canonical Relationship identity and trace.
  - [x] No alias, adapter, fallback, dual field, or Connection-to-Relationship shell remains.
- [x] Task 5: Verify and complete records (AC: 1-4)
  - [x] Sequential affected-module tests and full repository Gradle `test` pass.
  - [x] Frontend tests/build, source-set hygiene, encoding audit, active legacy scan, and `git diff --check` pass.
  - [x] M41 Projection/Spatial fixtures pass; BMad records complete. Epic 2 behavior not claimed.

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
- Replaced Projection `source`/`target` fields with named participant groups; migrated Spatial route
  derivation to consume participant endpoints without creating Engineering Relationships.
- Lowering preserves unresolved authored subjects for validator diagnostics and never invents Ports or
  Flows. Role names come from authored Port properties with deterministic duplicate suffixes.
- Migrated LSP projection payload to participant roles; removed superseded active Connection, graph-glsp,
  reuse, Semantic Macro, milestone script, demo, and stale test surfaces.

### Completion Notes List

- Engineering Relationship identity package-qualified and permutation-stable; Entity, Function, exact
  Port subjects retain authored Provenance.
- Engineering Flow remains independent model fact; no automatic Flow or route/geometry generation.
- Projection emits one coordinate-free Relationship group with all participant endpoints. Spatial keeps
  route-leg and geometry authority.
- Active Connection authority and source/target transport fields removed. No compatibility shim added.
- Full Gradle `test`, affected module tests, frontend tests/build, source-set hygiene, encoding audit,
  and `git diff --check` passed sequentially.

### File List

- `kernel/engineering-model/.../EngineeringRelationshipModels.kt` and test
- `kernel/projection-model/.../ProjectionElements.kt` and contract test
- Compiler lowerer, Projection transformation/compiler, placement, Spatial coverage/authority/anchor
  sources and their migrated tests
- `ide/lsp/.../AthenaProjectionPayloads.kt`, `AthenaProjectionSessionProtocol.kt`, inspection test
- Active legacy deletions recorded in this M42 worktree: Connection/graph-glsp/reuse/Semantic Macro
  surfaces, stale milestone scripts, examples, docs, and tests

### Change Log

- 2026-08-05: Created through BMad create-story from M42 Epic 1, PRD, architecture, previous-story
  intelligence, CodeGraph blast radius, current code, and git context.
- 2026-08-05: Implemented typed Relationship participant groups, independent Flow contract, direct
  lowering, Projection/Spatial migration, LSP transport migration, legacy deletion, and verification.
