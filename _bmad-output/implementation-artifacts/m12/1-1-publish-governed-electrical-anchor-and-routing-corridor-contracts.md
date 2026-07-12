---
baseline_commit: 179a0a2
---

# Story 1.1: Publish Governed Electrical Anchor And Routing-Corridor Contracts

Status: done

## Story

As a platform engineer,
I want Athena to publish explicit downstream anchor and routing-corridor contracts,
so that electrical connection rendering can improve without making route geometry or renderer guesses the source of truth.

## FR Traceability

- FR-1: render the first electrical-domain connections in a visually electrical way
- FR-2: anchor rendered connections to the intended downstream electrical endpoints reliably
- FR-3: make the first renderer path materially more readable under dense electrical cases
- FR-9: improve renderer correctness without reopening earlier semantic foundations
- NFR-1: keep canonical engineering meaning upstream of renderer and workbench behavior
- NFR-2: keep endpoint and routing behavior explainable against governed projection payloads
- NFR-3: keep routing ownership split across semantic, projection, and renderer layers
- NFR-6: keep M12 focused on renderer correctness and workbench hardening
- NFR-7: preserve the existing runtime, LSP, review, and workbench seams instead of creating a second frontend-owned authority

## Acceptance Criteria

1. Given the completed M11 electrical projection-family foundation, when M12 introduces connection-rendering hardening, then Athena publishes governed electrical anchor or port contracts and optional preferred routing-corridor guidance, and those contracts remain downstream of canonical engineering identity.
2. Given routing ownership is reviewed for architecture drift, when the boundary is inspected, then semantic layer owns endpoint identity, projection owns corridor guidance, and renderer owns visual path only, and rendered route geometry does not become engineering truth.

## Tasks / Subtasks

- [x] Publish the M12 electrical anchor contract layer in the kernel-owned projection boundary. (AC: 1, 2)
  - [x] Add additive typed contract types under `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/` for M12 nouns such as `ElectricalAnchor`, `ElectricalAnchorId`, `ElectricalConnectionEndpoint`, or equivalent clearly named types.
  - [x] Keep the contract layer additive beside existing M11 family, sheet, notation, and repeated-reference structures. Do not overload generic node or edge payloads if a typed vocabulary is warranted.
  - [x] Add clean KDoc for every new public or core Kotlin type introduced by this story.
- [x] Publish the first governed routing-corridor guidance model without making route geometry canonical. (AC: 1, 2)
  - [x] Add one additive downstream contract for preferred routing guidance such as `RoutingCorridor`, `RoutingHint`, or equivalent narrow M12 vocabulary.
  - [x] Make the contract explicit that this layer is guidance for downstream rendering only, not engineering truth.
  - [x] Keep the first vocabulary narrow to the first electrical renderer-hardening path. Do not widen this story into a general auto-routing platform.
- [x] Attach anchor and corridor contracts to the existing electrical projection path. (AC: 1, 2)
  - [x] Reuse the existing M11 projection-family, sheet, notation, and repeated-reference publication path rather than inventing a parallel renderer-side model.
  - [x] Update the electrical extension contribution seam so at least one dense electrical proof path can publish anchor and corridor metadata deterministically.
  - [x] Do not move ownership into `integrations/graph-glsp`, `ide/theia-frontend`, or ad hoc DTO-only protocol fields in this story.
- [x] Preserve canonical identity anchoring under the new renderer-correctness contracts. (AC: 1)
  - [x] Ensure anchors and routing guidance resolve from canonical semantic identity first and stay compatible with repeated references, reveal, inspection, review, and knowledge paths.
  - [x] Reuse existing `StableSemanticIdentity`, M11 repeated-reference contracts, and current runtime projection-session seams where possible instead of inventing new identity ladders.
- [x] Verify the new contract layer through focused tests and module documentation. (AC: 1, 2)
  - [x] Add or extend focused tests under:
    - `kernel/projection-model/src/test/kotlin/...`
    - `kernel/runtime/src/test/kotlin/...` if runtime-owned projection-session publication changes
    - `extensions/domain-electrical/src/test/kotlin/...` if the electrical extension needs direct anchor or corridor coverage
  - [x] Cover:
    - typed anchor contract publication
    - deterministic corridor publication
    - canonical identity anchoring for endpoints
    - explicit separation between endpoint truth, corridor guidance, and future rendered path
  - [x] Update affected module README files in English and Chinese if this story changes public core or extension contract surfaces.
  - [x] Run Gradle verification sequentially on Windows with Java 25; do not run build or test tasks concurrently.

## Dev Notes

### Story Intent

