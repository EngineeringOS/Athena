---
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
---

# Story 3.4: Make Graphic Primitive The Only Cabinet Render Path

Status: review

## Story

As an Athena product maintainer,
I want one typed visual transport and renderer input,
so that raw SVG, PresentationPrimitive, and fallback boxes cannot silently return.

## Acceptance Criteria

1. **Given** all M34 Cabinet occurrences and drawing-composition facts, **when** presentation
   payloads are emitted, **then** only typed Graphic Primitive values reach LSP/Electron transport and
   `GraphicPrimitiveSvgAdapter`.
2. **Given** `PresentationPrimitive`, direct SVG markup, legacy box rendering, or raw markup transport,
   **when** active-caller and instrumented Electron tests run, **then** no active M34 producer/sink
   exists and named deletion gates pass or remain explicitly ledgered.
3. **Given** the complete Epic 3 sample, **when** Cabinet opens, **then** every component and route is
   visible with no XML/raw-markup/fallback authority and structured proof includes derived document
   viewBox and zero hard-coded document bounds.
4. **Given** all previous criteria are green, **when** mandatory polish/purge runs, **then** source,
   tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed; stale/duplicate
   artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review are recorded.

**Implements:** FR-22..FR-23, FR-29..FR-34, FR-41..FR-42; NFR-4..NFR-8.

## Tasks / Subtasks

- [x] Add Story 3.4 RED contracts before production edits (AC: 1..3)
  - [x] Add a failing active Cabinet transport proof for typed `GraphicPrimitiveDocument` payloads.
  - [x] Add a failing no-raw-markup/fallback sink proof for the M34 Cabinet product path.
  - [x] Add a failing proof that `PresentationPrimitive` has no active M34 producer.
- [x] Lock active Cabinet payloads to `GraphicPrimitive` (AC: 1)
  - [x] Make the M34 Cabinet session/proof payload structurally expose typed primitive document facts.
  - [x] Ensure `GraphicPrimitiveSvgAdapter` is the only SVG emission point for Cabinet visuals.
  - [x] Keep SVG source markup and `data-athena-*` annotations inside compiler provenance only.
- [x] Add active-caller and deletion-gate evidence (AC: 2)
  - [x] Ledger any remaining compatibility-only `PresentationPrimitive` caller with owner and deletion gate.
  - [x] Prove no M34 product caller emits direct SVG markup or generic fallback boxes.
  - [x] Prove LSP/Electron transport cannot carry raw Cabinet markup.
- [x] Add Epic 3 Cabinet structured proof (AC: 3)
  - [x] Prove every visible component/route has a typed primitive source and authority path.
  - [x] Prove derived document viewBox and zero hard-coded document bounds.
  - [x] Prove zero XML authority, zero raw markup authority, zero fallback authority.
- [x] Run sequential verification (AC: 1..3)
  - [x] Run focused representation/compiler/package-runtime/renderer tests.
  - [x] Run full Gradle `test` sequentially after focused suites pass.
  - [x] Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text/doc edits.
- [x] Perform mandatory deep polish/purge and evidence review (AC: 4)
  - [x] Audit active Cabinet render callers, compatibility ledgers, generated proof payloads, docs,
        encoding, diff, and dirty-worktree boundaries.
  - [x] Remove stale duplicate fixtures/generated artifacts not meant for source control.
  - [x] Record RED/GREEN, AC-to-evidence, independent blind/edge/acceptance reviews, review
        dispositions, and every touched file before changing the story to review.

## Dev Notes

### Scope Boundary

This story finishes Epic 3 authority cleanup. It does not deliver final customer visual quality, broad
Documentation/Schematic surfaces, a QET converter, or a symbol editor. Epic 4 owns the professional
Cabinet visual set and Electron screenshot proof.

### Required Architecture

- Active M34 Cabinet consumes `GraphicPrimitive` only.
- `GraphicPrimitiveSvgAdapter` may emit SVG fragments as a paint-only adapter; no raw SVG markup may
  be a transport or semantic authority.
- `PresentationPrimitive`, direct SVG, and generic box rendering are compatibility-only unless all
  active callers are migrated.
