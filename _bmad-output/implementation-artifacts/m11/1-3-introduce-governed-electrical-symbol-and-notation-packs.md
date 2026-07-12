---
baseline_commit: c278a71
---

# Story 1.3: Introduce Governed Electrical Symbol And Notation Packs

Status: done

## Story

As a platform engineer,
I want Athena to define governed symbol and notation-pack contracts,
so that electrical symbol choice, labels, and markers stay inspectable, reusable, and downstream of semantics.

## FR Traceability

- FR-4: support the first governed electrical symbol and notation pack boundary
- FR-2: preserve one canonical semantic identity across richer electrical workbench views
- NFR-1: keep canonical engineering meaning upstream of sheet, symbol, notation, and renderer behavior
- NFR-2: keep subject identity stable across repeated views, sheets, symbols, and downstream representations
- NFR-4: keep the boundary between engineering entity, projection rule, sheet structure, and notation pack inspectable
- NFR-6: preserve the existing runtime, LSP, and workbench seams instead of creating frontend-owned authority

## Acceptance Criteria

1. Given canonical electrical entities and projection-family outputs, when Athena selects symbols, labels, or notation behavior, then those choices come from governed notation-pack contracts rather than renderer-local hardcoding, and the resulting mappings remain inspectable and extension-compatible.
2. Given notation behavior is reviewed for architectural drift, when the boundary is inspected, then symbol choice and labels remain downstream presentation rules, and engineering truth continues to live in canonical semantic state.

## Tasks / Subtasks

- [ ] Publish the first governed electrical notation-pack contract layer in the kernel-owned projection boundary. (AC: 1, 2)
  - [ ] Add additive typed notation-pack nouns under `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/` or another clearly justified downstream package for M11 concepts such as notation pack id, subject mapping, symbol token, label policy, or equivalent downstream contracts.
  - [ ] Keep notation ownership downstream and projection-owned. Do not attach symbol truth or label truth directly to `Engineering IR`.
  - [ ] Add clean KDoc for every new public/core Kotlin type introduced by this story.
- [ ] Freeze the first narrow electrical notation vocabulary for M11. (AC: 1)
  - [ ] Define only the first serious ECAD notation semantics needed now:
    - stable notation-pack identity
    - family-scoped symbol selection
    - governed label policy or label variant
    - inspectable marker or presentation token support where needed
  - [ ] Keep the vocabulary narrow to the first electrical ECAD path. Do not widen this story into a full symbol-library product, import/export pipeline, or unrestricted user-authored notation system.
  - [ ] Make it explicit that symbol choice and labels are downstream presentation rules, not engineering meaning.
- [ ] Attach the notation-pack model to the M11 electrical family path. (AC: 1, 2)
  - [ ] Update the electrical projection-facing seam so at least one governed notation pack can publish inspectable notation mappings for the existing electrical family set.
  - [ ] Reuse the M11 family-contract and sheet-contract path from Stories `1.1` and `1.2`; do not invent a second electrical taxonomy for symbols.
  - [ ] Preserve deterministic ordering and current runtime-supported view behavior while extending the model additively.
- [ ] Preserve canonical subject anchoring under notation selection. (AC: 2)
  - [ ] Ensure one canonical engineering subject can appear with different downstream symbol or label variants without creating new semantic identities.
  - [ ] Keep reveal, inspection, and later cross-reference paths capable of resolving notation output back to canonical `StableSemanticIdentity`.
  - [ ] Do not let symbol ids, label variants, or renderer tokens become the first owner of subject resolution in this story.
- [ ] Surface the first notation-pack model through current runtime and transport seams only as needed. (AC: 1, 2)
  - [ ] Extend runtime-owned projection/session or downstream payload contracts only where required to publish governed notation data.
  - [ ] Keep runtime and `ide/lsp` as consumers and transport owners, not semantic owners, of notation meaning.
  - [ ] Do not redesign the frontend/workbench in this story.
- [ ] Verify the notation-pack contract layer through focused tests and module documentation. (AC: 1, 2)
  - [ ] Add or extend focused tests under:
    - `kernel/projection-model/src/test/kotlin/...`
    - `kernel/compiler/src/test/kotlin/...` if notation selection changes derived projection output
    - `kernel/runtime/src/test/kotlin/...` if runtime-owned projection/session expectations change
    - `ide/lsp/src/test/kotlin/...` if notation-aware projection payloads change
    - `extensions/domain-electrical/src/test/kotlin/...` if the electrical plugin or electrical downstream view model publishes the first notation vocabulary directly
  - [ ] Cover:
    - stable notation-pack identity
    - deterministic subject-to-symbol mappings
    - canonical subject anchoring under notation variants
    - additive compatibility with the current electrical family and sheet path
    - explicit separation between notation identity and engineering identity
  - [ ] Update affected module README files in English and Chinese if this story changes public core or extension contract surfaces.
  - [ ] Run Gradle verification sequentially on Windows with Java 25; do not run build or test tasks concurrently.