- Story `1.1` is the contract-publication entry story for M12.
- The success condition is not "Athena already has trusted electrical rendering." The success condition is "Athena has one explicit, typed, governed anchor and corridor contract surface above downstream rendering."
- Story `1.2` should own the first conductor-first electrical connection rendering outcome.
- Story `1.3` should own stable endpoint anchoring and selection coherence in the delivered renderer path.
- Later M12 stories own viewport hardening, panel density, benchmark tiers, and electrical navigation surfaces.

### Architecture Guardrails

- Align to AD-62: electrical readability remains a downstream consequence of canonical state. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m12/ARCHITECTURE-SPINE.md#AD-62---Electrical-Readability-Remains-A-Downstream-Consequence-Of-Canonical-State]
- Align to AD-63: endpoint anchoring resolves through governed projection anchors. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m12/ARCHITECTURE-SPINE.md#AD-63---Endpoint-Anchoring-Resolves-Through-Governed-Projection-Anchors]
- Align to AD-64: routing ownership stays explicitly split across semantic, projection, and renderer layers. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m12/ARCHITECTURE-SPINE.md#AD-64---Routing-Ownership-Stays-Explicitly-Split-Across-Semantic-Projection-And-Renderer-Layers]
- Preserve inherited AD-27, AD-28, AD-57, and AD-60: projection boundary remains renderer-neutral, engineering identity remains canonical, repeated references stay identity-first, and renderer hardening must preserve mutation, review, and knowledge coherence. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m12/ARCHITECTURE-SPINE.md#Inherited-Invariants]

### Technical Requirements

- The current typed projection seam already exists in `kernel/projection-model` and carries M11 family, sheet, notation, and repeated-reference information.
- The current runtime projection session already exposes downstream-ready projection state through:
  - `AthenaRuntimeProjectionSession`
  - `AthenaRuntimeProjectionView`
  - `AthenaProjectionViewPayload`
- The current electrical extension already contributes the first M11 electrical families and dense proof outputs.
- Story `1.1` should build the M12 anchor and corridor contract layer above those seams rather than bypassing them with:
  - renderer-only edge models
  - frontend-only geometry maps
  - transport-only DTO fields with no kernel-owned contract
  - documentation-only naming with no typed contract surface

### Architecture Compliance

- The story is only successful if later M12 work can point to one clean ladder:
  - canonical `Engineering IR`
  - repeated-reference and M11 downstream projection contracts
  - typed electrical anchor contract
  - typed routing-corridor guidance
  - runtime and LSP delivery
  - renderer consumption
- Prevent these failure modes:
  - encoding endpoint ownership only in renderer-local node ids or graph-edge metadata
  - letting routing-corridor guidance become implied engineering truth
  - publishing anchor meaning only through `AthenaProjectionProtocol` payload classes before kernel/runtime contracts exist
  - widening Story `1.1` into conductor styling, viewport hardening, or navigation UX work

### Library / Framework Requirements

- Use the repo-approved stack already frozen by planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Reuse current Kotlin/JUnit test style already present in:
  - `kernel/projection-model`
  - `kernel/runtime`
  - `extensions/domain-electrical`
- Do not add third-party libraries just to model anchor or corridor vocabulary.

### File Structure Requirements

- Expected update files:
  - `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/` with one new dedicated anchor/corridor contract file or a small cohesive cluster of files
  - `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/`
  - `kernel/projection-model/README.md`
  - `kernel/projection-model/README.zh-CN.md`
  - `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/`
- Possible narrow additive update files if runtime-owned ready snapshots need to surface the new contracts:
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/`
  - `ide/lsp/src/test/kotlin/...` only if current supported-view or ready-payload expectations must reflect the new contract
- Files whose current behavior must be preserved:
  - `kernel/projection-model` downstream contract ownership
  - `kernel/runtime` projection-session ownership
  - `extensions/domain-electrical` governed family contribution path
  - `ide/lsp` transport-only delivery role
  - `integrations/graph-glsp` downstream adapter status
- Explicit non-goals:
  - no rendered conductor styling yet
  - no viewport or fit-to-view implementation yet
  - no panel-density redesign yet
  - no cross-reference navigation UX yet
  - no full auto-routing system

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test"`
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test"`
- Recommended focused regression if runtime publication changes:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:runtime:test"`
- Required proof checks:
  - at least one typed anchor contract exists with stable identity semantics
  - at least one typed routing-corridor guidance contract exists and stays distinct from canonical truth
  - endpoint identity remains canonical and compatible with repeated-reference contexts
  - no renderer-local or viewport-local semantics are introduced
  - no concurrent Gradle build or test execution on Windows

