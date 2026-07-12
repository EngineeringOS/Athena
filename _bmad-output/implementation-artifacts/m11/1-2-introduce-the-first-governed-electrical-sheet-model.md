---
baseline_commit: c278a71
---

# Story 1.2: Introduce The First Governed Electrical Sheet Model

Status: done

## Story

As an engineer,
I want Athena to support stable electrical sheets or pages,
so that I can navigate denser electrical documentation without turning page structure into engineering truth.

## FR Traceability

- FR-3: support the first governed sheet or page model for electrical workbench operation
- FR-2: preserve one canonical semantic identity across richer electrical workbench views
- NFR-1: keep canonical engineering meaning upstream of sheet, symbol, notation, and renderer behavior
- NFR-2: keep subject identity stable across repeated views, sheets, and downstream representations
- NFR-4: keep the sheet boundary inspectable for architecture and debugging
- NFR-6: preserve the existing runtime, LSP, and workbench seams instead of creating frontend-owned authority

## Acceptance Criteria

1. Given an electrical project with governed downstream views, when Athena constructs sheet-aware representations, then each sheet has stable identity, ordering, and navigation semantics, and sheet identity remains separate from engineering identity.
2. Given the same engineering subject appears on one or more sheets, when the workbench resolves selection or navigation, then Athena preserves the canonical subject anchor across those sheets, and sheet membership does not redefine the meaning of the subject.

## Tasks / Subtasks

- [ ] Publish the first governed electrical sheet contract layer in the kernel-owned projection boundary. (AC: 1, 2)
  - [ ] Add one additive typed sheet contract file under `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/` or another clearly justified downstream package for M11 nouns such as `ElectricalSheetId`, `ElectricalSheet`, `ElectricalSheetPlacement`, or equivalent sheet-owned contract types.
  - [ ] Keep sheet identity downstream and projection-owned. Do not reuse sheet ids as canonical engineering ids or attach sheet truth directly to `Engineering IR`.
  - [ ] Add clean KDoc for every new public/core Kotlin type introduced by this story.
- [ ] Freeze the first governed electrical sheet vocabulary for M11. (AC: 1)
  - [ ] Define the first narrow sheet semantics needed for serious electrical workbench depth only:
    - stable sheet identity
    - deterministic sheet ordering
    - sheet display name or label
    - sheet-owned membership or placement for canonical subjects
  - [ ] Keep the first sheet vocabulary narrow to the electrical ECAD path. Do not widen this story into report generation, print layout engines, or unrestricted page authoring.
  - [ ] Make it explicit that sheet membership is a downstream navigation/documentation contract, not engineering meaning.
- [ ] Attach the governed sheet model to the new M11 electrical view family path. (AC: 1, 2)
  - [ ] Update the electrical projection-facing seam so at least one first sheet-aware electrical representation can publish governed sheet data through approved plugin contribution or compiler/runtime-owned downstream derivation.
  - [ ] Reuse the M11 family-contract path from Story `1.1`; do not invent a second electrical view taxonomy for sheets.
  - [ ] Preserve deterministic ordering and current runtime-supported view behavior while extending the model additively.
- [ ] Preserve canonical subject anchoring across sheets. (AC: 2)
  - [ ] Ensure one canonical engineering subject can appear on one or more governed sheets without creating new semantic identities.
  - [ ] Keep reveal, inspection, and later cross-reference paths capable of resolving a sheet occurrence back to canonical `StableSemanticIdentity`.
  - [ ] Do not let sheet-local symbol placement, aliases, or page coordinates become the first owner of subject resolution in this story.
- [ ] Surface the first sheet-aware model through current runtime and transport seams only as needed. (AC: 1, 2)
  - [ ] Extend runtime-owned projection/session or downstream payload contracts only where required to publish governed sheet data.
  - [ ] Keep runtime and `ide/lsp` as consumers and transport owners, not semantic owners, of sheet meaning.
  - [ ] Do not redesign the frontend/workbench in this story.
