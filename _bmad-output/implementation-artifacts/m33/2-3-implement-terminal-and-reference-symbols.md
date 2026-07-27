---
story_id: 2.3
story_key: 2-3-implement-terminal-and-reference-symbols
epic: 2
status: done
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
created: '2026-07-23'
updated: '2026-07-23'
---

# Story 2.3: Implement Terminal And Reference Symbols

## Status

Done

## Story

As a controls engineer, I want terminals and references so the sheet looks like an engineering
document, not a plain graph.

## Acceptance Criteria

- Package includes terminal, terminal strip segment, folio/continuation reference, and reference
  label slots.
- Route anchor tests reject center fallback for demo terminal/reference symbols.
- Reference symbols expose slots for source/target sheet or zone facts.
- AC-to-evidence and polish/purge notes are recorded before review.

## Tasks/Subtasks

- [x] Add RED inventory/package tests for `iec.terminal`, `iec.terminal-strip-segment`, and
  `iec.folio-continuation-reference`. (AC: 1)
- [x] Add RED anatomy/Graphic Primitive IR tests for terminal anchors, terminal/reference label
  slots, source/target sheet-zone reference slots, bounds, lifecycle, profile, and provenance.
  (AC: 1,3)
- [x] Refactor Story 2.2 symbol construction/style support into a shared package-local helper before
  adding new definitions; do not duplicate primitive builders. (AC: 1,4)
- [x] Implement the three Athena-owned data-driven symbols and add their descriptor/resource/policy
  entries to the existing M33 IEC package. (AC: 1,3)
- [x] Add descriptor-anchor route evidence tests proving terminal/reference anchor coordinates and
  `centerFallbackAccepted == false`. (AC: 2)
- [x] Add negative tests for missing/mismatched anchors and required reference slots; failures must
  produce structured diagnostics and no route. (AC: 2,3)
- [x] Audit legacy `_reference` fixtures and remove or ledger only those touched/superseded. (AC: 4)
- [x] Run focused, module, repository, and encoding verification sequentially on Windows. (AC: 1..4)
- [x] Complete mandatory AC-to-evidence and deep polish/purge review. (AC: 4)

## Dev Notes

- Bind to M33 AD-1, AD-2, AD-3, AD-4, AD-6, AD-8, and AD-10.
- Reuse Story 2.2 `M33IecSymbolDefinition`, style tokens, primitive helpers, package descriptor,
  and `bindingPolicyTags`. Extract shared support rather than copy it.
- Exact new identities: `iec.terminal`, `iec.terminal-strip-segment`, and
  `iec.folio-continuation-reference`. Epic 2 then owns exactly ten symbols across its two libraries.
- Terminal and strip symbols require `line` and `field` terminal anchors plus a required
  `terminal-number` label slot. The continuation symbol requires a `continuation` reference anchor
  and a required `cross-reference` label slot.
- Continuation anatomy must expose reference slots `source-sheet`, `source-zone`, `target-sheet`,
  and `target-zone`; sheet slots are required and zone slots may be optional.
- Reference facts remain projection-side slots. Do not add `.athena` syntax or mutate semantic
  source from sheet/zone values.
- Route proof must use `DescriptorAnchorRouteEvidenceMapper` and descriptor-declared anchors. A
  missing descriptor/anchor returns structured diagnostics, null route, and no center fallback.
- No SVG, DOM, Theia, renderer, sheet-composition implementation, or IEC compliance claim.

## Architecture Compliance

- IEC remains package identity, not drawing-engine identity.
- Graphic primitives and reference slots are presentation facts, not engineering truth.
- Binding policy selects descriptors; renderer and Workbench do not infer from filenames or labels.
- Route evidence consumes explicit anchors and never fabricates center points.

## File Structure Requirements

- Shared symbol construction support and both symbol libraries stay in
  `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/`.
- Keep each Kotlin file cohesive and below the repository's approximate 200-300 line split
  threshold where roles differ.
- Tests stay in the matching package-runtime test source set. Do not add modules/dependencies.

## Testing

- TDD is mandatory: focused tests must fail before implementation.
- Validate every new anatomy with `DrawingSymbolAnatomyValidator` and every primitive scene with
  `GraphicPrimitiveIrValidator` using shared style tokens.
- Assert exact identity/descriptor/resource/policy sets, deterministic order, required slots,
  route anchor coordinates, and no fallback acceptance.
- Test both valid terminal-to-reference routing and missing/mismatched descriptor anchors.
- Run `:kernel:package-runtime:test`, full `gradlew test`, and `tools/encoding-audit.ps1` strictly
  sequentially.

## Evidence Plan

