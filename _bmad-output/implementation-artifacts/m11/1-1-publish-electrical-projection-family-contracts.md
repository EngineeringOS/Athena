---
baseline_commit: c278a71
---

# Story 1.1: Publish Electrical Projection Family Contracts

Status: done

## Story

As a platform engineer,
I want Athena to define explicit electrical projection-family contracts above canonical engineering entities,
so that schematic, cabinet, wiring, and documentation views can exist without creating parallel semantic models.

## FR Traceability

- FR-1: support richer electrical projection families for the first ECAD domain
- FR-2: preserve one canonical semantic identity across richer electrical workbench views
- NFR-1: keep canonical engineering meaning upstream of sheets, symbols, notation, and renderer behavior
- NFR-2: keep subject identity stable across repeated views and downstream representations
- NFR-4: keep the downstream projection boundary inspectable for architecture and debugging
- NFR-6: preserve the existing runtime, LSP, and workbench seams instead of creating frontend-owned authority

## Acceptance Criteria

1. Given the completed M7 to M9 projection and runtime spine, when M11 introduces richer electrical view families, then Athena publishes explicit downstream contracts for at least schematic, cabinet, wiring, and documentation-oriented projection families, and those contracts resolve back to canonical engineering identities rather than view-local entities.
2. Given one engineering subject appears in more than one projection family, when reveal or inspection is requested, then Athena can identify the same canonical subject across those families, and no projection family becomes a second semantic authority.

## Tasks / Subtasks

- [x] Publish the M11 electrical projection-family contract layer in the kernel-owned projection boundary. (AC: 1, 2)
  - [x] Add one additive typed contract file under `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/` for M11 nouns such as `ElectricalProjectionFamily`, `ElectricalProjectionDescriptor`, or equivalent contract types that describe downstream electrical families without redefining canonical engineering meaning.
  - [x] Keep the contract layer additive beside existing `ProjectionDocument` and `ViewDefinition` structures. Do not collapse M11 concerns into ad hoc `String` ids only if a typed vocabulary is sufficient.
  - [x] Add clean KDoc for every new public/core Kotlin type introduced by this story.
- [x] Freeze the first governed electrical family vocabulary for M11. (AC: 1)
  - [x] Publish an explicit first family set covering at least:
    - schematic
    - cabinet
    - wiring
    - documentation
  - [x] Define what each family means as presentation intent only. Do not let family choice redefine engineering semantics, page ownership, or symbol truth.
  - [x] Keep the first vocabulary narrow to the first ECAD domain. Do not widen this story into multi-domain drafting or import/export compatibility work.
- [x] Extend the existing electrical view-definition contribution seam to attach the new family contracts. (AC: 1, 2)
  - [x] Update the electrical domain plugin view-definition publication so current and new M11-capable views expose the family contract through governed plugin contribution paths.
  - [x] Preserve deterministic approved-plugin ordering and existing `AthenaViewDefinitionContributor` behavior.
  - [x] Do not move family ownership into runtime payload classes or frontend-only adapters in this story.
- [x] Preserve canonical identity anchoring across families. (AC: 2)
  - [x] Ensure the new contract types and attached view-definition data make it explicit that one canonical engineering subject may participate in multiple electrical families without creating new semantic identities.
  - [x] Reuse existing `StableSemanticIdentity`, `ViewDefinition`, `ProjectionDocument`, and runtime projection-session seams where possible rather than inventing a second identity or alias model.
- [x] Verify the new contract layer through focused tests and module documentation. (AC: 1, 2)
  - [x] Add or extend focused tests under:
    - `kernel/projection-model/src/test/kotlin/...`
    - `kernel/runtime/src/test/kotlin/...` if runtime-hosted view-definition or projection-session expectations change
    - `extensions/domain-electrical/src/test/kotlin/...` if the domain plugin needs direct view-definition coverage
  - [x] Cover:
    - typed family contract publication
    - deterministic family ordering
    - canonical identity anchoring across multiple families
    - additive compatibility with current cabinet and wiring views
  - [x] Update affected module README files in English and Chinese if this story changes public core or extension contract surfaces.
  - [x] Run Gradle verification sequentially on Windows with Java 25; do not run build or test tasks concurrently.

