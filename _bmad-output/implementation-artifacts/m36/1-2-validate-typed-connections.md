---
baseline_commit: 593d0a1dc86c0bac126fbde0501cc07051180346
---

# Story M36-E1.S2: Validate Typed Connections

Status: done

## Story

As an engineer or AI agent,
I want to connect declared ports through typed connections,
So that invalid engineering relationships fail before projection.

**Requirements:** FR-3, FR-10.

## Acceptance Criteria

1. An authored connection resolves to exactly two declared typed ports unless it uses an explicit
   network operation.
2. Endpoint existence, direction, signal kind, role, and compatibility are validated before layout
   or routing.
3. Invalid, self-conflicting, or ambiguous endpoints fail deterministically with both source locations.
4. A connection cannot resolve directly to an Element, Symbol, SVG node, or geometry reference.

## Tasks / Subtasks

- [x] Add failing connection-contract validation tests for valid, missing, incompatible, self, and ambiguous endpoints (AC: 1-4)
- [x] Extend `connection-model` with compiler-owned typed connection validation derived from Engineering IR (AC: 1-3)
- [x] Map validation failures through compiler and LSP diagnostics with endpoint provenance (AC: 2-4)
- [x] Run targeted, full regression, encoding audit, and diff check; record evidence (AC: 1-4)

## Dev Notes

- Reuse M36-E1.S1 `ConnectableEntityContract`; do not create an Engineering Component System.
- Athena source is SSOT. No SVG, XML, renderer, LSP, or planner authority.
- The validator works from canonical Engineering IR. `connectable enabled` is the narrow M36 admission boundary.
- Keep generic terms: entity, interface, port, connection, network. Network semantics are Story 1.3.
- Do not add geometry binding, placement, routing, or ELK work in this story.
- Compiler diagnostics must use `SemanticDiagnosticCategory.CONNECTION` and normal source spans; LSP only publishes compiler output.
- Identity is normalized portable source identity. Do not restore absolute-path or v1 lock compatibility.
- Run Gradle commands sequentially. Full root `test` is mandatory before status `review`.

## Previous Story Intelligence

- M36-E1.S1 introduced `ConnectableEntityContractCompiler` in `connection-model` and compiler integration in `AthenaCompilerCompilationSupport`.
- It proved normal compiler/LSP diagnostic publication. Extend that path rather than adding an LSP scanner.
- Full regression found stale pre-M35 package and source-unit fixtures; current source hierarchy and derived v2 locks are authoritative.

## Dev Agent Record

### Debug Log References

- Pending RED test.

### Completion Notes

- Added compiler-owned validation for admitted typed connections: resolved endpoints, two connectable ports, self-connection, direction, signal kind, role, and shared compatibility parameters.
- Invalid endpoint-to-endpoint relationships emit two normal compiler/LSP `CONNECTION` diagnostics, one at each Port provenance span.
- The validator leaves non-admitted legacy connections uninterpreted; it does not introduce geometry, SVG, layout, routing, or ECS authority.
- Evidence: connection-model tests, focused compiler and LSP diagnostics tests, root `test` (8m36s), encoding audit, and `git diff --check` passed on 2026-07-28.

### File List

- `kernel/connection-model/src/main/kotlin/com/engineeringood/athena/connection/ConnectableEntityContracts.kt`
- `kernel/connection-model/src/test/kotlin/com/engineeringood/athena/connection/ConnectableEntityContractCompilerTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM36ConnectableEntityCompilationTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt`

### Change Log

- 2026-07-28: Implemented and fully verified M36-E1.S2 typed connection validation.

## References

- `_bmad-output/implementation-artifacts/m36/epics.md` - M36-E1.S2.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-28-m36/prd.md` - FR-3, FR-10.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-28-m36/ARCHITECTURE-SPINE.md` - source authority and lowering boundaries.
