---
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
---

# Story 4.2: Compose The Professional Cabinet Drawing

Status: review

## Story

As a customer reviewing Athena,
I want the complete sample arranged as a credible Cabinet drawing,
so that the first impression is a professional engineering product.

## Acceptance Criteria

1. **Given** the governed compiled Element set and project layout facts, **when** spatial and
   `drawing-composition` compilers run, **then** enclosure, rails, devices, terminals, labels, route
   channels, frame, and title block use derived bounds and stable industrial spacing.
2. **Given** desktop and narrow available viewports, **when** Cabinet fits and centers, **then** no
   visible item is clipped/off-screen, no text overflows, no unintended component overlap exists, no
   route endpoint leaves its anchor, and no normal-state hitbox/border/background is visible.
3. **Given** hover, selection, or DnD is active, **when** interaction state changes, **then** dotted
   interaction borders appear only for that state and disappear on return to normal.
4. **Given** all previous criteria are green, **when** mandatory polish/purge runs, **then** source,
   tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed; stale/duplicate
   artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review are recorded.

**Implements:** FR-30, FR-33..FR-36, FR-41; NFR-7..NFR-9.

## Tasks / Subtasks

- [x] Add Story 4.2 RED contracts before production edits (AC: 1..3)
  - [x] Add a failing professional Cabinet composition proof for enclosure, rail, devices, terminals, route channel, frame, and title block.
  - [x] Add a failing fit/overlap/route-anchor proof for desktop and narrow viewport envelopes.
  - [x] Add a failing normal/active interaction chrome proof.
- [x] Expand the M34 sample project into a professional Cabinet arrangement (AC: 1)
  - [x] Add representative devices, ports, connections, and Cabinet layout facts to the sample `.athena` source.
  - [x] Add binding rules from sample device facts to the governed Cabinet element set.
  - [x] Preserve Cabinet as the only customer-facing M34 view.
- [x] Compose the professional Cabinet drawing proof (AC: 1, 2)
  - [x] Use derived bounds from element bodies and `drawing-composition`, not hard-coded document viewBox.
  - [x] Prove enclosure, rails, terminals, labels, route channels, frame, and title block are in-bounds.
  - [x] Prove no unintended component overlap, clipping, text overflow, route endpoint drift, or fallback component.
- [x] Prove interaction chrome behavior structurally (AC: 3)
  - [x] Record normal-state chrome as transparent/hidden.
  - [x] Record active interaction border as state-only and not part of normal drawing authority.
  - [x] Keep Theia/workbench chrome out of semantic/render authority.
- [x] Run sequential verification (AC: 1..3)
  - [x] Run focused compiler/drawing-composition/renderer tests.
  - [x] Run full Gradle `test` sequentially after focused suites pass.
  - [x] Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text/doc edits.
- [x] Perform mandatory deep polish/purge and evidence review (AC: 4)
  - [x] Audit sample source, binding rules, composition proof, package hierarchy, generated outputs, docs,
        encoding, diff, and dirty-worktree boundaries.
  - [x] Remove stale duplicate fixtures/generated artifacts not meant for source control.
  - [x] Record RED/GREEN, AC-to-evidence, independent blind/edge/acceptance reviews, review
        dispositions, and every touched file before changing the story to review.

## Dev Notes

### Scope Boundary

This story improves the Cabinet sample composition and structural proof. It does not run Electron
screenshots or claim QET/EPLAN/IEC standards equivalence. Story 4.3 owns final E2E screenshots and
handoff.

### Required Architecture

- Project `.athena` owns actual devices, ports, connections, and layout.
- Package `.athena` owns reusable element contracts and binding rules.
- `drawing-composition` owns sheet/frame/zone/structure facts and derived document bounds.
- Normal chrome remains transparent; active interaction chrome is state-only.

### Previous Story Intelligence

- Story 4.1 added the governed Cabinet element vocabulary and proof test.
- Story 3.4 added typed render-path proof and no raw-markup/fallback gates.
- The current M34 sample source is still small and must be expanded for product credibility.

### Testing Requirements

- Capture genuine RED before production edits.
- Assert composition facts structurally; do not use DOM parsing.
- Keep desktop/narrow fit checks deterministic and numeric.
- Run Gradle verification sequentially only.

### References

- [M34 PRD](../../planning-artifacts/prds/prd-Athena-2026-07-24-m34/prd.md)
- [M34 Architecture Spine](../../planning-artifacts/architecture/architecture-Athena-2026-07-24-m34/ARCHITECTURE-SPINE.md)
- [M34 Epics](epics.md)
- [Story 4.1](4-1-deliver-the-native-typed-cabinet-element-set.md)
- [Professional Renderer Target](m34-professional-renderer-target.md)

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: added `AthenaM34ProfessionalCabinetCompositionTest` before production edits and watched it fail on missing Cabinet sample content.
- GREEN: expanded `examples/m34/sample-project/src/01-native-cabinet-proof.athena` and `examples/m34/sample-project/packages/representation/athena/iec/cabinet-bindings.athena`, then re-ran the focused proof to green.
- Verification: ran focused compiler test, full `.\gradlew.bat --no-daemon --console=plain test`, and `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.
- Polish/purge: reviewed the touched sample, binding, and proof files against ACs; no stale duplicate fixture remained in the active M34 path.

### Completion Notes List

- Ultimate context engine analysis completed from M34 PRD, architecture spine, epics, Story 4.1,
  current M34 sample source, and drawing-composition test patterns.
- Professional Cabinet composition proof now covers a richer rolling-shutter cabinet sample with
  governed source facts, package bindings, derived structure bounds, fit checks, and transparent
  normal chrome assertions.
- Mixed native and SVG rendering is covered through both `drawing.foreground` and `foreground`
  palette tokens so the Cabinet proof matches the active render path.

### File List

- `_bmad-output/implementation-artifacts/m34/4-2-compose-the-professional-cabinet-drawing.md`
- `examples/m34/sample-project/src/01-native-cabinet-proof.athena`
- `examples/m34/sample-project/packages/representation/athena/iec/cabinet-bindings.athena`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34ProfessionalCabinetCompositionTest.kt`
- `_bmad-output/implementation-artifacts/m34/sprint-status.yaml`

### Change Log

- 2026-07-25: Created Story 4.2 with professional Cabinet composition scope and proof guardrails.
- 2026-07-25: Expanded the M34 sample Cabinet project, added the professional Cabinet composition proof,
  and completed RED/GREEN plus full suite and encoding verification.