## Dev Notes

### Story Intent

- Story `1.1` is the contract-publication entry story for M11.
- The success condition is not "Athena already has all serious ECAD views." The success condition is "Athena has one explicit, typed, governed contract for electrical projection families above canonical engineering meaning."
- Story `1.2` owns the first explicit governed sheet model.
- Story `1.3` owns governed symbol and notation packs.
- Story `1.4` owns runtime/LSP delivery of multi-view electrical outputs.
- Story `1.5` owns reveal and inspection coherence across delivered views.

### Architecture Guardrails

- Align to AD-53: electrical workbench depth starts from canonical engineering entities, not symbols or pages. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m11/ARCHITECTURE-SPINE.md#AD-53---Electrical-Workbench-Depth-Starts-From-Canonical-Engineering-Entities-Not-Symbols]
- Align to AD-54: M11 must introduce explicit electrical projection families above one canonical subject. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m11/ARCHITECTURE-SPINE.md#AD-54---M11-Introduces-Explicit-Electrical-Projection-Families-Above-One-Canonical-Subject]
- Preserve inherited AD-27 and AD-28: `kernel/projection-model` remains the renderer-neutral projection boundary and engineering identity remains outside downstream views. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m11/ARCHITECTURE-SPINE.md#Inherited-Invariants]
- Preserve AD-61: QElectroTech and EPLAN may influence vocabulary or workflow expectations, but they do not define Athena's semantic center. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m11/ARCHITECTURE-SPINE.md#AD-61---Product-References-Constrain-UX-And-Workflow-Not-Semantic-Ownership]

### Technical Requirements

- The current typed view-definition seam already exists in `kernel/layout-model`:
  - `ViewDefinition`
  - `ProjectionOwnershipContract`
  - `LayoutIntent`
  - `ViewEmphasis`
- The current downstream projection document already packages a `ViewDefinition` together with derived nodes, connections, and labels in `kernel/projection-model`.
- The current runtime projection session already exposes supported views through:
  - `AthenaRuntimeProjectionView`
  - `AthenaRuntimeProjectionSession`
  - `AthenaProjectionViewPayload`
- The current electrical domain plugin already contributes `cabinet` and `wiring` view definitions through `AthenaViewDefinitionContributor`.
- Story `1.1` should build the M11 family contract above those seams rather than bypassing them with:
  - runtime-only fields
  - frontend-only enums
  - hard-coded renderer branching
  - documentation-only naming with no typed contract surface

### Architecture Compliance

- The story is only successful if later M11 work can point to one clean ladder:
  - canonical `Engineering IR`
  - typed electrical projection-family contract
  - governed view definitions
  - downstream sheet / notation / repeated-reference depth
  - runtime and LSP delivery
- Prevent these failure modes:
  - encoding family meaning only in raw view ids like `"cabinet"` or `"wiring"` with no typed M11 contract
  - adding sheet or symbol semantics in Story `1.1`
  - pushing electrical family meaning into `AthenaProjectionProtocol` payloads before kernel/runtime contracts exist
  - letting renderer targets or surface mappings become the de facto family model

### Library / Framework Requirements

- Use the repo-approved stack already frozen by planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Reuse current Kotlin/JUnit test style already present in:
  - `kernel/projection-model`
  - `kernel/runtime`
  - `extensions/domain-electrical`
- Do not add third-party libraries just to model the family contract vocabulary.

### File Structure Requirements

- Expected update files:
  - `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionModel.kt` or one new sibling contract file under the same package
  - `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/`
  - `kernel/projection-model/README.md`
  - `kernel/projection-model/README.zh-CN.md`
  - `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`
- Possible narrow additive update files if the current view-definition type is the right host for the new family contract:
  - `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`
  - `kernel/layout-model/src/test/kotlin/com/engineeringood/athena/layout/`
  - `kernel/layout-model/README.md`
  - `kernel/layout-model/README.zh-CN.md`
