---
baseline_commit: ad382d8a2d1841771c5b95e008f29f78a6f751cd
---

# Story 1.3: Derive Layout IR For Cabinet And Wiring Views

Status: done

## Story

As a platform engineer,
I want `:kernel:compiler` to derive `Layout IR` for the `cabinet` and `wiring` views from one `Engineering IR` source,
so that Athena can express different human-facing arrangements without forking semantic truth.

## Acceptance Criteria

1. Given an active semantic project that compiles into canonical `Engineering IR`, when projection derivation is requested for the supported `cabinet` or `wiring` view, then `:kernel:compiler` derives a distinct `Layout IR` for that view from canonical semantics, and the derivation path is explicit and deterministic for the same semantic snapshot and view definition.
2. Given `Layout IR` represents view intent rather than exact geometry, when layout artifacts are inspected, then they can represent grouping, ordering, relative placement, and view-specific emphasis, and they do not need to encode final renderer primitives or recover missing semantic meaning.
3. Given two supported views are derived from the same semantic project, when the resulting layout artifacts are compared, then both layouts reference the same canonical semantic identities, and differences between them are layout intent differences rather than separate semantic realities.
4. Given the compiler-owned layout derivation path is implemented, when the standard Java `25` build and regression checks are executed, then the workspace builds successfully and both supported view definitions can produce deterministic `Layout IR`, and the implementation demonstrates one canonical semantic source with multiple derived layouts.

## Tasks / Subtasks

- [x] Enrich the durable `Layout IR` contract so it can express layout intent rather than just flat nodes. (AC: 2, 3)
  - [x] Add explicit layout types for grouping, ordering, relative placement, and view-specific emphasis while keeping canonical semantic identity first-class.
  - [x] Keep the model downstream-only: no renderer primitives, coordinates, or semantic reinterpretation.
  - [x] Document every new core Kotlin layout type with KDoc.
- [x] Add a compiler-owned layout derivation seam for supported view definitions. (AC: 1, 4)
  - [x] Add deterministic compiler logic that derives `Layout IR` from `Engineering IR` plus a supported `ViewDefinition`.
  - [x] Keep derivation in `:kernel:compiler` rather than runtime, UI, or renderer code.
  - [x] Expose the supported derived layouts through compiler-owned APIs that later stories can reuse.
- [x] Derive distinct `cabinet` and `wiring` layouts from one semantic source. (AC: 1, 2, 3)
  - [x] Make the `cabinet` layout emphasize structural ownership and placement.
  - [x] Make the `wiring` layout emphasize connectivity and signal flow.
  - [x] Preserve the same canonical semantic identities across both layouts.
- [x] Cover the new derivation path with deterministic regression tests. (AC: 1, 3, 4)
  - [x] Add or update layout-model tests for the richer contract shapes.
  - [x] Add compiler tests that prove `cabinet` and `wiring` layouts derive from the same semantic example and remain deterministic across repeated runs.
  - [x] Preserve the current M1 runtime viewer and SVG path while adding the explicit layout stage.
- [x] Update module documentation for the new compiler-owned layout stage. (AC: 4)
  - [x] Update `:kernel:layout-model` docs to describe the richer layout contract.
  - [x] Update compiler docs if needed to explain that explicit layout derivation now exists before geometry work begins.
  - [x] Do not claim `Geometry IR`, view switching, or runtime projection sessions in this story.

## Dev Notes

### Story Intent

- Story `1.3` is the first real proof that M2 has an explicit layout stage rather than a viewer-local arrangement.
- The success condition is not “full projection runtime.” It is “the compiler can deterministically derive two different layout-intent artifacts from one canonical semantic document.”
- Story `1.4` will introduce `Geometry IR`. Story `1.3` must stop at `Layout IR`.

### Architecture Guardrails

- Align to the manifesto and `manifesto/docs/architecture/09-layout-and-geometry.md`: `Engineering IR` answers what the system is, `Layout IR` answers how humans want to see it, and geometry is still deferred.
- Align to AD-2: deterministic projection derivation remains compiler-owned.
- Align to AD-4: supported view definitions are typed extension contributions and may only declare presentation intent.
- Align to AD-5: canonical semantic identity must survive across layout artifacts.
- Align to AD-8: durable layout contracts live in `:kernel:layout-model`; runtime and UI are consumers, not derivation owners.

### Technical Requirements

- Keep the current `Engineering IR -> SvgRenderModel` path working for M1 compatibility; do not move runtime viewer ownership in this story.
- Add an explicit compiler-owned layout derivation stage and expose it through compiler APIs or compiler success results.
- Derive layout from the same canonical semantic document for both supported view definitions:
  - `cabinet`
  - `wiring`
- Make the layouts meaningfully different by intent:
  - `cabinet` emphasizes ownership / structural placement
  - `wiring` emphasizes connectivity / signal flow
- Layout artifacts must stay deterministic for the same semantic input and view definition.
- Keep all newly introduced core Kotlin classes documented with KDoc.

### Architecture Compliance

