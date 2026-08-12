---
story: 1.3
epic: 1
title: Validate And Compile Canonical Connection IR
status: done
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
created: 2026-08-10
---

# Story 1.3: Validate And Compile Canonical Connection IR

Status: done

## Story

As an engineer,
I want accepted connectivity compiled into one canonical renderer-neutral Connection IR,
so that every downstream view consumes identical validated connection facts.

## Acceptance Criteria

1. Given valid Engineering Connections/Nets and Port anatomy, when `ConnectionIrCompiler` runs, then it
   resolves, validates, normalizes, derives topology, canonicalizes, and publishes a `ConnectionDocument`
   from new `kernel/connection-model`; equal inputs produce equal `athena-connection-ir-c14n-v1` SHA-256 digest.
2. Given invalid endpoint direction/role, domain, potential/signal, physical requirement, missing endpoint,
   or topology, when compilation runs, then no replacement Connection IR publishes and accepted publication
   remains `STALE`/`UNAVAILABLE` under existing runtime rules.
3. Given a Net needing branch, merge, pass-through, or interruption topology, when IR publishes, then stable
   typed Topology Operators preserve endpoint order and source trace; operators cannot change Net membership
   or own route geometry.

## Tasks / Subtasks

- [x] Define `kernel/connection-model` canonical contracts (AC: 1, 3)
  - [x] Add `ConnectionDocument`, `ConnectionFact`, `NetFact`, endpoint facts, resolved specifications,
        topology operator types, validation state, source trace, and canonical digest contracts.
  - [x] Keep dependency only on `kernel:engineering-model`; exclude coordinates, renderer nodes, DOM, Konva,
        viewport, mouse, and authored topology/ordering fields.
  - [x] Add deterministic canonicalization and `athena-connection-ir-c14n-v1` SHA-256 serialization.
- [x] Implement compiler-owned resolve/validate/normalize/topology/publication pipeline (AC: 1-3)
  - [x] Resolve Engineering Ports, Connections, Nets, scoped effective facts, and potential/signal references.
  - [x] Validate all endpoint role/direction, closed kinds, physical requirements, unresolved references,
        and domain compatibility; fail closed before IR publication.
  - [x] Derive stable `BRANCH`, `MERGE`, `PASS_THROUGH`, and `INTERRUPTION` operators without changing Net
        membership or embedding route geometry.
- [x] Integrate accepted publication and retention (AC: 2)
  - [x] Publish only valid canonical `ConnectionDocument` through existing runtime service boundary.
  - [x] Preserve previous accepted publication as `STALE` on rejected replacement; return `UNAVAILABLE` when
        no accepted publication exists. Keep diagnostics exact and human-first.
- [x] Prove contracts and regression hygiene (AC: 1-3)
  - [x] Model tests for contract invariants, canonical order, digest stability, operator identity, and trace.
  - [x] Compiler/validation/runtime tests for valid and fail-closed paths, publication retention, and no
        geometry/render vocabulary.
  - [x] Run affected Gradle tests sequentially, encoding audit, source-set hygiene audit, and product proof.

## Dev Notes

### Authority And Scope

```text
Athena source
  -> EngineeringConnection / EngineeringNet
  -> ConnectionIrCompiler
  -> ConnectionDocument (canonical derived IR)
  -> Story 1.4 ConnectionProjection
```

This story owns canonical connection IR only. It does not implement route geometry, SVG/Konva painting,
canvas edits, or projection adapters. Source remains authority for engineering meaning. Connection IR is
compiler-owned derived truth and is disposable/recomputable.

### Required Pipeline

```text
resolve -> validate -> normalize -> topology -> canonicalize -> publish
```

Any error blocks replacement publication. Existing accepted publication retention must use current runtime
`STALE`/`UNAVAILABLE` contracts; do not invent a second cache or compatibility adapter.

### Canonical Contract Rules

- `ConnectionDocument` depends only on `kernel:engineering-model`.
- Canonical digest prefix is exact: `athena-connection-ir-c14n-v1`.
- Canonical ordering derives from stable semantic identities and authored endpoint roles, never source map
  iteration, parser offsets, viewport, or geometry.
- Topology operators are typed derived facts: `BRANCH`, `MERGE`, `PASS_THROUGH`, `INTERRUPTION`; each keeps
  stable identity, ordered endpoint/continuation identities, and source trace.
- Operators never mutate Net membership. Angles, bends, junction paint, and route segments remain later
  projection/spatial concerns.
- No `ProjectionConnection`, generic Relationship route lowering, `.elmt`, HTML, XML, renderer, layout,
  compatibility, fallback, milestone, demo, or sample production path.

### Existing M46.1/1.2 Intelligence

- `EngineeringConnection` and `EngineeringNet` already carry source-derived identity, roles, typed properties,
  effective specification properties, and provenance.
- Generic `EngineeringRelationship` is not connectivity and must stay absent from Connection IR.
- Invalid endpoint or fact admission publishes no partial Connection/Net.
- Gradle verification on Windows is strictly sequential. Tree-sitter command is `Set-Location ide/tree-sitter-athena; yarn test`.

### Testing Requirements

Use Kotlin/JUnit tests in owning modules. Cover deterministic equal-input digest, order normalization,
all six closed kinds, every operator, invalid direction/role/domain/potential/fact/topology, accepted
publication retention, and no renderer/layout fields. Run:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:connection-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:validation:test
.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
Set-Location ide/tree-sitter-athena; yarn test; Set-Location ../..
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
```

## Dev Agent Record

### Agent Model Used

Codex

### Debug Log References

- RED `:kernel:connection-model:test` confirmed missing canonical contract surface before implementation.
- Runtime compile initially lacked direct `:kernel:connection-model` dependency; added explicit module dependency and reran green.
- First LSP run reached `BUILD SUCCESSFUL` but wrapper timed out; rerun with 240s produced clean exit.

### Completion Notes List

- Added `kernel:connection-model` canonical Connection IR contracts, deterministic normalization, and SHA-256 digest.
- Added compiler-owned `ConnectionIrCompiler` resolve/validate/normalize/topology/canonicalize flow.
- Added stable BRANCH, MERGE, PASS_THROUGH, and INTERRUPTION operators with source trace and no geometry fields.
- Added `AthenaConnectionPublicationService` with accepted replacement, `STALE`, and `UNAVAILABLE` states; exposed through runtime registry/context.
- Verification passed sequentially: `:kernel:engineering-model:test`, `:kernel:language:test`, `:kernel:connection-model:test`, `:kernel:compiler:test`, `:kernel:validation:test`, `:kernel:runtime:test`, `:ide:lsp:test`, Tree-sitter `yarn test`, encoding audit, source-set hygiene audit.

- Ultimate BMad context created from M46 PRD, architecture spine, Epic 1 story plan, previous Story 1.2
  contracts and verification lessons.

### File List

- `settings.gradle.kts`
- `kernel/connection-model/build.gradle.kts`
- `kernel/connection-model/src/main/kotlin/com/engineeringood/athena/connection/ConnectionModels.kt`
- `kernel/connection-model/src/test/kotlin/com/engineeringood/athena/connection/ConnectionDocumentTest.kt`
- `kernel/compiler/build.gradle.kts`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionIrCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ConnectionIrCompilerTest.kt`
- `kernel/runtime/build.gradle.kts`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaConnectionPublicationService.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaConnectionPublicationServiceTest.kt`

### Change Log

- 2026-08-10: Created Story 1.3 context; status `ready-for-dev`.
- 2026-08-10: Implemented canonical Connection IR, topology operators, and accepted publication retention; status `review`.
