---
baseline_commit: ad382d8a2d1841771c5b95e008f29f78a6f751cd
---

# Story 1.2: Contribute The First Supported View Definitions From The Electrical Extension

Status: done

## Story

As a platform engineer,
I want the electrical extension to contribute the first supported `cabinet` and `wiring` view definitions through typed contracts,
so that M2 proves multi-view behavior without hard-coding view logic into the desktop application.

## Acceptance Criteria

1. Given explicit `ViewDefinition` contracts exist in `:kernel:layout-model`, when the electrical extension is updated for M2, then it contributes the first two supported view definitions, `cabinet` and `wiring`, and both view definitions are registered through typed extension contracts rather than private UI code.
2. Given the first supported view definitions are contributed by an extension, when the runtime and compiler inspect those contributions, then the view definitions can declare layout intent, grouping rules, and view emphasis, and they are not allowed to redefine engineering meaning or canonical semantic ownership.
3. Given the first proof pair is fixed for M2, when project planning fixtures and extension registration are reviewed, then `cabinet` plus `wiring` is the explicit proof pair used by the milestone, and other candidate view families remain deferred beyond the first proof.
4. Given extension-contributed view definitions are implemented, when the standard Java `25` build and plugin checks are executed, then the workspace builds successfully and both supported view definitions are discoverable through approved contracts, and the implementation preserves non-sovereign extension boundaries.

## Tasks / Subtasks

- [x] Add the typed plugin contribution seam for supported view definitions. (AC: 1, 2, 4)
  - [x] Add a compiler-owned typed plugin contract for contributed `ViewDefinition` values.
  - [x] Add the necessary manifest extension-point vocabulary and validator allowance for view-definition contributions.
  - [x] Keep the contract discoverable by runtime without making the desktop UI the registration owner.
- [x] Make `ViewDefinition` rich enough for the first proof pair without turning it into a semantic model. (AC: 1, 2, 3)
  - [x] Extend `ViewDefinition` to carry layout intent, grouping rules, and view emphasis metadata.
  - [x] Keep the metadata presentation-oriented and deterministic; do not encode canonical semantics into it.
  - [x] Preserve Story `1.1` boundaries: no layout derivation or geometry derivation in this story.
- [x] Update the electrical extension to contribute the first supported proof pair. (AC: 1, 2, 3, 4)
  - [x] Make `ElectricalRuntimeDomainPlugin` implement the new typed view-definition contribution contract.
  - [x] Contribute exactly the `cabinet` and `wiring` definitions as the M2 proof pair.
  - [x] Keep other candidate view families deferred and undocumented as active contributions.
- [x] Expose and test contribution inspection through existing compiler/runtime seams. (AC: 2, 4)
  - [x] Add compiler-side tests proving the plugin contract remains valid and the electrical plugin publishes the expected view definitions.
  - [x] Add runtime-side tests proving the hosted plugin inventory or contribution APIs can inspect the contributed definitions deterministically.
  - [x] Keep plugin-governance checks non-sovereign and explicit.
- [x] Update module documentation and preserve M1 behavior. (AC: 3, 4)
  - [x] Update `:extensions:domain-electrical` docs to mention view-definition contributions.
  - [x] Update any kernel docs needed to reflect the new contribution seam.
  - [x] Do not refactor the current runtime viewer-inspector contribution path or the `Engineering IR -> SvgRenderModel` pipeline in this story.

## Dev Notes

### Story Intent

- Story `1.2` proves that supported view definitions are extension-owned contributions over core-owned contracts, not hard-coded desktop state.
- Story `1.3` will derive `Layout IR` from these definitions. Story `1.2` must stop earlier and only establish the contribution seam plus the first proof pair.
- The success condition is not “many views forever.” It is “the electrical extension can contribute exactly `cabinet` and `wiring` through typed contracts that runtime and compiler can inspect.”

### Architecture Guardrails

- Align to AD-4: supported `ViewDefinition` contracts are contributed by extensions and may declare layout intent, grouping rules, emphasis, and presentation policy only.
- Align to AD-5: contributed view definitions must not redefine canonical semantic identity or engineering ownership.
- Align to AD-8: durable `ViewDefinition` data stays in `:kernel:layout-model`; plugin contract vocabulary stays in core-owned plugin boundaries.
- Preserve current M1 invariants:
  - `Engineering IR` remains the only semantic truth.
  - commands remain the only semantic mutation path.
  - UI is not the owner of view registration.

### Technical Requirements

- Prefer a compiler-owned typed plugin contract that returns `ViewDefinition` values from `:kernel:layout-model`.
- Keep view-definition metadata deterministic and small. A good first slice is:
  - layout intent
  - grouping rules
  - view emphasis
- Keep the electrical proof pair explicit:
  - `cabinet`
  - `wiring`
- Do not add runtime projection sessions, view switching, layout derivation, geometry derivation, or renderer branching here.
- Keep all newly introduced core Kotlin classes documented with KDoc.

### Architecture Compliance

