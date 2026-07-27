---
story_id: 3.1
story_key: 3-1-compile-symbol-anatomy-to-graphic-primitive-ir
epic: 3
status: done
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
created: '2026-07-23'
updated: '2026-07-23'
---

# Story 3.1: Compile Symbol Anatomy To Graphic Primitive IR

## Status

Done

## Story

As a rendering pipeline developer, I want descriptor-backed symbols compiled to Graphic Primitive
IR so rendering stays backend-neutral.

## Acceptance Criteria

- Compiler maps symbol primitives, anchors, label slots, style tokens, transforms, and bounds into
  Graphic Primitive IR.
- Compiler does not inspect raw SVG DOM, CSS classes, file names, or label text for meaning.
- Compiler emits proof for symbol-to-primitive mapping.
- AC-to-evidence and polish/purge notes are recorded before review.

## Tasks/Subtasks

- [x] Add RED generic compiler tests for lossless primitives/transforms, bounds, style tokens,
  anchors, label/reference slots, and deterministic proof. (AC: 1,3)
- [x] Add RED fail-closed tests for invalid anatomy, invalid primitive IR, blank resource handle,
  and adapter-unsupported primitive kinds. (AC: 1..3)
- [x] Implement frontend-neutral `DrawingSymbolPrimitiveCompiler` contracts in
  `kernel/representation-model`; reuse existing anatomy/IR validators. (AC: 1..3)
- [x] Add M33 package adapter compiling `M33IecSymbolCatalog` through descriptor/resource entries
  without renderer or file-name inference. (AC: 1..3)
- [x] Replace file-like `resources/athena-iec-*.vector` values with governed `athena-native:`
  resource handles and close the Epic 2 cleanup/action item. (AC: 2,3)
- [x] Prove all ten catalog symbols compile with exact descriptor/resource/primitive/anchor/slot
  evidence and deterministic ordering. (AC: 1,3)
- [x] Audit M30 `PresentationPrimitive`, `NativeRepresentationLibraryLoader`, and direct SVG paths;
  remove only superseded M33 shortcuts and ledger retained compatibility. (AC: 4)
- [x] Run focused, module, repository, and encoding verification sequentially on Windows. (AC: 1..4)
- [x] Complete mandatory AC-to-evidence and deep polish/purge review. (AC: 4)

## Dev Notes

- Bind to M33 AD-1, AD-2, AD-3, AD-6, AD-8, and AD-10.
- Generic compiler input is anatomy + resolved descriptor id + governed resource handle + style
  tokens. It must not depend on package-runtime, IEC identities, SVG, DOM, CSS, or Theia.
- Output is a validated `GraphicPrimitiveDocument` plus structured proof carrying descriptor,
  symbol, resource handle, primitive ids/kinds, anchor ids, label/reference slot ids, style ids, and
  bounds. Anchors/slots remain evidence around the IR; do not invent them as graphic primitives.
- Preserve existing primitive instances losslessly, including nested groups and transforms.
- Use a declared supported-kind set so concrete adapters can fail closed before rendering an
  unsupported primitive family.
- Package adapter resolves catalog definitions by typed descriptor/resource ids and requires the
  `athena-native:` handle scheme. It may validate a resource handle but must not infer symbol meaning
  from path text.
- Do not delete M30 `PresentationPrimitive` or `PresentationSvgPath` while existing M30-M32 callers
  remain. Record them in cleanup ledger for the exact migration story.
- No SVG adapter, Workbench integration, viewBox, screenshot, or `.athena` syntax in this story.

## Architecture Compliance

- `representation-model` stays generic and dependency-free.
- `package-runtime` owns descriptor/catalog joining, not the renderer.
- Graphic Primitive IR is the only new renderer-neutral scene output.
- Package handles are resolved upstream; the compiler never opens arbitrary asset files.

## File Structure Requirements

- Generic compiler models/behavior belong in one cohesive
  `kernel/representation-model/.../DrawingSymbolPrimitiveCompiler.kt` file unless it exceeds the
  repository responsibility threshold.
- M33 catalog adapter and tests belong in package-runtime matching source sets.
- No dependency or module changes are expected.

## Testing

- TDD is mandatory: focused compiler and package-adapter tests must fail before implementation.
- Generic tests must prove lossless nested group/transform preservation and validator diagnostics.
- Adapter tests must compile all ten symbols, prove exact catalog order, reject unknown/unresolved
  descriptor/resource bindings, and reject non-`athena-native:` handles.
- Run `:kernel:representation-model:test`, `:kernel:package-runtime:test`, full `gradlew test`, and
  `tools/encoding-audit.ps1` strictly sequentially.

## Evidence Plan