- Possible verification-only update files if runtime expectations need to reflect the new family contract:
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt`
  - `ide/lsp/src/test/kotlin/...` only if payload expectations already cover supported-view metadata
- Files whose current behavior must be preserved:
  - [`kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionModel.kt`](../../../kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionModel.kt)
    - projection documents remain downstream and inspectable
  - [`kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`](../../../kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt)
    - view definitions remain presentation intent only
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt)
    - runtime session remains the owner of supported-view delivery
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt)
    - plugin view-definition contributions remain governed and deterministic
  - [`extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`](../../../extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt)
    - current cabinet and wiring proof must continue to work as additive first members of the richer M11 family model
- Explicit non-goals:
  - no sheet/page structure yet
  - no notation-pack implementation yet
  - no repeated-reference resolution yet
  - no runtime/LSP payload expansion beyond what is strictly needed to publish the new contract
  - no frontend/workbench UI redesign

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test"`
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test"`
- Recommended focused regression if runtime view-definition delivery changes:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:runtime:test"`
- Required proof checks:
  - at least four first-family contracts exist or are representable through the typed contract surface
  - current cabinet and wiring view definitions are preserved as valid governed views
  - canonical identity anchoring remains explicit
  - no renderer-local or page-local semantics are introduced
  - no concurrent Gradle build/test execution on Windows

### Current Code State To Preserve

- [`kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`](../../../kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt) currently defines `ViewDefinition` as typed presentation intent with layout intent, grouping rules, emphasis, description, and ownership contract, but no M11 family contract.
- [`kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionModel.kt`](../../../kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionModel.kt) currently packages one `ViewDefinition` into one `ProjectionDocument`; it does not yet expose an explicit electrical family concept.
- [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt) currently builds supported views from compiler-owned `supportedViewDefinitions()` and surfaces them through `AthenaRuntimeProjectionView`.
- [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt) currently gathers plugin-owned view definitions in deterministic approved-plugin order.
- [`extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`](../../../extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt) currently publishes only `cabinet` and `wiring` as the first proof pair; M11 should deepen this seam rather than bypass it.
- [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt) currently serializes supported views and ownership contracts only; do not let this protocol become the first owner of M11 semantics.

### Previous Milestone Intelligence

- M7 already proved the first projection contract boundary, runtime-owned projection sessions, and workbench delivery path. M11 should extend those proven seams instead of creating a second projection architecture.
- M8 already proved mutation authority and review anchoring across source and graph. Story `1.1` must stay upstream of mutation and review logic.
- M9 already proved executable engineering knowledge above canonical structure. Story `1.1` must not mix knowledge-runtime concerns into the projection-family contract layer.
- The repo conventions still matter directly here:
  - physical implementation artifacts must live in milestone-local `m*` folders
  - public/core Kotlin surfaces require KDoc
  - Java `25` and sequential Windows Gradle execution are non-negotiable

### Git Intelligence Summary

- Current baseline commit:
  - `c278a71 feat: complete M9 executable engineering knowledge proof`
- Practical implication:
  - M11 starts from a stable post-M9 kernel/runtime baseline
  - Story `1.1` should be additive and contract-first
  - the first file touches should stay in projection/layout/runtime/electrical seams, not frontend-only code

### Latest Technical Information

- No extra web research is required for this story.
- The versions that matter are already frozen by local planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - Eclipse Theia `1.73.1` for downstream product surfaces, but this story should not begin in Theia-owned code

### Project Structure Notes

- `m11/` is now the active milestone-local implementation artifact folder and should follow the same convention as `m8/` and `m9/`.
- Keep naming explicit and easy to read:
  - `ElectricalProjectionFamily`
  - `ElectricalProjectionDescriptor`
  - `DocumentationProjectionFamily`
  - other downstream family nouns as needed
- Avoid putting `Ir` into new names unless the type actually belongs to a distinct IR layer that must be disambiguated.
- Prefer a dedicated new contract file over overloading one existing file into a mixed-responsibility dump if the projection-model or layout-model file would become hard to read.