- [ ] Verify the sheet contract layer through focused tests and module documentation. (AC: 1, 2)
  - [ ] Add or extend focused tests under:
    - `kernel/projection-model/src/test/kotlin/...`
    - `kernel/runtime/src/test/kotlin/...` if runtime-owned projection/session expectations change
    - `ide/lsp/src/test/kotlin/...` if sheet-aware projection payloads change
    - `extensions/domain-electrical/src/test/kotlin/...` if the electrical plugin or electrical downstream view model publishes the first sheet vocabulary directly
  - [ ] Cover:
    - stable sheet identity and deterministic ordering
    - canonical subject anchoring across one or more sheets
    - additive compatibility with the current electrical projection-family path
    - explicit separation between sheet identity and engineering identity
  - [ ] Update affected module README files in English and Chinese if this story changes public core or extension contract surfaces.
  - [ ] Run Gradle verification sequentially on Windows with Java 25; do not run build or test tasks concurrently.

## Dev Notes

### Story Intent

- Story `1.2` is the sheet-contract entry story for M11.
- The success condition is not "Athena already behaves like a full document editor." The success condition is "Athena has one explicit, typed, governed sheet model that stays downstream of canonical engineering meaning."
- Story `1.1` already introduced governed electrical projection-family contracts and the first four-family electrical vocabulary.
- Story `1.3` owns governed symbol and notation packs.
- Story `1.4` owns richer runtime/LSP delivery of multi-view electrical outputs.
- Story `1.5` owns reveal and inspection coherence across delivered views.
- Epic `2.x` owns repeated references, dense proof fixtures, and denser workbench interaction.

### Architecture Guardrails

- Align to AD-55: sheet identity is projection-owned and separate from engineering identity. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m11/ARCHITECTURE-SPINE.md#AD-55---Sheet-Identity-Is-Projection-Owned-And-Separate-From-Engineering-Identity]
- Align to AD-53: electrical workbench depth starts from canonical engineering entities, not symbols or pages. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m11/ARCHITECTURE-SPINE.md#AD-53---Electrical-Workbench-Depth-Starts-From-Canonical-Engineering-Entities-Not-Symbols]
- Align to AD-54: M11 projection families stay above one canonical subject and sheet structure must compose with them rather than replace them. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m11/ARCHITECTURE-SPINE.md#AD-54---M11-Introduces-Explicit-Electrical-Projection-Families-Above-One-Canonical-Subject]
- Preserve inherited AD-27, AD-28, and AD-29: `kernel/projection-model` remains the renderer-neutral projection boundary, engineering identity remains outside downstream views, and layout/geometry stay view-scoped metadata. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m11/ARCHITECTURE-SPINE.md#Inherited-Invariants]
- Preserve AD-59 and AD-60: sheet-aware depth must remain additive through existing runtime/LSP seams and stay coherent with mutation, review, and knowledge paths. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m11/ARCHITECTURE-SPINE.md#AD-59---Workbench-Density-Remains-Additive-Through-Existing-Runtime-And-LSP-Seams] [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m11/ARCHITECTURE-SPINE.md#AD-60---Electrical-Workbench-Depth-Must-Preserve-Mutation-Review-And-Knowledge-Coherence]

### Technical Requirements

- Story `1.1` already added typed projection-family contracts on the current `ViewDefinition` seam in `kernel/layout-model`.
- The current downstream projection document still packages one `ViewDefinition` with nodes, connections, and labels in `kernel/projection-model`.
- The current runtime projection session still publishes supported views and one active projection snapshot through:
  - `AthenaRuntimeProjectionView`
  - `AthenaRuntimeProjectionSession`
  - `AthenaProjectionViewPayload`
- The current electrical domain plugin now contributes:
  - `cabinet`
  - `wiring`
  - `schematic`
  - `documentation`
  through `AthenaViewDefinitionContributor`.
- Story `1.2` should introduce the first explicit sheet model above those seams rather than bypassing them with:
  - frontend-only page trees
  - renderer-local page coordinates as semantic truth
  - documentation-only labels with no typed contract surface
  - ad hoc string maps that make sheet membership opaque

### Architecture Compliance

- The story is only successful if later M11 work can point to one clean ladder:
  - canonical `Engineering IR`
  - typed electrical projection-family contract
  - typed governed electrical sheet model
  - governed notation and repeated-reference depth
  - runtime and LSP delivery
