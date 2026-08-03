---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 4.1: Author The Dedicated M38 Example

Status: done

## Story

As an evaluator,
I want one dedicated source-first M38 project,
so that exact attachment is tested with realistic geometry instead of mocked presentation payloads.

## Acceptance Criteria

1. `examples/m38/professional-control-drawing` exists and uses Java-style Athena package hierarchy.
2. Source contains one complex package-local SVG-backed Element, one simple native Element, explicit
   Port-to-Anchor bindings, power/control/PE Connections, labels, one explicit junction, and one
   explicit no-connect crossing.
3. Engineering metadata stays in `.athena`; SVG carries geometry and neutral `data-athena-ref`
   references only.
4. The project reuses no active M36/M37 example, copied QET runtime format, XML authority, mock
   `PresentationDocument`, or hardcoded sample policy.
5. Valid source compiles without blocking diagnostics.
6. Dedicated example tests, package hierarchy checks, hygiene, encoding audit, forbidden scan, and
   `git diff --check` pass sequentially.

## Tasks / Subtasks

- [x] Create dedicated M38 project structure (AC: 1, 4)
  - [x] Create `examples/m38/professional-control-drawing`.
  - [x] Put Athena source under `src/com/engineeringood/m38/professionalcontroldrawing`.
  - [x] Add package-local resource folders without copying M36/M37 active examples.
- [x] Author source-first engineering example (AC: 2, 3)
  - [x] Add one complex SVG-backed Element declaration and resource binding.
  - [x] Add one simple native Element declaration.
  - [x] Add explicit Port-to-Anchor bindings for all visible connection endpoints.
  - [x] Add power, control, and PE Connections with labels.
  - [x] Add explicit junction and explicit no-connect crossing facts in Athena source.
- [x] Add safe SVG resource (AC: 2, 3)
  - [x] Use package-local SVG with neutral `data-athena-ref` geometry references only.
  - [x] Reject/avoid SVG-owned Port, signal, role, compatibility, component, or route metadata.
  - [x] Avoid XML/QET runtime authority and remote resources.
- [x] Add dedicated tests (AC: 1-6)
  - [x] Assert package declaration matches source folder hierarchy.
  - [x] Assert source contains required M38 example concepts without M36/M37 reuse.
  - [x] Assert SVG contains only allowed neutral Athena geometry references.
  - [x] Compile the example and assert no blocking diagnostics.
- [x] Verify sequentially (AC: 6)
  - [x] Run affected Gradle tests one command at a time.
  - [x] Run source-set hygiene audit.
  - [x] Run encoding audit.
  - [x] Run forbidden scan for XML/QET/mock presentation/hardcoded sample policy/M36-M37 reuse.
  - [x] Run `git diff --check`.

## Dev Notes

M38-E4 proves the current truthful drawing path. This story only creates the dedicated example and
its structural tests. Story 4.3 owns Electron screenshots and product E2E proof.

Hard rules:

- Stay in M38. Do not change M36/M37 examples.
- No Java2D. Render layer remains Theia/IDE.
- No normal-source syntax for route bends, paint, label offsets, transforms, or renderer mechanics.
- Athena source is SSOT for engineering facts.
- SVG is geometry only. `data-athena-ref` may identify geometry; it cannot define Ports, signals,
  roles, components, compatibility, routes, or labels.
- XML/QET material may be reference knowledge only, never runtime authority.
- Pre-public cleanup rule applies: if an old path blocks the current example, refactor/delete it
  directly, no compatibility shim.

Relevant current files and patterns:

- `examples/m34/professional-control-drawing` shows old professional drawing package hierarchy.
- `examples/m36/connectivity-cabinet` shows package-local SVG resources but must not be reused.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodySupport.kt`
  owns safe SVG geometry admission.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationConnectorCompiler.kt`
  owns strict endpoint connector lowering.
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedProfessionalDrawingSampleTest.kt`
  currently points at M37; add a dedicated M38 test instead of broadening old milestone proof.

Previous story learning:

- Story 3.3 made endpoint inspection use typed `PresentationDocument` connector endpoints only.
- Connector endpoints need source provenance and projection IDs for Port, binding, occurrence, and
  Anchor.
- Theia rejects malformed endpoint trace. This example must feed those facts through source and
  compiler, not frontend inference.

Testing requirements:

```powershell
.\gradlew.bat --no-daemon --console=plain -q :kernel:compiler:test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

If LSP/Theia product scripts are touched, also run their relevant `yarn test` sequentially.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-31: Story created from M38 sprint order after Story 3.3 reached review.
- 2026-07-31: Dev started from sprint order.
- 2026-07-31: Focused M38 example test first failed on unsupported `Drive`, invalid `m38` evidence namespace, incompatible roles, missing SVG-backed anchors, missing label slot, and unsupported `svg-vendor` variant. Fixed source/example material directly; no compatibility shim.
- 2026-07-31: Verification passed sequentially: focused M38 example test, full `:kernel:compiler:test`, source-set hygiene audit, encoding audit, sample forbidden scan, and `git diff --check`.

### Completion Notes

- Created dedicated `examples/m38/professional-control-drawing` project with Java-style package hierarchy.
- Added source-first M38 engineering example with power/control/PE connections, explicit PE junction evidence, explicit no-connect crossing evidence, package-local SVG-backed vendor-drive Element, native terminal/power/breaker/motor/PE Elements, and explicit Port-to-Anchor material resolution.
- Kept SVG as geometry-only resource with neutral `data-athena-ref` hints; `.athena` owns device, Port, signal, role, connection, evidence, and binding facts.
- Added dedicated compiler test proving hierarchy, forbidden material absence, safe SVG hints, clean compile, material resolution, and Control Drawing connector publication.

### File List

- `_bmad-output/implementation-artifacts/m38/4-1-author-the-dedicated-m38-example.md`
- `_bmad-output/implementation-artifacts/m38/sprint-status.yaml`
- `examples/m38/professional-control-drawing/athena.yaml`
- `examples/m38/professional-control-drawing/src/com/engineeringood/m38/professionalcontroldrawing/01-professional-control-drawing.athena`
- `examples/m38/professional-control-drawing/packages/representation/com/engineeringood/m38/professional/drawing-profile.athena`
- `examples/m38/professional-control-drawing/packages/representation/com/engineeringood/m38/professional/m38-elements.athena`
- `examples/m38/professional-control-drawing/packages/representation/com/engineeringood/m38/professional/m38-bindings.athena`
- `examples/m38/professional-control-drawing/packages/representation/com/engineeringood/m38/professional/svg/vendor-drive.svg`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM38ProfessionalDrawingSampleTest.kt`

### Change Log

- 2026-07-31: Created Story 4.1 context and marked ready for dev.
- 2026-07-31: Started Story 4.1 implementation.
- 2026-07-31: Implemented dedicated M38 source-first example and marked story ready for review.