### References

- [Source: _bmad-output/planning-artifacts/epics-M11-2026-07-11.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-11-m11/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-11-m11/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m11/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/planning-artifacts/implementation-readiness-report-2026-07-11.md]
- [Source: _bmad-output/implementation-artifacts/README.md]
- [Source: _bmad-output/implementation-artifacts/m9/sprint-status.yaml]
- [Source: kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt]
- [Source: kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionModel.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt]
- [Source: extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt]

## Story Completion Status

- Status: done
- Completion note: Implemented typed governed electrical projection-family contracts, published the first four-family electrical view set, updated module documentation, and verified the touched kernel/runtime/LSP paths with sequential Java 25 runs.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M11 epic breakdown, PRD, addendum, architecture spine, readiness report, and root sprint-status review
- codegraph review of projection/runtime/view-definition seams and current electrical view contribution path
- implementation-artifact layout review against existing milestone-local folders `m0/` through `m9/`
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:layout-model:test"` (expected red-phase compile failure before contract implementation)
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:layout-model:test :kernel:projection-model:test :extensions:domain-electrical:test"` (green targeted kernel and extension verification)
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest"` (targeted LSP verification)
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaRuntimeProjectionSessionTest"` (targeted runtime projection-session verification)
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaPluginRuntimeServicesTest"` (targeted hosted runtime verification)
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:runtime:test"` (attempted full runtime-module regression; did not complete within local timeout window)

### Completion Notes List

- Standardized M11 implementation planning into the milestone-local `m11/` folder instead of the implementation-artifacts root.
- Identified the primary contract seam for Story `1.1` as the existing `ViewDefinition` / `ProjectionDocument` / plugin view-definition path.
- Isolated the downstream surfaces that must remain consumers, not owners: runtime projection sessions and the Athena LSP projection protocol.
- Kept Story `1.1` narrowly contract-first and explicitly deferred sheets, notation packs, repeated references, and frontend-heavy work to later stories.
- Added typed projection-family contracts to `:kernel:layout-model`, including explicit electrical-family vocabulary and canonical identity / semantic-authority anchoring.
- Extended `ElectricalRuntimeDomainPlugin` to publish deterministic `cabinet`, `wiring`, `schematic`, and `documentation` view definitions while preserving current cabinet-first behavior.
- Added focused contract and regression coverage in layout-model, projection-model, runtime, electrical extension, and LSP tests for family publication, ordering, and canonical identity anchoring across families.
- Updated English and Chinese module READMEs for `:kernel:layout-model`, `:kernel:projection-model`, and `:extensions:domain-electrical`, including a UTF-8 repair of the projection-model Chinese README.
- Verified the touched kernel, extension, runtime, and LSP paths sequentially under Java 25; one full `:kernel:runtime:test` attempt was also made but exceeded the local timeout window after the targeted runtime classes had already passed.

### File List

- _bmad-output/implementation-artifacts/m11/1-1-publish-electrical-projection-family-contracts.md
- _bmad-output/implementation-artifacts/m11/sprint-status.yaml
- _bmad-output/implementation-artifacts/m11/README.md
- _bmad-output/implementation-artifacts/sprint-status.yaml
- extensions/domain-electrical/README.md
- extensions/domain-electrical/README.zh-CN.md
- extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt
- extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPluginTest.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt
- kernel/layout-model/README.md
- kernel/layout-model/README.zh-CN.md
- kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt
- kernel/layout-model/src/test/kotlin/com/engineeringood/athena/layout/LayoutModelTest.kt
- kernel/projection-model/README.md
- kernel/projection-model/README.zh-CN.md
- kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionModelContractTest.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServicesTest.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt

### Change Log

- 2026-07-12: Implemented M11 Story 1.1 by publishing typed governed electrical projection-family contracts, extending the electrical view-definition family set to four deterministic views, updating module documentation, and verifying targeted kernel/runtime/LSP regressions under Java 25.
