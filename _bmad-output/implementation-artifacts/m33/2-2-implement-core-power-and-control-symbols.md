---
story_id: 2.2
story_key: 2-2-implement-core-power-and-control-symbols
epic: 2
status: done
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
created: '2026-07-23'
updated: '2026-07-23'
---

# Story 2.2: Implement Core Power And Control Symbols

## Status

Done

## Story

As a controls engineer, I want professional power/control symbols so the demo no longer renders
generic boxes.

## Acceptance Criteria

- Package includes exactly the first seven required families: power supply marker, protective
  breaker/fuse, switch/contact, relay coil, lamp/indicator, motor/load, and connection dot.
- Every symbol proves anchors, label slots, primitive output, bounds, lifecycle, and profile
  compatibility.
- M32 binding resolution for matching concepts returns native symbol descriptors with fallback
  rejected. Live SVG/Workbench no-fallback proof remains Story 3.3/6.2 scope.
- AC-to-evidence and polish/purge notes are recorded before review.

## Tasks/Subtasks

- [x] Add RED contract tests for the seven exact symbol identities and package descriptor/resource
  entries. (AC: 1)
- [x] Add RED anatomy and Graphic Primitive IR validation tests for anchors, label/reference slots,
  hotspots, bounds, lifecycle, profile/package tags, provenance, primitive structure, and stable
  ids. (AC: 1,2)
- [x] Implement an Athena-owned data-driven core symbol library using existing
  `DrawingSymbolAnatomy` and `GraphicPrimitive` contracts. (AC: 1,2)
- [x] Replace `descriptor.iec.scaffold` / `resource.iec.scaffold` in the Story 2.1 package descriptor
  with the seven native descriptor/resource entries. (AC: 1,2)
- [x] Prove M32 binding resolution selects a native descriptor and reports no renderer fallback;
  do not add SVG, Theia, DOM, or source-syntax behavior. (AC: 3)
- [x] Add negative/quality tests rejecting fallback/scaffold identities, single generic-box device
  anatomy, missing required anchors/slots, duplicate ids, and external provenance. (AC: 1..3)
- [x] Run focused, module, repository, and encoding verification sequentially on Windows. (AC: 1..4)
- [x] Complete mandatory AC-to-evidence and deep polish/purge review, then update the cleanup
  ledger. (AC: 4)

## Dev Notes

- Bind to M33 AD-1, AD-2, AD-3, AD-6, AD-7, and AD-10.
- Reuse `DrawingSymbolAnatomy`, `DrawingSymbolAnatomyValidator`, `GraphicPrimitive`, and
  `GraphicPrimitiveIrValidator`; do not invent a second symbol or primitive model.
- Extend `M33IecRepresentationPackage` from Story 2.1. Keep IEC naming inside this package; generic
  drawing contracts remain in `kernel/representation-model`.
- Required stable identities:
  `iec.power-supply-marker`, `iec.protective-device`, `iec.switch-contact`, `iec.relay-coil`,
  `iec.lamp-indicator`, `iec.motor-load`, and `iec.connection-dot`.
- Every device symbol must have at least two meaningful primitives and must not be represented by a
  lone generic rectangle. A connection dot may remain one dedicated `ConnectionDot` primitive.
- Every symbol must include required anchors, at least one label slot, optional reference slot,
  bounded hotspot, positive bounds, supported orientation, active lifecycle, `m33-iec` profile tag,
  Athena package id, and Athena-owned provenance.
- Primitive ids must be unique per symbol; geometry and bounds must pass Graphic Primitive IR
  validation using renderer-neutral style-token ids.
- Use standards only as reference anchors. Do not claim IEC compliance and do not copy QET, EPLAN,
  vendor, SVG path, or JSX assets.
- No `.athena` syntax, compiler semantics, renderer adapter, Workbench UI, or screenshot work in
  this story.

## Architecture Compliance

- `.athena` remains semantic truth; this package contains visual definitions only.
- Package/profile/binding selection stays in M32 package runtime. Symbols do not select themselves.
- Renderer-neutral primitives are the output boundary. SVG remains future adapter scope.
- Normal interaction chrome and viewBox calculation are not symbol-package authority.

## File Structure Requirements

- Primary package API and definitions belong under
  `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/`.
- Tests belong under the matching `kernel/package-runtime/src/test/...` package.
- Keep cohesive definitions together, but split primitive-builder/support code if a Kotlin file
  crosses roughly 200-300 lines with distinct responsibilities.
- Do not add dependencies or new modules.

## Testing

- TDD is mandatory: run the new focused test and confirm RED before implementation.
- Validate all seven anatomies with `DrawingSymbolAnatomyValidator`.
- Wrap each anatomy's primitives in `GraphicPrimitiveDocument` and validate with
  `GraphicPrimitiveIrValidator` using declared bounds and shared renderer-neutral style tokens.
- Assert exact symbol/descriptor/resource identity sets, deterministic order, required anchor ids,
  label-slot roles, lifecycle/profile/package metadata, and Athena provenance.
- Assert M32 package descriptor validation and one representative `BindingResolver` path with
  `rendererFallbackAccepted == false`.
- Run `:kernel:package-runtime:test`, full `gradlew test`, and `tools/encoding-audit.ps1` strictly
  sequentially.