- AC1: exact three-symbol inventory plus ten-entry package inventory and validator proofs.
- AC2: descriptor-anchor route evidence success/failure tests with no center fallback.
- AC3: exact source/target sheet-zone slot assertions and missing-slot diagnostics.
- AC4: Dev Agent Record, sequential regressions, encoding audit, adversarial review, and fixture
  scan/cleanup ledger.

## Polish And Purge

Extract shared Story 2.2 symbol support, remove duplicate/dead helpers, and audit old `_reference`
fixtures. Do not delete historical or unrelated fixtures; ledger any touched debt with owner,
reason, target, and verification.

## Previous Story Intelligence

- Story 2.2 created seven valid core symbols, three renderer-neutral style tokens, and the
  Athena-owned package descriptor.
- Story 2.2 extended descriptor entries with validated `bindingPolicyTags`; missing, mismatched,
  and ambiguous multi-descriptor selection fails closed while untagged one-entry M32 packages stay
  compatible.
- Story 2.2 removed all scaffold ids and passed full 147-task regression.
- `M33IecCoreSymbolLibrary.kt` is now about 260 lines and mixes shared factories with core inventory;
  Story 2.3 must split shared support before adding more symbols.

## Git Intelligence

- Baseline commit remains M32 `0311ad6`; M33 work is uncommitted and must be treated as one coherent
  milestone change.
- Use existing `kotlin.test`, validators, and route-evidence contracts; no external dependency or
  reference-source modification.
- Never add `.tools`.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Create-story context pass fixed exact symbol, route-evidence, reference-slot, and source-authority
  boundaries before implementation.
- CodeGraph inspected Story 2.2 symbol/package contracts and existing
  `DescriptorAnchorRouteEvidenceMapper` success/failure patterns.
- RED: focused test compilation failed because `M33IecTerminalReferenceSymbolLibrary` did not exist.
- GREEN: terminal/reference inventory, anatomy/primitive validation, ten-symbol package, route
  evidence, and missing anchor/slot tests passed.
- REGRESSION RED: Story 2.2 package test assumed the whole package would permanently contain only
  seven entries; corrected it to assert the stable core prefix while allowing package extension.
- REFACTOR: split the 260-line mixed core file into a 192-line shared support file, 119-line core
  library, 59-line terminal/reference library, and catalog authority.
- MODULE: `:kernel:package-runtime:test` passed.
- FULL REGRESSION: `gradlew test` passed with 147 actionable tasks (11 executed, 136 up-to-date).

### Completion Notes List

- Added `iec.terminal`, `iec.terminal-strip-segment`, and `iec.folio-continuation-reference`, taking
  the native package to ten deterministic symbols.
- Terminal/strip definitions expose `line`/`field` anchors and required terminal-number slots;
  continuation exposes a reference anchor, required cross-reference label, required source/target
  sheet slots, and optional source/target zone slots.
- Descriptor-anchor route evidence resolves exact terminal/reference coordinates, returns null on a
  missing mapping, and never accepts center fallback.
- Introduced `M33IecSymbolCatalog` and generic `M33IecSymbolDefinition`; shared styles, metadata,
  labels, anchors, and primitive builders no longer belong to or duplicate the core-only library.
- AC-to-evidence: AC1 is covered by three-symbol and ten-entry package inventory tests; AC2 by route
  success/failure and no-fallback assertions; AC3 by exact required reference-slot assertions and
  missing-slot diagnostics; AC4 by this record, regressions, encoding audit, fixture scan, and review.
- Polish/purge: stale core-specific definition naming and mixed helper ownership were removed;
  scaffold ids remain absent; `_reference` hits are unchanged historical/defensive fixtures already
  governed by `_bmad-output/implementation-artifacts/deferred-work.md` and M32 cleanup evidence.
- Adversarial review covered shared-helper duplication, wrong catalog ownership, brittle seven-entry
  assertions, missing reference facts, missing route anchors, center fallback, source mutation,
  primitive validity, stale fixture handling, and file-size responsibility boundaries; no blocking
  finding remains.
- Final acceptance review: PASS for AC1-AC4; no unresolved Story 2.3 finding remains.

### File List

- `_bmad-output/implementation-artifacts/m33/2-3-implement-terminal-and-reference-symbols.md`
- `_bmad-output/implementation-artifacts/m33/sprint-status.yaml`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M33IecCoreSymbolLibrary.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M33IecRepresentationPackage.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M33IecSymbolCatalog.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M33IecSymbolSupport.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M33IecTerminalReferenceSymbolLibrary.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/M33IecCoreSymbolLibraryTest.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/M33IecTerminalReferenceSymbolLibraryTest.kt`

## Change Log

- 2026-07-23: Expanded BMAD story context and fixed exact symbol, route-proof, and shared-support boundaries.
- 2026-07-23: Implemented and verified terminal, terminal-strip, and folio-continuation symbols with
  governed route/reference facts.
- 2026-07-23: Final adversarial and acceptance review passed; story closed.