- Prevent these failure modes:
  - treating sheet ids as canonical engineering ids
  - storing sheet meaning only in raw view ids, free-form properties, or frontend widgets
  - letting one sheet occurrence redefine the meaning of a subject
  - pushing the first sheet model directly into renderer or Theia-owned payloads before kernel/runtime contracts exist
  - widening Story `1.2` into notation packs, repeated references, or dense workbench polish

### Library / Framework Requirements

- Use the repo-approved stack already frozen by planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Reuse current Kotlin/JUnit test style already present in:
  - `kernel/projection-model`
  - `kernel/runtime`
  - `ide/lsp`
  - `extensions/domain-electrical`
- Do not add third-party libraries just to model the sheet vocabulary.

### File Structure Requirements

- Expected update files:
  - `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionModel.kt` or one new sibling contract file under the same package
  - `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/`
  - `kernel/projection-model/README.md`
  - `kernel/projection-model/README.zh-CN.md`
- Likely additive update files if runtime-supported view or snapshot contracts need to surface sheet data:
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt`
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`
- Likely additive update files if the electrical extension publishes the first narrow sheet vocabulary directly:
  - `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`
  - `extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPluginTest.kt`
  - `extensions/domain-electrical/README.md`
  - `extensions/domain-electrical/README.zh-CN.md`
- Files whose current behavior must be preserved:
  - [`kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`](../../../kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt)
    - view definitions and family contracts remain presentation intent only
  - [`kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionModel.kt`](../../../kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionModel.kt)
    - projection documents remain downstream and inspectable
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt)
    - runtime session remains the owner of supported-view delivery
  - [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt)
    - `ide/lsp` remains a transport and delivery surface, not a sheet-semantics owner
  - [`extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`](../../../extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt)
    - current four-family electrical proof must continue to work while sheets are added additively
- Explicit non-goals:
  - no notation-pack implementation yet
  - no repeated-reference resolution yet
  - no sheet-editing UI or page-layout editor
  - no report generation or print pipeline
  - no frontend/workbench redesign

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test"`
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test"`
- Recommended focused regressions if runtime or transport payloads change:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaRuntimeProjectionSessionTest"`
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest"`
- Required proof checks:
  - at least one first sheet model exists with stable typed identity and deterministic ordering
  - the same canonical subject can appear on one or more sheets without changing semantic identity
  - current electrical projection-family behavior remains additive and deterministic
  - sheet identity remains explicit and distinct from engineering identity
  - no concurrent Gradle build/test execution on Windows

### Current Code State To Preserve

- [`kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`](../../../kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt) now defines typed projection-family contracts on `ViewDefinition`, including `ElectricalProjectionDescriptor`.
- [`kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionModel.kt`](../../../kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionModel.kt) currently packages one `ViewDefinition` into one `ProjectionDocument`; it does not yet expose an explicit governed sheet concept.
- [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt) currently builds supported views from compiler-owned `supportedViewDefinitions()` and surfaces them through `AthenaRuntimeProjectionView`; it does not yet publish sheet-aware contracts.
- [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt) currently serializes supported views, ownership contracts, and ready projection data only; it must not become the first owner of sheet semantics.
- [`extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`](../../../extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt) now publishes four electrical projection families and is the likely first contribution point for a narrow sheet-aware electrical representation.

### Previous Story Intelligence

- Story `1.1` proved the right M11 seam: typed downstream contracts belong on the kernel-owned projection/view-definition path, while runtime and LSP remain consumers.
- Story `1.1` already expanded the electrical plugin view set to `cabinet`, `wiring`, `schematic`, and `documentation`. Story `1.2` should compose sheet structure with that family vocabulary instead of replacing it.
- Story `1.1` also established a practical verification pattern for M11:
  - targeted kernel/module tests first
  - targeted runtime and LSP tests when transport changes
  - sequential Gradle execution only on Windows
- Story `1.1` uncovered a real Windows hazard: parallel test runs can corrupt Gradle test-result state and produce opaque `EOFException` or `NoSuchFileException` failures. Story `1.2` must keep runtime and LSP verification strictly sequential.