## Evidence Plan

- AC1: exact seven-symbol inventory and package descriptor/resource tests.
- AC2: anatomy/primitive validation plus structural assertions per symbol.
- AC3: representative M32 binding proof with no fallback; live renderer proof deferred explicitly
  to Story 3.3/6.2.
- AC4: Dev Agent Record, full regressions, encoding audit, adversarial review, and cleanup ledger.

## Polish And Purge

Remove the Story 2.1 scaffold descriptor/resource ids and close its cleanup-ledger row. Audit for
duplicate ids, generic fallback anatomy, copied/external provenance, dead builders, SVG/Theia
leakage, and hard-coded coordinates outside package-local symbol geometry.

## Previous Story Intelligence

- Story 2.1 established package id `com.athena.standard.representation.iec`, profile `m33-iec`,
  fail-closed ownership validation, local-registry discovery, and M32 binding proof.
- `profileFor()` requires at least one compatible engineering package id and normalizes duplicates.
- The Story 2.1 scaffold descriptor/resource is temporary debt owned by this story; leaving it in
  the completed core package is a failure.
- Story 2.1 full regression passed 147 Gradle tasks and recorded no M30 asset as superseded.

## Git Intelligence

- Baseline commit is M32 `0311ad6`; all M33 work is currently uncommitted and must remain coherent
  with the existing M33 Epic 1 files.
- Follow the repository's Kotlin grouping rule and existing `kotlin.test` conventions.
- Never add `.tools` or modify reference QET sources.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Create-story context pass identified the Story 3.3/6.2 live-render dependency and constrained
  Story 2.2 to package/anatomy/primitive/binding proof.
- CodeGraph inspected `DrawingSymbolAnatomy`, Graphic Primitive IR, M32 package descriptor,
  Binding Manifest, and Binding Resolver contracts before implementation.
- RED: focused test compilation failed because `M33IecCoreSymbolLibrary` did not exist.
- GREEN: seven-symbol inventory, anatomy, primitive, package, and negative tests passed.
- INTEGRATION RED: module regression proved M32 selected the alphabetically first descriptor from a
  multi-descriptor package because no entry-level binding policy contract existed.
- INTEGRATION GREEN: added validated `bindingPolicyTags` and deterministic fail-closed descriptor
  selection while preserving untagged single-descriptor M32 compatibility.
- REVIEW RED/GREEN: added and fixed tests for transparent connection dots, text fill, rating/model
  slots, wrong single-descriptor policy fallback, and cross-entry policy ambiguity.
- MODULE: `:kernel:package-runtime:test` passed.
- FULL REGRESSION: `gradlew test` passed with 147 actionable tasks (13 executed, 134 up-to-date).

### Completion Notes List

- Added seven deterministic Athena-owned IEC-style definitions: power supply marker, protective
  device, switch/contact, relay coil, lamp, motor/load, and filled connection dot.
- Every anatomy passes existing anatomy and Graphic Primitive IR validators with required anchors,
  device/rating slots, optional reference slots, bounded hotspots, active lifecycle, orientation,
  profile/package identity, and Athena provenance.
- Replaced Story 2.1 scaffold entries with native descriptor/resource identities and three
  renderer-neutral style tokens; no SVG, JSX, Theia, source syntax, or external asset entered the
  package.
- Extended Representation Package entries with validated binding policy tags so a package can own
  multiple descriptors without arbitrary alphabetical selection. Missing, mismatched, or ambiguous
  mappings fail closed and never accept renderer fallback.
- AC-to-evidence: AC1 is covered by exact inventory/package tests; AC2 by per-symbol anatomy and
  primitive validation; AC3 by representative binding, missing/ambiguous policy, and no-fallback
  tests; AC4 by this record, cleanup ledger, regressions, encoding audit, and adversarial review.
- Polish/purge: scaffold ids are absent from production/test code; no copied QET/EPLAN/vendor asset,
  fallback box, dead builder, duplicate primitive id, unresolved style, or UI/renderer authority
  remains in touched code.
- Adversarial review addressed 14 risks: skeletal story context, premature live-render claim,
  scaffold debt, arbitrary descriptor selection, missing/mismatched/ambiguous policy, invalid tags,
  incomplete style refs, transparent dot, text fill, rating slot, generic-box anatomy, old M32
  diagnostic behavior, and external provenance.
- Final acceptance review: PASS for AC1-AC4; no unresolved Story 2.2 finding remains.

### File List

- `_bmad-output/implementation-artifacts/m33/2-2-implement-core-power-and-control-symbols.md`
- `_bmad-output/implementation-artifacts/m33/cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m33/sprint-status.yaml`
- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/RepresentationPackageModels.kt`
- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/RepresentationPackageValidation.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/BindingResolver.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M33IecCoreSymbolLibrary.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M33IecRepresentationPackage.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/M33IecCoreSymbolLibraryTest.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/M33IecRepresentationPackageTest.kt`

## Change Log

- 2026-07-23: Expanded BMAD story context and clarified the renderer/live-proof dependency boundary.
- 2026-07-23: Implemented and verified seven package-backed IEC-style core symbols and governed
  multi-descriptor binding policy.
- 2026-07-23: Final adversarial and acceptance review passed; story closed.