### Current Code State To Preserve

- `kernel/projection-model` currently carries M11 downstream projection vocabulary but does not yet expose explicit M12 anchor or corridor contracts.
- `kernel/runtime` currently transports M11-rich projection state and must remain the owner of projection-session delivery if M12 contract publication expands.
- `extensions/domain-electrical` currently publishes M11 electrical views and the dense proof path; M12 should deepen this seam instead of bypassing it.
- `ide/lsp` currently serializes projection-ready payloads and must not become the first owner of M12 renderer semantics.
- `integrations/graph-glsp` currently translates governed projection payloads into downstream graph shapes and must remain a consumer, not owner, of anchor or corridor truth.

### Previous Milestone Intelligence

- M11 already proved electrical projection families, sheet contracts, notation packs, repeated references, and dense proof publication. Story `1.1` must reuse those results instead of reopening them.
- M8 already proved mutation authority and review anchoring across source and graph. Story `1.1` stays upstream of interaction behavior and must not fork mutation semantics.
- M9 and M10 already proved governed knowledge and reasoning paths. Story `1.1` must keep the new anchor and corridor contracts compatible with those downstream consumers.
- Repo conventions still matter directly here:
  - physical implementation artifacts must live in milestone-local `m*` folders
  - public or core Kotlin surfaces require KDoc
  - Java `25` and sequential Windows Gradle execution are non-negotiable

### Git Intelligence Summary

- Current baseline commit:
  - `179a0a2 feat: complete M10 reasoning proof`
- Practical implication:
  - M12 starts from a stable post-M11 projection and product baseline
  - Story `1.1` should be additive and contract-first
  - first file touches should stay in projection/runtime/electrical seams, not frontend-only styling code

### Project Structure Notes

- `m12/` is now the active milestone-local implementation artifact folder and should follow the same convention as `m10/` and `m11/`.
- Keep naming explicit and easy to read:
  - `ElectricalAnchor`
  - `ElectricalConnectionEndpoint`
  - `RoutingCorridor`
  - `RoutingHint` only if the meaning is truly hint-level
- Avoid putting `Ir` into new names unless the type actually belongs to a distinct IR layer that must be disambiguated.
- Prefer one dedicated contract file or one small cohesive cluster over bloating an existing projection-model file into a mixed-responsibility dump.

### References

- [Source: _bmad-output/planning-artifacts/epics-M12-2026-07-12.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-12-m12/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-12-m12/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m12/ARCHITECTURE-SPINE.md]
- [Source: docs/roadmap/athena-milestone-roadmap.md]
- [Source: docs/usages/m11-proof-usage.md]

## Story Completion Status

- Status: done
- Completion note: Typed electrical anchor, endpoint, and routing-corridor contracts now publish through projection-model, runtime, and LSP with focused verification and updated module documentation.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M12 epic breakdown, PRD, addendum, and architecture spine review
- M11 implementation-artifact folder pattern review
- milestone-local implementation-artifact structure check against `m10/` and `m11/`

### Completion Notes List

- Added typed M12 projection contracts for `ElectricalAnchor`, `ElectricalConnectionEndpoint`, and `ElectricalRoutingCorridor` with stable projection-local ids and KDoc.
- Derived canonical endpoint anchors and corridor guidance from `EngineeringDocument` plus existing M11 projection outputs without moving ownership into runtime adapters or frontend code.
- Published the new contracts through runtime and Athena LSP ready payloads, and extended electrical view contribution scopes to declare anchor and corridor surfaces explicitly.
- Added focused projection-model, runtime, electrical-extension, and LSP coverage, and refreshed stale runtime view-order assertions uncovered during verification.

### File List

- _bmad-output/implementation-artifacts/m12/1-1-publish-governed-electrical-anchor-and-routing-corridor-contracts.md
- kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionElectricalRouting.kt
- kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionDocument.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionElectricalContractsDeriver.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionModels.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSupport.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionPayloads.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionSessionProtocol.kt
- extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeViews.kt
- kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionModelContractTest.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeTest.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticDiffInspectionTest.kt
- extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPluginTest.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionM11DepthRequestTest.kt
- kernel/projection-model/README.md
- kernel/projection-model/README.zh-CN.md

### Change Log

- 2026-07-12: Created the M12 Story 1.1 implementation artifact with contract-first guardrails for endpoint anchors and routing-corridor guidance.
- 2026-07-12: Implemented and verified typed electrical anchor, endpoint, and routing-corridor publication through projection-model, runtime, and LSP.