### Git Intelligence Summary

- Current baseline commit:
  - `c278a71 feat: complete M9 executable engineering knowledge proof`
- Practical implication:
  - M11 is still being developed on a stable post-M9 baseline with local M11 work in progress
  - Story `1.2` should remain additive and contract-first
  - the first file touches should stay in projection/runtime/electrical seams, not Theia-first code

### Latest Technical Information

- No extra web research is required for this story.
- The versions that matter are already frozen by local planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - Eclipse Theia `1.73.1` remains downstream product surface only

### Project Structure Notes

- `m11/` is the active milestone-local implementation artifact folder and should continue following the same convention as `m8/` and `m9/`.
- Keep naming explicit and easy to read:
  - `ElectricalSheetId`
  - `ElectricalSheet`
  - `ElectricalSheetMembership`
  - `ElectricalSheetOrder`
  - other sheet nouns only if the layer is genuinely needed
- Avoid putting `Ir` into new names unless the type actually belongs to a distinct IR layer that must be disambiguated.
- Prefer one dedicated sheet contract file over overloading an existing file into a mixed-responsibility dump if `ProjectionModel.kt` or runtime transport files would become hard to read.

### References

- [Source: _bmad-output/planning-artifacts/epics-M11-2026-07-11.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-11-m11/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-11-m11/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m11/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m11/1-1-publish-electrical-projection-family-contracts.md]
- [Source: kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt]
- [Source: kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionModel.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt]
- [Source: extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt]

## Story Completion Status

- Status: done
- Completion note: Added a governed projection-owned sheet model in `kernel/projection-model`, derived deterministic electrical sheet sets in compiler output, and surfaced additive sheet metadata through runtime and LSP without changing canonical engineering identity ownership.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test --tests com.engineeringood.athena.projection.ProjectionModelContractTest"`
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaRuntimeProjectionSessionTest"`
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest"`

### Completion Notes List

- Added `ProjectionSheetId`, `ProjectionSheetSubject`, and `ProjectionSheet` with KDoc under `kernel/projection-model`.
- Extended `ProjectionDocument` with additive `sheets` support.
- Derived deterministic sheet sets in `ProjectionModelDeriver`, including a two-sheet documentation view that reuses canonical subject identity across sheets.
- Extended runtime ready snapshots with `activeSheetId` and `sheets`.
- Extended LSP ready payloads with `activeSheetId` and `sheets`.
- Updated English and Chinese projection-model README files.

### File List

- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionModel.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt`
- `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionModelContractTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`
- `kernel/projection-model/README.md`
- `kernel/projection-model/README.zh-CN.md`

### Change Log

- `2026-07-12`: Implemented the first governed electrical sheet model, updated runtime/LSP transport seams, and verified targeted projection/runtime/LSP tests sequentially on Java 25.

- M11 epic breakdown, PRD, addendum, architecture spine, and sprint-status review
- Story `1.1` implementation artifact review for carry-forward seam and Windows verification learnings
- codegraph review of projection/runtime/LSP/electrical view-delivery seams around future sheet ownership

### Completion Notes List

- Derived Story `1.2` directly from the M11 epic, PRD, addendum, and architecture spine instead of inventing a new page model outside the approved milestone scope.
- Kept the story narrowly focused on the first governed sheet model and explicitly deferred notation packs, repeated-reference resolution, dense workbench polish, and UI redesign.
- Anchored the likely implementation seam in `kernel/projection-model` with additive runtime/LSP/electrical updates only if required for downstream delivery.
- Carried forward the Story `1.1` architectural rule that runtime and LSP remain consumers of governed downstream contracts, not semantic owners.

### File List

- _bmad-output/implementation-artifacts/m11/1-2-introduce-the-first-governed-electrical-sheet-model.md
- _bmad-output/implementation-artifacts/m11/sprint-status.yaml
- _bmad-output/implementation-artifacts/sprint-status.yaml

### Change Log

- 2026-07-12: Created the M11 Story 1.2 implementation artifact with governed sheet-model guardrails, carry-forward context from Story 1.1, and milestone-local sprint tracking.