- `Layout IR` must remain downstream of semantic truth and upstream of future geometry derivation.
- The compiler may interpret supported `ViewDefinition` metadata, but it may not treat plugins as semantic authorities.
- Runtime may inspect derived layouts later, but it must not own the derivation rules here.
- Do not introduce geometry, desktop view switching, runtime projection sessions, or incremental layout refresh in this story.

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
  - `kernel/layout-model/src/test/kotlin/com/engineeringood/athena/layout/LayoutModelTest.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
  - `kernel/layout-model/README.md`
  - `kernel/layout-model/README.zh-CN.md`
- Likely add files:
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/LayoutIrDeriver.kt`
- Preserve current behavior in:
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjection.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SvgRenderModelDeriver.kt`
  - `ui/compose-workbench/**`

### Testing Requirements

- Minimum verification commands:
  - `java25`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:layout-model:test :kernel:compiler:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
  - `.\gradlew.bat --no-daemon --console=plain build`
- Required proof tests:
  - repeated derivation for the same semantic example and view definition yields identical `Layout IR`
  - the compiler exposes both supported layout projections for the same canonical semantic document
  - `cabinet` and `wiring` differ by layout intent while preserving canonical semantic identity references
  - the current runtime viewer path still works after the layout stage is introduced
- Keep Gradle verification sequential on Windows.

### Previous Story Intelligence

- Story `1.1` established the durable module boundaries. Use them instead of inventing projection types in compiler internals.
- Story `1.2` established that `cabinet` and `wiring` are extension-contributed typed view definitions with layout intent, grouping rules, and view emphasis metadata.
- The repo already enforces:
  - grouped module layout
  - Java `25`
  - KDoc on core Kotlin classes
  - English and Chinese README coverage for core modules
- Keep the current M1 SVG/runtime proof alive while adding the explicit layout stage.

### Git Intelligence Summary

- `ad382d8 Complete M1 runtime workspace and regroup modules` is the current baseline that M2 extends.
- The recent M2 stories have added durable kernel modules first, then typed extension seams second. Story `1.3` should continue that pattern by adding compiler-owned derivation third.

### References

- `_bmad-output/planning-artifacts/epics-M2-2026-07-06.md`
  - `Story 1.3: Derive Layout IR For Cabinet And Wiring Views`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-06/prd.md`
  - `FR-1`
  - `FR-3`
  - `FR-4`
  - `NFR-1`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-06/ARCHITECTURE-SPINE.md`
  - `AD-2`
  - `AD-4`
  - `AD-5`
  - `AD-8`
- `manifesto/docs/architecture/09-layout-and-geometry.md`
- `_bmad-output/implementation-artifacts/m2/1-2-contribute-the-first-supported-view-definitions-from-the-electrical-extension.md`
- `examples/m0/demo-cabinet.athena`
- `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerRenderingModel.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SvgRenderModelDeriver.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`

## Story Completion Status

- Status: done
- Completion note: Added explicit compiler-owned `Layout IR` derivation for `cabinet` and `wiring`, enriched the durable layout contract, and verified deterministic projections plus existing runtime/build compatibility under Java `25`.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story context creation baseline:
  - `git rev-parse HEAD`
- Targeted implementation verification:
  - `java25; .\gradlew.bat --no-daemon --console=plain :kernel:layout-model:test :kernel:compiler:test`
  - `java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
- Full regression verification:
  - `java25; .\gradlew.bat --no-daemon --console=plain build`

### Completion Notes List

- Expanded `:kernel:layout-model` so `Layout IR` can represent groups, ordering, relative placement, and layout-owned relationships while preserving canonical semantic identity.
- Added `LayoutIrDeriver` and compiler APIs that derive supported layouts from canonical `Engineering IR` using extension-contributed `ViewDefinition` metadata.
- `CompilerCompilationSuccess` now exposes derived layouts alongside the existing rendering result without breaking the current runtime viewer path.
- Added deterministic compiler tests for the `cabinet` and `wiring` proof pair and kept the existing SVG/runtime pipeline green under Java `25`.
- Updated English and Chinese module documentation for the layout-model and compiler modules to describe the new explicit layout stage.

## File List

- `_bmad-output/implementation-artifacts/m2/1-3-derive-layout-ir-for-cabinet-and-wiring-views.md`
- `_bmad-output/implementation-artifacts/m2/sprint-status.yaml`
- `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`
- `kernel/layout-model/src/test/kotlin/com/engineeringood/athena/layout/LayoutModelTest.kt`
- `kernel/layout-model/README.md`
- `kernel/layout-model/README.zh-CN.md`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/LayoutIrDeriver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `kernel/compiler/README.md`
- `kernel/compiler/README.zh-CN.md`

## Change Log

- 2026-07-06: Created Story 1.3 with compiler, layout-model, and manifesto-aligned implementation guardrails.
- 2026-07-06: Implemented deterministic compiler-owned `Layout IR` derivation for `cabinet` and `wiring`, added regression coverage, and updated module documentation.
