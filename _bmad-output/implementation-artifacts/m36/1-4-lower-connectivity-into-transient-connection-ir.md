---
baseline_commit: 593d0a1dc86c0bac126fbde0501cc07051180346
---

# Story M36-E1.S4: Lower Connectivity Into Transient Connection IR

Status: done

## Story

As the compiler,
I want to lower validated connectivity into disposable Connection IR,
so that later placement and routing use typed facts without gaining semantic authority.

**Requirements:** FR-37, FR-38.

## Acceptance Criteria

1. Connection IR includes entities, ports, connections, networks, compatibility evidence, provenance, and explicit constraint owner and strength.
2. Semantic, Representation, Physical, and Layout Preference constraints remain distinguishable after lowering, and required constraints cannot be weakened by later planners.
3. Connection IR is compiler-owned, non-authored, non-persisted project truth and is excluded from renderer and LSP raw transport.
4. Lowering is deterministic for the same source, package snapshot, and compiler snapshot, and preserves stable identities and source spans without inventing new engineering meaning.

## Tasks / Subtasks

- [x] Add failing tests for transient connection lowering and constraint ownership (AC: 1-4)
  - [x] Cover a valid lowered connection snapshot, owner/strength preservation, deterministic identities, and no raw transport leakage.
  - [x] Prove the tests fail before any model or compiler changes.
- [x] Extend the compiler-owned transient connection facts to carry owner and strength (AC: 1-2)
  - [x] Reuse the existing `EngineeringIrLowerer` boundary and the canonical engineering model; do not add a parallel semantic scanner or new parsing path.
  - [x] Keep the lowered facts transient, snapshot-bound, and non-authoritative.
- [x] Integrate lowering validation boundaries and diagnostics evidence (AC: 2-3)
  - [x] Ensure required constraints stay required through lowering while planners may only optimize preferences.
  - [x] Keep renderer and LSP transport on normalized typed facts only; never expose raw lowered IR as project truth.
- [x] Run story evidence gate (AC: 1-4)
  - [x] Run sequential Gradle tests, encoding audit, and `git diff --check`.
  - [x] Record AC-to-evidence, file list, and completion notes.

## Dev Notes

- Athena source is SSOT. Do not add XML authority, compatibility shims, renderer-owned truth, or a second component system.
- `EngineeringIrLowerer.lower` already resolves components, ports, connections, functions, and `connectionNetworks`; extend that lowering boundary instead of inventing a separate semantic scanner.
- The current canonical IR lives in `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt` and already carries `EngineeringDocument`, `EngineeringConnectionNetwork`, `EngineeringNetworkJunction`, `EngineeringNetworkCompatibilityEvidence`, `EngineeringReference`, and `StableSemanticIdentity`.
- `kernel/connection-model/src/main/kotlin/com/engineeringood/athena/connection/ConnectableEntityContracts.kt` already validates typed connectivity and network semantics. This story should consume validated connectivity, not re-derive it from parser state.
- Preserve the architecture boundary from M36 PRD/architecture: transient Connection IR is compiler-owned and disposable; later planners may optimize Layout Preferences but cannot weaken required Semantic, Representation, or Physical constraints.
- Keep the scope out of SVG admission, Port-to-Anchor binding, Layout Graph construction, routing, Cabinet placement, and renderer work. Those belong to later stories.
- Use the existing generic connectivity vocabulary: Connectable Entity, Interface, Port, Connection, Network, Junction.
- Run RED first, then sequential Gradle verification. Do not run Gradle tasks in parallel.

### Project Structure Notes

- Likely touchpoints: `kernel/engineering-model`, `kernel/compiler`, and `kernel/connection-model`.
- Likely test locations: `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler`, `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic`, and `kernel/connection-model/src/test/kotlin/com/engineeringood/athena/connection`.
- Do not move work into `ide/`, `extensions/`, `routing-model`, `physical-model`, or renderer modules for this story.

### References

- `_bmad-output/implementation-artifacts/m36/epics.md` - Story 1.4, FR-37, FR-38.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-28-m36/prd.md` - FR-37, FR-38, NFR-9.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-28-m36/ARCHITECTURE-SPINE.md` - AD-3, AD-4.
- `_bmad-output/implementation-artifacts/m36/1-3-model-networks-and-semantic-junctions.md` - prior network lowering and validation pattern.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt` - current lowering boundary.
- `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt` - canonical lowered IR types.
- `kernel/connection-model/src/main/kotlin/com/engineeringood/athena/connection/ConnectableEntityContracts.kt` - validated connectivity contract.

## Dev Agent Record

### Agent Model Used

Codex (GPT-5)

### Debug Log References

- 2026-07-28: Added red tests for transient connection IR lowering, constraint owner/strength preservation, deterministic lowering, and public transport leakage guards.
- 2026-07-28: Introduced compiler-owned `ConnectionIr` models and a `lowerConnectionIr` boundary on `EngineeringIrLowerer`.
- 2026-07-28: Verified `:kernel:compiler:test --tests com.engineeringood.athena.compiler.ConnectionIrLoweringTest`, `:kernel:compiler:test`, `:kernel:connection-model:test`, `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`, and `git diff --check`.

### Completion Notes List

- Added a compiler-owned transient `ConnectionIr` model with entities, ports, connections, networks, junctions, and compatibility evidence carrying explicit owner and strength.
- Lowered canonical engineering connectivity into transient Connection IR without changing the canonical `EngineeringDocument` authority boundary.
- Kept the transient IR out of compiler success payloads and public transport surfaces.
- Verified the change with sequential Gradle tests plus encoding and diff hygiene checks.

### File List

- _bmad-output/implementation-artifacts/m36/1-4-lower-connectivity-into-transient-connection-ir.md
- _bmad-output/implementation-artifacts/m36/sprint-status.yaml
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionIrLowerer.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionIrModels.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ConnectionIrLoweringTest.kt
