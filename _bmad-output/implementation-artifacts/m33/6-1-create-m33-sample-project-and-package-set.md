---
story_id: 6.1
story_key: 6-1-create-m33-sample-project-and-package-set
epic: 6
status: done
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
created: '2026-07-23'
updated: '2026-07-23'
---

# Story 6.1: Create M33 Sample Project And Package Set

## Status

Done

## Story

As a solution engineer, I want an M33 sample project so the milestone has a concrete demo target.

## Acceptance Criteria

- `examples/m33/sample-project` contains Athena-owned source, packages, profiles, and assets.
- Demo covers power supply, protective device, switch/control input, relay/actuator, terminal
  strip, motor/load, lamp/status, and reference marker.
- `athena.yaml` repository contract exists so LSP opens cleanly.
- AC-to-evidence and polish/purge notes are recorded before review.

## Tasks/Subtasks

- [x] Create the governed M33 Cabinet repository contract, source, lockfile, and Athena-owned XML
  package assets.
- [x] Resolve the seven Cabinet devices through the Engineering Package, Presentation Profile,
  Binding Manifest, native Representation Package, and Graphic Primitive IR chain.
- [x] Declare and load the composition-owned folio continuation/reference symbol without creating a
  fake semantic device.
- [x] Carry package-backed primitives, labels, terminals, and evidence through LSP, GLSP, and the
  Cabinet frontend renderer.
- [x] Add compiler and LSP tests for repository open, package-driven visual selection, text transport,
  fail-closed metadata, complete subject resolution, semantic-type compatibility, and explicit
  semantic-port-to-anchor mapping.
- [x] Complete three-layer adversarial review, AC-to-evidence mapping, and deep polish/purge.

## Dev Notes

- Bind to M33 AD-1, AD-6, AD-10.
- Avoid real vendor/proprietary assets.
- Package facts must affect visible rendering or be removed.

## Testing

- Compiler/runtime tests for sample package resolution.
- LSP/repository contract smoke for sample project.

## Evidence Plan

- Product proof references sample source and package identities.

## Polish And Purge

Audit examples for unused M32 copy/paste packages and stale screenshots.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED/GREEN: invalid active profile previously fell back to legacy M32 facts; the M33 package path
  now activates only when declared and then fails closed on load or validation errors.
- RED/GREEN: an unknown model previously dropped one subject from a partial fact list; every projected
  M33 Cabinet subject now requires one complete governed binding.
- RED/GREEN: port order previously selected the next available visual anchor; XML `anchorMap` facts
  now own exact semantic-port-to-representation-anchor mapping.
- RED/GREEN: package metadata could reclassify a canonical device or select a mismatched profile;
  semantic type, profile id/version/context/style, and representation package id/version now validate.
- Three-layer review found silent fallback, partial resolution, guessed anchors, duplicate package
  identities, absent reference evidence, and stale second authority. Blocking findings were fixed;
  renderer/composition and XML trust-hardening items were assigned to Stories 6.2 and 7.2.
- Fresh verification: compiler sample suite `8/8`, package-runtime suite successful, and LSP Cabinet
  projection smoke successful.

### Completion Notes List

- AC1: `examples/m33/sample-project` contains `athena.yaml`, `athena.lock`, one compact semantic
  Cabinet source, and separate engineering/profile/binding/representation package assets.
- AC2: seven semantic devices cover power, protection, control, relay, terminal strip, motor, and
  status. The sample Representation Package also governs `iec.folio-continuation-reference`; live
  composition-owned placement is intentionally Story 6.2, not a fake `.athena` device.
- AC3: compiler repository-contract validation and real LSP initialize/open/projection tests use the
  sample root and finish with active `cabinet` projection.
- AC4: this record and the cleanup ledger contain the AC evidence, review dispositions, and purge
  decisions.
- Package facts affect visible Cabinet output: changing the binding XML changes the selected symbol,
  changing an anchor map changes terminal geometry, and motor text survives the full transport path.
- No QET/vendor runtime dependency, visual syntax in `.athena`, built-in duplicate product inventory,
  or generic fallback is admitted by the active M33 package path.
- This story does not claim professional composition or customer-demo readiness. Stories 6.2 and 6.3
  still own live frame/rail/reference composition, structured product smoke, and Electron screenshots.

### File List

- `_bmad-output/implementation-artifacts/m33/6-1-create-m33-sample-project-and-package-set.md`
- `_bmad-output/implementation-artifacts/m33/cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m33/sprint-status.yaml`
- `examples/m33/sample-project/README.md`
- `examples/m33/sample-project/athena.lock`
- `examples/m33/sample-project/athena.yaml`
- `examples/m33/sample-project/packages/engineering/m33-cabinet-products.xml`
- `examples/m33/sample-project/packages/manifests/m33-cabinet-bindings.xml`
- `examples/m33/sample-project/packages/profiles/m33-iec.xml`
- `examples/m33/sample-project/packages/representation/m33-iec-symbols.xml`
- `examples/m33/sample-project/src/01-professional-cabinet.athena`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M33CabinetPackageSet.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/M33CabinetPresentationFactDeriver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriver.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM33SampleProjectCompilerTest.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/PresentationAnatomy.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/NativeRepresentationPrimitiveEmitter.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaM33CabinetProjectionSmokeTest.kt`
- `integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`
- `integrations/graph-glsp/src/athena-glsp-projection-source.ts`
- `ide/theia-frontend/src/browser/athena-graph-presentation-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-presentation-node.tsx`
- `ide/package.json`
- `ide/theia-product/package.json`

## Change Log

- 2026-07-23: Created the M33 Cabinet sample/package set, connected package-backed native symbols to
  the live Cabinet Presentation IR path, removed guessed/partial fallback behavior, and completed
  adversarial review plus cleanup evidence.
