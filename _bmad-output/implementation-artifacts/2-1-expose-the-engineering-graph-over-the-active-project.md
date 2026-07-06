---
baseline_commit: bdc3227afbefd6834923c325aa556eaa8a2f6d77
---

# Story 2.1: Expose The Engineering Graph Over The Active Project

Status: done

## Story

As an operator or platform builder,
I want `Athena Runtime` to expose the active project as an `Engineering Graph` with stable identities and queryable relationships,
so that I can inspect semantic objects and dependencies through a runtime-facing model without creating a second semantic authority beside `Engineering IR`.

## Acceptance Criteria

1. Given an active `Workspace` and `Project` managed by `Athena Runtime`, when graph services are requested, then the runtime exposes an `Engineering Graph` projection over the active project's canonical semantic state, and the graph is owned as a runtime projection rather than as an independent semantic model.
2. Given semantic objects such as systems, components, ports, connections, and related properties exist in canonical state, when they are represented through the `Engineering Graph`, then nodes and relationships reuse stable canonical identities, and the graph does not invent graph-only semantic meaning or competing durable identifiers.
3. Given a consumer needs semantic inspection, when it performs graph query, traversal, reference lookup, or dependency inspection, then published graph APIs can return semantic neighbors, referenced objects, and affected relationships, and those operations do not require the consumer to access parser-specific or renderer-specific structures directly.
4. Given the first graph projection is implemented, when standard Java `25` build and runtime checks run, then the workspace builds successfully and graph inspection works over the active project, and the implementation preserves the invariant that `Engineering IR` remains the only canonical semantic authority.

## Tasks / Subtasks

- [x] Add a runtime-owned engineering-graph projection over canonical compiler output. (AC: 1, 2)
  - [x] Add documented runtime graph projection types for nodes, relationships, references, and unavailable projections.
  - [x] Keep graph identity shared with canonical `Engineering IR` rather than inventing graph-only stable identifiers.
  - [x] Ensure parse failure produces an unavailable runtime projection while semantic validation blockage can still expose graph structure.
- [x] Expose the engineering graph as a runtime capability. (AC: 1, 3)
  - [x] Add a typed `AthenaEngineeringGraphService` in the runtime service registry.
  - [x] Expose the shared graph service through `AthenaExecutionContext`.
  - [x] Keep the graph projection runtime-owned and independent from renderer-private or parser-private consumer contracts.
- [x] Support the first graph query and traversal surface. (AC: 2, 3)
  - [x] Add node lookup, node-kind filtering, relationship-kind filtering, neighbor traversal, dependency traversal, and reference lookup.
  - [x] Add affected-relationship inspection over the active graph.
  - [x] Keep all graph APIs operating on runtime-facing graph types only.
- [x] Document and verify the graph boundary. (AC: 1, 2, 3, 4)
  - [x] Add architecture-facing docs describing the runtime graph as a projection over canonical `Engineering IR`.
  - [x] Add focused runtime tests proving stable identities, traversal, unresolved references, and service-registry exposure.
  - [x] Keep verification strictly sequential on Windows and preserve the root Java `25` build path.

## Dev Notes

### Story Intent

- Story `2.1` is the first Epic `2` semantic-inspection slice.
- It exposes a runtime graph over the active project but does not introduce semantic mutation.
- The graph exists to support traversal, inspection, and later command or diff work.

### Architecture Guardrails

- `Engineering IR` remains the only canonical semantic authority.
- `Engineering Graph` is a runtime projection with shared stable identities.
- The runtime owns graph capability through typed services.
- Graph APIs must not leak parser-owned AST types or renderer-owned scene types.
- Command runtime, diff, undo/redo, and incremental recomputation remain later Epic `2` work.

### Technical Requirements

- Reuse canonical identities already assigned by `EngineeringIrLowerer`:
  - `system:<name>`
  - `component:<name>`
  - `port:<owner>.<port>`
  - `connection:<from>-><to>`
- Keep graph projection derivation inside `:runtime`.
- Keep all new core Kotlin classes documented with KDoc.
- Preserve the package root `com.engineeringood.athena`.

### Testing Requirements

- Minimum verification commands for story completion:
  - `.\\gradlew.bat --no-daemon --console=plain :runtime:test`
  - `.\\gradlew.bat --no-daemon --console=plain build`
  - `.\\gradlew.bat --no-daemon --console=plain test`
- On this Windows repo, Gradle verification must remain sequential. Do not run `build` and `test` concurrently.

### References

- `_bmad-output/planning-artifacts/epics.md`
  - `Story 2.1: Expose The Engineering Graph Over The Active Project`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-03/prd.md`
  - `FR-4`, `FR-5`, `FR-6`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-03/ARCHITECTURE-SPINE.md`
  - `AD-2`, `AD-3`, `AD-5`
- `docs/compiler/m1-engineering-graph-boundary.md`

## Story Completion Status

- Status: review
- Completion note: Runtime-owned engineering-graph projection, query surface, service exposure, and sequential verification are complete for Story `2.1`.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Created a runtime graph projection derived from `CompilerCompilationSuccess.document` so the graph reuses canonical semantic identities instead of inventing graph-only IDs.
- Added a shared `AthenaEngineeringGraphService` to the runtime service registry and routed execution-context graph projection through that typed service.
- Added tests for ready and unavailable graph projections, unresolved references under semantic blockage, node-kind queries, relationship-kind queries, neighbors, dependencies, and affected-relationship inspection.
- Reconfirmed the repo-specific Windows rule that Gradle verification must remain sequential; concurrent `build` and `test` runs produced output collisions and were discarded in favor of clean sequential reruns.
- Verified with `.\\gradlew.bat --no-daemon --console=plain :runtime:test`, `.\\gradlew.bat --no-daemon --console=plain build`, and `.\\gradlew.bat --no-daemon --console=plain test`.

### Completion Notes List

- Added a runtime-facing engineering graph projection that exposes nodes, relationships, references, and unavailable parse results without relocating semantic authority.
- Added node-kind and relationship-kind filtering plus neighbor, dependency, reference, and affected-relationship inspection APIs.
- Added a typed runtime graph service so consumers resolve graph capability through `AthenaServiceRegistry` and `AthenaExecutionContext`.
- Added architecture-facing graph-boundary documentation tied to `Engineering IR` ownership and runtime service ownership.
- Verified the full Story `2.1` proof path sequentially on Java `25`.

### File List

- `_bmad-output/implementation-artifacts/2-1-expose-the-engineering-graph-over-the-active-project.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `docs/compiler/m1-engineering-graph-boundary.md`
- `docs/compiler/m1-runtime-host-boundary.md`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaEngineeringGraphProjection.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaEngineeringGraphService.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt`
- `runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaEngineeringGraphProjectionTest.kt`
- `runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeTest.kt`

### Change Log

- Added the first runtime-owned engineering graph projection and query surface over the active project.
- Added a typed runtime graph service in the service registry and exposed it through execution context.
- Added graph-boundary documentation and sequential verification guidance for the Story `2.1` proof.