- The extension contribution seam must remain core-owned and typed.
- The electrical extension may contribute view intent metadata, but not canonical semantic meaning.
- Runtime may inspect contributed definitions, but it must not privately invent or normalize new view families behind the plugin.
- This story should make Story `1.3` possible without already performing Story `1.3`.

### Library / Framework Requirements

- Use the existing repo-pinned stack:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Keep dependency changes minimal and local to the affected modules.
- Reuse the current Kotlin/JUnit test approach.

### File Structure Requirements

- Likely update files:
  - `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`
  - `kernel/compiler/build.gradle.kts`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginContracts.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginManifestModel.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginValidator.kt`
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginContractTest.kt`
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginTestFixtures.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServicesTest.kt`
  - `extensions/domain-electrical/build.gradle.kts`
  - `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`
  - `extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPluginTest.kt`
  - `extensions/domain-electrical/README.md`
  - `extensions/domain-electrical/README.zh-CN.md`
- Preserve current behavior in:
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SvgRenderModelDeriver.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjection.kt`
  - `ui/compose-workbench/**`

### Testing Requirements

- Minimum verification commands:
  - `java25`
  - `.\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test :kernel:runtime:test`
  - `.\gradlew.bat --no-daemon --console=plain build`
- Required proof tests:
  - electrical plugin publishes exactly `cabinet` and `wiring`
  - contributed definitions expose layout-intent metadata instead of semantic ownership
  - runtime can inspect contributed definitions deterministically
  - plugin manifest and validator rules remain green with the new extension point
- Keep Gradle verification sequential on Windows.

### Previous Story Intelligence

- Story `1.1` established `:kernel:layout-model` and `:kernel:geometry-model` as durable homes, so Story `1.2` must use those model boundaries instead of reintroducing projection shapes in runtime or UI.
- The repo already enforces:
  - grouped module layout
  - Java `25`
  - KDoc on core Kotlin classes
  - English and Chinese README coverage for core modules
- Do not broaden the M1 runtime viewer-inspector seam into a full projection session API in this story.

### References

- `_bmad-output/planning-artifacts/epics-M2-2026-07-06.md`
  - `Story 1.2: Contribute The First Supported View Definitions From The Electrical Extension`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-06/prd.md`
  - `FR-4`, `FR-5`, `FR-6`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-06/ARCHITECTURE-SPINE.md`
  - `AD-4`
  - `AD-5`
  - `AD-8`
- `_bmad-output/implementation-artifacts/m2/1-1-establish-explicit-layout-and-geometry-model-modules.md`
- `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginContracts.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginManifestModel.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginValidator.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`

## Story Completion Status

- Status: done
- Completion note: Added the typed view-definition contribution seam, published the electrical `cabinet` and `wiring` proof pair, and verified discoverability through compiler/runtime tests without changing the current M1 render path.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Initial implementation verification:
  - `java25; .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test :kernel:compiler:test :kernel:runtime:test`
  - First pass failed in `AthenaRuntimeTest` because one direct `AthenaPluginRuntimeServices` test stub needed the new `viewDefinitionContributions()` method.
- Follow-up fix:
  - Updated the `AthenaRuntimeTest` stub to implement `viewDefinitionContributions()`.
- Final verification:
  - `java25; .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test :kernel:compiler:test :kernel:runtime:test`
  - `java25; .\gradlew.bat --no-daemon --console=plain build`

### Completion Notes List

- Extended `ViewDefinition` with explicit layout intent, grouping rules, and view-emphasis metadata for the first proof pair.
- Added a compiler-owned typed plugin contract for contributed view definitions and the new `VIEW_DEFINITIONS` manifest extension point.
- Updated plugin-governance validation so domain plugins may declare `VIEW_DEFINITIONS`, and hosted runtime services reject undeclared or unimplemented view-definition contracts.
- Updated `ElectricalRuntimeDomainPlugin` to contribute exactly the `cabinet` and `wiring` view definitions.
- Exposed contributed view definitions through runtime-hosted plugin inspection and covered the new seam with compiler, runtime, and extension tests.
- Preserved the current `Engineering IR -> SvgRenderModel` compiler path and the existing runtime inspector-view contribution path.

## File List

- `_bmad-output/implementation-artifacts/m2/1-2-contribute-the-first-supported-view-definitions-from-the-electrical-extension.md`
- `_bmad-output/implementation-artifacts/m2/sprint-status.yaml`
- `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`
- `kernel/compiler/build.gradle.kts`
- `kernel/runtime/build.gradle.kts`
- `extensions/domain-electrical/build.gradle.kts`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginManifestModel.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginContracts.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginValidator.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`
- `extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPluginTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginContractTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServicesTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeTest.kt`
- `extensions/domain-electrical/README.md`
- `extensions/domain-electrical/README.zh-CN.md`

## Change Log

- 2026-07-06: Added typed electrical view-definition contributions for `cabinet` and `wiring`, with compiler/runtime inspection and governance coverage.