- AC1: generic compiler equality/validation tests and ten-symbol package integration.
- AC2: source scans plus fail-closed resource-handle/unsupported-kind tests.
- AC3: deterministic structured proof assertions for descriptor→symbol→primitive/bounds mapping.
- AC4: Dev Agent Record, sequential regressions, encoding audit, adversarial review, and cleanup
  ledger updates.

## Polish And Purge

Close fake file-like native resource paths. Do not prematurely remove M30 compatibility models;
ledger their remaining callers and target Story 3.2/3.3. Scan for raw SVG path/DOM/CSS/file-name
inference in the new compiler and adapter.

## Previous Epic Intelligence

- Epic 2 produced `M33IecSymbolCatalog` with ten validated symbols and one package descriptor.
- Multi-descriptor package selection is governed by validated `bindingPolicyTags` and fails closed.
- Epic 2 retrospective identified logical `resources/athena-iec-*.vector` paths as a critical Epic 3
  predecessor; unresolved file-like paths cannot count as rendered assets.
- Story 1.2 already defines and validates Graphic Primitive IR, including groups/transforms,
  duplicate ids, cycles, non-finite geometry, style resolution, and bounds.
- M30 `PresentationPrimitive` and `PresentationSvgPath` still have active M30-M32 callers and cannot
  be deleted in Story 3.1 without migrating those call paths.

## Git Intelligence

- Baseline commit remains M32 `0311ad6`; all M33 artifacts are uncommitted and form one milestone.
- Reuse `kotlin.test` and current model patterns; no external dependencies.
- Never add `.tools` or modify QET/reference sources.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: package-runtime test compilation failed because `M33IecSymbolCompilationService` did not
  exist; subsequent governance tests failed for package validation, unknown catalog entries,
  blank/swapped native handles, style refs, package identity, and cyclic primitive graphs.
- GREEN: generic compiler and ten-symbol package adapter focused tests passed after each minimal
  implementation step.
- REVIEW: independent Blind Hunter found four blocking/important issues: pre-validation cycle
  flattening, unbound native handles, ignored descriptor styles, and unlocked package identity.
  All four received RED tests and fixes; re-review confirmed resolution.
- MODULE: `:kernel:representation-model:test` and `:kernel:package-runtime:test` passed sequentially.
- FULL REGRESSION: `gradlew test` passed with 147 actionable tasks (12 executed, 135 up-to-date).

### Completion Notes List

- Added a frontend-neutral compiler that validates anatomy and Graphic Primitive IR before proof
  flattening, preserving groups/transforms and safely diagnosing cycles and unsupported kinds.
- Added deterministic proof for descriptor, symbol, exact native resource handle, primitive ids and
  kinds, anchors, label/reference slots, style tokens, and bounds.
- Added a fail-closed M33 catalog adapter that validates package identity/coordinates, catalog
  inventory, exact descriptor-resource handles, and descriptor-governed style references.
- Replaced all M33 IEC pseudo-file resource paths with exact `athena-native:<resource-id>` handles.
- AC-to-evidence: AC1 is covered by lossless generic compiler tests and exact ten-symbol catalog
  proof; AC2 by source scan plus package/handle/style/unsupported-kind negative tests; AC3 by exact
  deterministic proof assertions; AC4 by this record, cleanup ledger, sequential regression,
  encoding audit, and independent adversarial review.
- Polish/purge: no SVG/DOM/CSS/file-name inference exists in the new compiler or adapter. M30
  `PresentationPrimitive`, `PresentationSvgPath`, and `NativeRepresentationLibraryLoader` remain
  active compatibility paths and are explicitly staged for Story 3.2/3.3 rather than deleted.
- The package adapter is intentionally staged upstream output in Story 3.1; Story 3.2 now explicitly
  owns production SVG-path consumption and an integration test so it cannot remain test-only.
- Final acceptance review: PASS for AC1-AC4; no unresolved Story 3.1 finding remains.

### File List

- `_bmad-output/implementation-artifacts/m33/3-1-compile-symbol-anatomy-to-graphic-primitive-ir.md`
- `_bmad-output/implementation-artifacts/m33/3-2-implement-svg-adapter-for-graphic-primitive-ir.md`
- `_bmad-output/implementation-artifacts/m33/cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m33/sprint-status.yaml`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M33IecRepresentationPackage.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M33IecSymbolCompilationService.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/M33IecRepresentationPackageTest.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/M33IecSymbolCompilationServiceTest.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/DrawingSymbolPrimitiveCompiler.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/DrawingSymbolPrimitiveCompilerTest.kt`

## Change Log

- 2026-07-23: Expanded BMAD story context and fixed generic compiler, package adapter, and native resource-handle boundaries.
- 2026-07-23: Implemented and verified governed symbol-anatomy compilation and exact ten-symbol
  package adaptation to Graphic Primitive IR.
- 2026-07-23: Resolved all adversarial review findings, completed polish/purge evidence, and closed
  the story.