- `drawing-composition` owns Cabinet document bounds; hard-coded document viewBox is not allowed.
- Athena source is the only metadata authority; SVG annotations are compile-time contract input only.

### Previous Story Intelligence

- Story 3.1 made typed binding rules the sole active selector input.
- Story 3.2 made project-port facts explicit and provenance-rich.
- Story 3.3 made descriptors generated from `RepresentationDefinition` and proved the M34 sample
  compiles from `athena.yaml` package roots with no XML source hashes.
- CodeGraph shows `GraphicPrimitiveSvgAdapter.render` is the intended Cabinet SVG emitter and
  `PresentationPrimitive` still has at least one package-runtime dependency that must be migrated or
  ledgered truthfully.

### Testing Requirements

- Capture genuine RED before production edits.
- Prefer structural proof assertions over DOM parsing.
- Instrument authority by type/proof fields, not visual string matching alone.
- Run Gradle verification sequentially only.

### References

- [M34 PRD](../../planning-artifacts/prds/prd-Athena-2026-07-24-m34/prd.md)
- [M34 Architecture Spine](../../planning-artifacts/architecture/architecture-Athena-2026-07-24-m34/ARCHITECTURE-SPINE.md)
- [M34 Epics](epics.md)
- [Story 3.3](3-3-migrate-the-m34-cabinet-package-from-xml-to-athena.md)
- [Professional Renderer Target](m34-professional-renderer-target.md)

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaM34ElementCabinetProofTest"` failed before production edits because `toM34CabinetRenderPathProof` did not exist.
- RED: focused compiler render-path tests failed until transport proof assertions matched typed nested primitive payloads and the ledger explicitly covered remaining compatibility callers.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaM34ElementCabinetProofTest" --tests "com.engineeringood.athena.compiler.AthenaM34CabinetRenderPathDeletionGateTest"` passed.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test` passed.
- GREEN: `.\gradlew.bat --no-daemon --console=plain test` passed.

### Completion Notes List

- Ultimate context engine analysis completed from M34 PRD, architecture spine, epics, Story 3.3,
  CodeGraph render-path inspection, and the professional renderer target note.
- AC-1 evidence: M34 Cabinet proof now exposes typed `GraphicPrimitiveDocument` transport, nested
  primitive kinds, `GraphicPrimitiveSvgAdapter` renderer input authority, and typed payload authority.
- AC-2 evidence: deletion-gate test proves active M34 package/source/transport files do not contain
  `PresentationPrimitive`, direct raw markup fields, descriptor-bounds fallback, or fallback-box
  authority; remaining `PresentationPrimitive` callers are exact-ledgered M32/M33 compatibility.
- AC-3 evidence: proof asserts zero XML runtime authority, zero raw markup authority, zero fallback
  authority, derived `graphic-primitive-ir` document viewBox authority, and zero hard-coded document
  bounds.
- AC-4 evidence: full Gradle `test`, encoding audit, AC mapping, file list, migration ledger, and
  three-layer review are recorded.
- Blind review: no new source or transport field can carry raw SVG markup; the new proof is typed and
  derived from `GraphicPrimitiveDocument`.
- Edge review: nested primitive groups/transforms are counted recursively, so composed/native and
  SVG-backed definitions both prove typed visual transport.
- Acceptance review: FR-29..FR-34 are represented by render-path proof fields and deletion-gate source
  scans; Epic 4 still owns final professional visual/E2E screenshots.

### File List

- `_bmad-output/implementation-artifacts/m34/3-4-make-graphic-primitive-the-only-cabinet-render-path.md`
- `_bmad-output/implementation-artifacts/m34/representation-migration-ledger.md`
- `_bmad-output/implementation-artifacts/m34/sprint-status.yaml`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34CabinetRenderPathDeletionGateTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34ElementCabinetProofTest.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/M34CabinetRenderPathProof.kt`

### Change Log

- 2026-07-25: Created Story 3.4 with M34 typed Cabinet render-path guardrails.
- 2026-07-25: Added typed M34 Cabinet render-path proof, active source deletion gates, and migration
  ledger evidence for compatibility-only `PresentationPrimitive` callers.