## Dev Notes

### Story Intent

- Story `1.3` is the notation-contract entry story for M11.
- The success condition is not "Athena already has a full symbol library product." The success condition is "Athena has one explicit, typed, governed notation-pack model that stays downstream of canonical engineering meaning."
- Story `1.1` already introduced governed electrical projection-family contracts and the first four-family electrical vocabulary.
- Story `1.2` already introduced the first governed electrical sheet model.
- Story `1.4` owns richer runtime/LSP delivery of multi-view electrical outputs.
- Story `1.5` owns reveal and inspection coherence across delivered views.
- Epic `2.x` owns repeated references, dense proof fixtures, and denser workbench interaction.

### Architecture Guardrails

- Align to AD-56: symbol and notation packs are governed downstream contracts. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m11/ARCHITECTURE-SPINE.md#AD-56---Symbol-And-Notation-Packs-Are-Governed-Downstream-Contracts]
- Align to AD-53: electrical workbench depth starts from canonical engineering entities, not symbols. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m11/ARCHITECTURE-SPINE.md#AD-53---Electrical-Workbench-Depth-Starts-From-Canonical-Engineering-Entities-Not-Symbols]
- Align to AD-54 and AD-55: notation must compose with the existing electrical family and sheet path rather than replace them. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m11/ARCHITECTURE-SPINE.md#AD-54---M11-Introduces-Explicit-Electrical-Projection-Families-Above-One-Canonical-Subject] [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m11/ARCHITECTURE-SPINE.md#AD-55---Sheet-Identity-Is-Projection-Owned-And-Separate-From-Engineering-Identity]
- Preserve inherited AD-27, AD-28, and AD-29: `kernel/projection-model` remains the renderer-neutral projection boundary, engineering identity remains outside downstream views, and layout/geometry stay view-scoped metadata. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m11/ARCHITECTURE-SPINE.md#Inherited-Invariants]
- Preserve AD-59 and AD-60: notation-aware depth must remain additive through existing runtime/LSP seams and stay coherent with mutation, review, and knowledge paths. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m11/ARCHITECTURE-SPINE.md#AD-59---Workbench-Density-Remains-Additive-Through-Existing-Runtime-And-LSP-Seams] [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m11/ARCHITECTURE-SPINE.md#AD-60---Electrical-Workbench-Depth-Must-Preserve-Mutation-Review-And-Knowledge-Coherence]

### Technical Requirements

- Story `1.1` already added typed projection-family contracts on the `ViewDefinition` seam in `kernel/layout-model`.
- Story `1.2` already extended `ProjectionDocument` with governed sheet support and additive runtime/LSP delivery.
- The current downstream projection document still packages one `ViewDefinition` with nodes, connections, labels, and sheets in `kernel/projection-model`.
- The current electrical domain plugin now contributes:
  - `cabinet`
  - `wiring`
  - `schematic`
  - `documentation`
  through `AthenaViewDefinitionContributor`.
- Story `1.3` should introduce the first explicit notation-pack model above those seams rather than bypassing them with:
  - renderer-local symbol tables
  - frontend-only label heuristics
  - free-form token maps with no typed contract surface
  - ad hoc symbol ids that make subject mapping opaque

### Architecture Compliance

- The story is only successful if later M11 work can point to one clean ladder:
  - canonical `Engineering IR`
  - typed electrical projection-family contract
  - typed governed electrical sheet model
  - typed governed notation pack
  - runtime and LSP delivery
- Prevent these failure modes:
  - treating symbol ids or label variants as canonical engineering ids
  - storing notation meaning only in render contributions, CSS tokens, or frontend widgets
  - letting one symbol variant redefine the meaning of a subject
  - pushing the first notation-pack model directly into renderer or Theia-owned payloads before kernel/runtime contracts exist
  - widening Story `1.3` into repeated references, dense workbench polish, or full symbol-library authoring

### Library / Framework Requirements

- Use the repo-approved stack already frozen by planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Reuse current Kotlin/JUnit test style already present in:
  - `kernel/projection-model`
  - `kernel/compiler`
  - `kernel/runtime`
  - `ide/lsp`
  - `extensions/domain-electrical`
- Do not add third-party libraries just to model the notation vocabulary.

### File Structure Requirements

- Expected update files:
  - `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/`
  - `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/`
  - `kernel/projection-model/README.md`
  - `kernel/projection-model/README.zh-CN.md`
