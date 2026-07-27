---
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
---

# Story 4.1: Deliver The Governed Cabinet Element Set

Status: review

## Story

As a Cabinet engineer,
I want a compact professional set of typed reusable components,
so that the demo represents recognizable industrial equipment instead of generic boxes.

## Acceptance Criteria

1. **Given** the M34 Athena package contains concise native definitions and complex annotated-SVG
   definitions, **when** the governed set compiles through both source frontends, **then** it provides
   governed enclosure, DIN rail, protective device, switch/control, relay/contactor, terminal block,
   power supply, actuator/load, label, and route-channel Symbols/Elements required by the sample.
2. **Given** each connectable Element, **when** contract validation runs, **then** required anchors,
   role/direction/signal compatibility, labels, bounds, version, lifecycle, and provenance are
   complete.
3. **Given** the typed set is rendered, **when** visual structure is inspected, **then** component
   proportions, terminals, line weights, label hierarchy, and transparent normal chrome match the
   approved industrial Cabinet criteria without standards-compliance claims.
4. **Given** all previous criteria are green, **when** mandatory polish/purge runs, **then** source,
   tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed; stale/duplicate
   artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review are recorded.

**Implements:** FR-30, FR-32, FR-35, FR-41..FR-42; NFR-7..NFR-9.

## Tasks / Subtasks

- [x] Add Story 4.1 RED contracts before production edits (AC: 1..3)
  - [x] Add a failing compile proof for all required Cabinet element categories.
  - [x] Add a failing contract validation proof for anchors, labels, bounds, version, lifecycle, and provenance.
  - [x] Add a failing render-structure proof for terminals, line weights, label hierarchy, and transparent chrome.
- [x] Author the governed M34 Cabinet element set (AC: 1)
  - [x] Add concise native Athena Symbol/Element definitions for simple Cabinet shapes.
  - [x] Add or reuse governed annotated-SVG definitions only where shape complexity justifies it.
  - [x] Keep resources colocated with their Athena package files under a package-like directory hierarchy.
- [x] Complete connectable element contracts (AC: 2)
  - [x] Ensure every connectable Element declares compatible anchors without owning project ports.
  - [x] Ensure label slots, bounds, lifecycle, version, and provenance compile deterministically.
  - [x] Fail closed on missing/incompatible contracts; no fallback component is admitted.
- [x] Add typed render-structure proof (AC: 3)
  - [x] Prove all visible elements lower to typed `GraphicPrimitiveDocument` bodies.
  - [x] Prove normal chrome remains transparent and no fallback box is produced.
  - [x] Prove industrial criteria by structured categories rather than standards-compliance claims.
- [x] Run sequential verification (AC: 1..3)
  - [x] Run focused compiler/package/renderer tests.
  - [x] Run full Gradle `test` sequentially after focused suites pass.
  - [x] Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text/doc edits.
- [x] Perform mandatory deep polish/purge and evidence review (AC: 4)
  - [x] Audit package source hierarchy, SVG resource colocation, stale XML/Kotlin fixtures, generated outputs,
        docs, encoding, diff, and dirty-worktree boundaries.
  - [x] Remove stale duplicate fixtures/generated artifacts not meant for source control.
  - [x] Record RED/GREEN, AC-to-evidence, independent blind/edge/acceptance reviews, review
        dispositions, and every touched file before changing the story to review.

## Dev Notes

### Scope Boundary

This story supplies the governed element vocabulary for the Cabinet product surface. It does not
complete final arrangement, Electron screenshots, QET import, or standards-compliance claims. Story
4.2 owns professional drawing composition; Story 4.3 owns E2E proof and handoff.

### Required Architecture

- Athena source remains the only metadata authority.
- Symbol is atomic visual anatomy; Element is reusable visual composition, not an engineering component.
- SVG may be referenced only as governed compile-time geometry with node-local `data-athena-*` contracts.
- Package resources should be colocated with their Athena package files, avoiding string path rewrites
  that assume project-relative duplication.
- Active Cabinet render path consumes `GraphicPrimitive` only.

### Previous Story Intelligence

- Story 3.4 added `M34CabinetRenderPathProof` and deletion gates for no active raw markup, fallback,
  or `PresentationPrimitive` authority.
- The professional renderer target note sets the quality bar but keeps exact QET-style schematic
  matching as Epic 4/E2E scope-gated work if Cabinet remains the only product view.

### Testing Requirements

- Capture genuine RED before production edits.
- Assert package categories and contracts structurally.
- Avoid DOM parsing; inspect compiled definitions, anchors, label slots, primitive kinds, and render-path proof.
- Run Gradle verification sequentially only.

### References

- [M34 PRD](../../planning-artifacts/prds/prd-Athena-2026-07-24-m34/prd.md)
- [M34 Architecture Spine](../../planning-artifacts/architecture/architecture-Athena-2026-07-24-m34/ARCHITECTURE-SPINE.md)
- [M34 Epics](epics.md)
- [Story 3.4](3-4-make-graphic-primitive-the-only-cabinet-render-path.md)
- [Professional Renderer Target](m34-professional-renderer-target.md)

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaM34CabinetElementSetTest"` failed before source edits because the required governed Cabinet element identities were absent.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaM34CabinetElementSetTest"` passed after the element set and label SVG were added.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaM34CabinetElementSetTest" --tests "com.engineeringood.athena.compiler.AthenaRepresentationPackageSnapshotCompilerTest" --tests "com.engineeringood.athena.compiler.AthenaM34ElementCabinetProofTest"` passed.
- GREEN: `.\gradlew.bat --no-daemon --console=plain test` passed.
- GREEN: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Ultimate context engine analysis completed from M34 PRD, architecture spine, epics, Story 3.4,
  current `examples/m34/sample-project` package layout, and the professional renderer target note.
- AC-1 evidence: `cabinet-element-set.athena` now provides governed enclosure, DIN rail, protective
  device, switch/control, relay/contactor, terminal block, power supply, actuator/load, label, and
  route-channel Elements; `cabinet-label.svg` provides governed label-slot coverage.
- AC-2 evidence: connectable Elements expose terminal anchors with accepted direction/signal
  predicates, version `1.0.0`, `.athena` provenance, intrinsic bounds, and typed primitive bodies.
- AC-3 evidence: `AthenaM34CabinetElementSetTest` proves every required Element lowers to accepted
  typed render-path proof with no XML/raw-markup/fallback/hard-coded-bounds authority.
- AC-4 evidence: full Gradle `test`, encoding audit, AC mapping, package hierarchy audit, and
  three-layer review are recorded.
- Blind review: new package material is source-owned `.athena` plus one colocated governed SVG; no XML
  manifest or sidecar metadata was introduced.
- Edge review: label-slot coverage uses SVG only for the contract-bearing text node; connectable
  terminals remain explicit anchors with no inferred meaning from ordinary geometry.
- Acceptance review: FR-30, FR-32, and FR-35 are covered structurally; Story 4.2 still owns final
  composition quality and visual layout.

### File List

- `_bmad-output/implementation-artifacts/m34/4-1-deliver-the-native-typed-cabinet-element-set.md`
- `_bmad-output/implementation-artifacts/m34/sprint-status.yaml`
- `examples/m34/sample-project/packages/representation/athena/iec/cabinet-element-set.athena`
- `examples/m34/sample-project/packages/representation/athena/iec/cabinet-label.svg`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34CabinetElementSetTest.kt`

### Change Log

- 2026-07-25: Created Story 4.1 with governed Cabinet element-set scope and proof guardrails.
- 2026-07-25: Added governed Cabinet Element set, colocated label SVG, and structural package/render
  proof.