- Likely additive update files if notation selection changes derived projection output:
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt`
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- Likely additive update files if runtime-supported view or snapshot contracts need to surface notation data:
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/`
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`
- Likely additive update files if the electrical extension publishes the first narrow notation vocabulary directly:
  - `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/`
  - `extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPluginTest.kt`
  - `extensions/domain-electrical/README.md`
  - `extensions/domain-electrical/README.zh-CN.md`
- Explicit non-goals:
  - no repeated-reference implementation yet
  - no dense workbench UX redesign
  - no import/export symbol-library compatibility layer
  - no unrestricted user-authored notation editor

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test"`
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test"`
- Recommended focused regressions if notation-aware derived output or transport changes:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaCompilerTest"`
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaRuntimeProjectionSessionTest"`
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest"`
- Required proof checks:
  - at least one first notation pack exists with stable typed identity and deterministic symbol selection
  - the same canonical subject can appear with notation variants without changing semantic identity
  - current electrical family and sheet behavior remains additive and deterministic
  - notation identity remains explicit and distinct from engineering identity
  - no concurrent Gradle build/test execution on Windows

### Current Code State To Preserve

- `kernel/layout-model` already defines typed projection-family contracts on `ViewDefinition`, including `ElectricalProjectionDescriptor`.
- `kernel/projection-model` now packages one `ViewDefinition` into one `ProjectionDocument` and includes governed sheet support.
- `kernel/runtime` now builds supported views from compiler-owned `supportedViewDefinitions()` and can surface sheet-aware contracts.
- `ide/lsp` now serializes supported views, ownership contracts, active projection data, and additive sheet-aware projection payloads.
- `extensions/domain-electrical` now publishes four electrical projection families and remains the likely first contribution point for a narrow notation-aware electrical representation.

### Previous Story Intelligence

- Story `1.1` proved the right M11 seam: typed downstream contracts belong on the kernel-owned projection/view-definition path, while runtime and LSP remain consumers.
- Story `1.2` proved the next seam: governed sheet contracts belong in the projection boundary and flow outward additively through compiler, runtime, and LSP.
- Story `1.2` also reinforced the Windows verification rule: all Gradle build and test execution in this repo must stay strictly sequential.
- Recent cleanup established the Kotlin file-organization rule in `AGENTS.md`: split large mixed-responsibility files by role, but do not explode tiny related types into one-file-per-class.

### Git Intelligence Summary

- Current baseline commit:
  - `c278a71 feat: complete M9 executable engineering knowledge proof`
- Practical implication:
  - M11 is still being developed on a stable post-M9 baseline with local M11 work in progress.
  - Story `1.3` should remain additive and contract-first.
  - The first file touches should stay in projection/compiler/runtime/electrical seams, not Theia-first code.

### Latest Technical Information

- No extra web research is required for this story.
- The versions that matter are already frozen by local planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - Eclipse Theia `1.73.1` remains downstream product surface only

### Project Structure Notes

- `m11/` is the active milestone-local implementation artifact folder and should continue following the same convention as earlier milestones.
- Keep naming explicit and easy to read:
  - `ProjectionNotationPackId`
  - `ProjectionNotationPack`
  - `ProjectionNotationSubject`
  - `ProjectionSymbolKey`
  - `ProjectionLabelPolicy`
  - or equivalent downstream nouns only if genuinely needed
- Avoid putting `Ir` into new names unless the type actually belongs to a distinct IR layer that must be disambiguated.
- Prefer a few cohesive files grouped by role over one giant mixed file or one-file-per-tiny-type explosion.

### References

- [Source: _bmad-output/planning-artifacts/epics-M11-2026-07-11.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-11-m11/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-11-m11/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m11/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m11/1-1-publish-electrical-projection-family-contracts.md]
- [Source: _bmad-output/implementation-artifacts/m11/1-2-introduce-the-first-governed-electrical-sheet-model.md]

## Story Completion Status

- Status: done
- Completion note: Added governed electrical notation-pack contracts, deterministic subject-to-symbol mappings, and additive runtime/LSP delivery without moving notation ownership into renderer or frontend state.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M11 epic breakdown, PRD, addendum, architecture spine, and sprint-status review
- Story `1.1` and `1.2` implementation artifact review for carry-forward seam and Windows verification learnings
- recent code review of projection/runtime/LSP/electrical seams after sheet-model implementation and file-organization refactor

### Completion Notes List

- Derived Story `1.3` directly from the M11 epic, PRD, addendum, and architecture spine instead of inventing a renderer-local symbol layer.
- Kept the story narrowly focused on the first governed notation-pack model and explicitly deferred repeated references, dense workbench polish, full symbol-library authoring, and UI redesign.
- Anchored the likely implementation seam in `kernel/projection-model` with additive compiler/runtime/LSP/electrical updates only if required for downstream delivery.
- Carried forward the Story `1.1` and `1.2` architectural rule that runtime and LSP remain consumers of governed downstream contracts, not semantic owners.

### File List

- `_bmad-output/implementation-artifacts/m11/1-3-introduce-governed-electrical-symbol-and-notation-packs.md`
- `_bmad-output/implementation-artifacts/m11/sprint-status.yaml`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`

### Change Log

- `2026-07-12`: Created the M11 Story 1.3 implementation artifact with governed notation-pack guardrails, carry-forward context from Stories 1.1 and 1.2, and milestone-local sprint tracking.
