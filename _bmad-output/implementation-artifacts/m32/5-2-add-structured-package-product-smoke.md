---
status: review
baseline_commit: 8d12ba03c05b373b98fbc513e6c7f3f6fce962d6
story_id: 5.2
epic: 5
title: Add Structured Package Product Smoke
---

# Story 5.2: Add Structured Package Product Smoke

## Status

Review

## Story

As an Athena maintainer,
I want structured product smoke for package resolution and rendering,
so that M32 completion does not depend on visual guessing.

## Required Context

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/prd.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m32/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/implementation-artifacts/m32/epics.md`
- Previous story: `_bmad-output/implementation-artifacts/m32/5-1-create-m32-sample-packages-and-project.md`
- Epic 4 retrospective: `_bmad-output/implementation-artifacts/m32/epic-4-retro-2026-07-22.md`
- Usage docs: `docs/usages/engineering-package-platform.md`

## Acceptance Criteria

1. Given the exact M32 sample, when product smoke runs, then it verifies engineering package
   resolution, representation package resolution, manifest selection, descriptor validation,
   anchor mapping, label binding, route anchoring, derived bounds, profile switching, and no
   fallback rendering.
2. Given visual evidence is captured, when screenshot proof is inspected, then it is human-review
   evidence only and is backed by structured assertions for every semantic and package claim.
3. Given the story implementation is complete, when smoke scripts, proof payloads, screenshots,
   docs, package runtime fixtures, and sprint artifacts are reviewed, then stale proof paths are
   removed or ledgered and AC evidence is recorded.
4. Mandatory Polish/Purge Gate complete.

## Tasks/Subtasks

- [x] Add RED tests for a structured M32 product smoke proof over the Story 5.1 sample. (AC: 1)
- [x] Add RED tests proving screenshot/visual evidence is secondary and cannot satisfy package
  claims without structured assertions. (AC: 2)
- [x] Implement structured smoke proof payloads at the package-runtime/sample boundary using
  resolver, binding, descriptor validation, occurrence, render, route, bounds, profile switch, and
  no-fallback evidence. (AC: 1,2)
- [x] Document the product smoke proof and screenshot-secondary rule. (AC: 2,3)
- [x] Run focused tests sequentially, then full regression sequentially; do not run Gradle
  concurrently on Windows. (AC: 1..3)
- [x] Complete mandatory polish/purge review and record final evidence. (AC: 3,4)

## Dev Notes

- Consume `M32SamplePackageSet`, `BindingResolver`, descriptor validators,
  `PackageBackedRepresentationOccurrenceFactory`, `DescriptorBackedGraphicResourceRenderPayloadMapper`,
  and `DescriptorAnchorRouteEvidenceMapper` rather than inventing a parallel proof path.
- The smoke proof must be structured data. Screenshot fields may exist only as secondary
  human-review references and must not be accepted unless structured checks pass.
- Prove no fallback: binding fallback, renderer fallback, route center fallback, and visual generic
  fallback should be false/diagnostic.
- Keep implementation in package-runtime/sample proof boundary. Do not add Theia DOM parsing,
  renderer-owned package decisions, or `.athena` visual syntax.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text edits.

## Testing Requirements

- Follow TDD: write failing smoke proof tests before production code.
- Focused command should target the package runtime module.
- Full regression command after story completion:
  `.\gradlew.bat --no-daemon --console=plain check`.
- Encoding audit command after docs/text edits:
  `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test` failed before implementation because `M32ProductSmokeProofRunner` was unresolved.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test` passed after adding `M32ProductSmokeProofRunner` and structured proof payloads.
- REFACTOR VERIFY: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test` passed after documentation updates.
- REGRESSION: `.\gradlew.bat --no-daemon --console=plain check` exited 0 with `BUILD SUCCESSFUL`.
- ENCODING: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` exited 0 with `Encoding audit passed.`
- PURGE: `git status --short` showed M32 artifacts, `settings.gradle.kts`, docs, `examples/m32/`, `kernel/package-model/`, and `kernel/package-runtime/`; `.tools` was not present.
- IDE E2E REGRESSION RED: M32 sample failed real Electron/LSP startup because the sample project
  was missing `athena.yaml`/`athena.lock`; after adding those, the IDE opened far enough to expose
  source diagnostics from invalid `project` root syntax and unsupported source-layer device types.
- IDE E2E REGRESSION GREEN: `yarn start:smoke:m32` in `ide/theia-product` passed with
  `ATHENA_M32_PACKAGE_PLATFORM_PROOF`, zero LSP diagnostics for
  `src/01-package-platform-demo.athena`, Outline path
  `M32PackagePlatformDemo > ShutterMotorM32 > up`, default `activeViewId: "cabinet"`, screenshot
  `m32-graph-workbench-smoke.png`, `svgViewBox: "0 12 705 148"`, route/body intersections `0`,
  and fallback representations `[]`.

### Completion Notes List

- Added `M32ProductSmokeProofRunner` and structured proof payloads that compose the sample set,
  Binding Resolver, Binding Evidence payloads, descriptor validation, package-backed occurrence
  creation, descriptor-backed render payloads, route evidence, profile switching, and no-fallback
  checks.
- Added `M32ProductSmokeProofTest` to verify product smoke is structured authority and screenshot
  evidence is secondary human-review context only.
- Documented structured product smoke in `docs/usages/engineering-package-platform.md`.
- Added IDE-openable repository files and product smoke script coverage so package proof now
  includes actual Theia/Electron LSP initialization, Outline, Graph Workbench, screenshot, and
  zero-diagnostic checks.
- AC evidence:
  - AC1: `M32ProductSmokeProofTest` verifies package resolution, representation resolution,
    manifest selection, descriptor validation, anchor mapping, label binding, occurrence creation,
    derived bounds, route anchoring, profile switching, and no accepted fallback.
  - AC2: `M32ProductSmokeProofTest` verifies `acceptanceAuthority == "structured-proof"` and
    screenshot evidence cannot satisfy package claims without structured proof.
  - AC3: docs and purge gate completed; no screenshot-only proof path or visual guessing shortcut
    was retained.
  - AC4: encoding audit and purge/status gate completed.

### File List

- `_bmad-output/implementation-artifacts/m32/5-2-add-structured-package-product-smoke.md`
- `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- `examples/m32/sample-project/athena.yaml`
- `examples/m32/sample-project/athena.lock`
- `examples/m32/sample-project/src/01-package-platform-demo.athena`
- `ide/theia-product/package.json`
- `ide/theia-product/scripts/verify-athena-m32-sample-project.js`
- `docs/usages/engineering-package-platform.md`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M32ProductSmokeProof.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M32SamplePackageSet.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/M32ProductSmokeProofTest.kt`

## Change Log

- 2026-07-22: Story created for M32 Epic 5 structured package product smoke.
- 2026-07-22: Added structured M32 product smoke proof and marked story ready for review after focused and full verification.
- 2026-07-22: Added real IDE/Electron M32 sample smoke after repository-contract and source
  diagnostics were found by E2E verification.

## Mandatory Final Polish/Purge Gate

- Review touched and adjacent smoke proof code, sample files, docs, tests, screenshots, sprint
  status, and cleanup ledger.
- Remove stale screenshot-only proof paths, visual guesses, fallback success claims, or unowned demo
  smoke shortcuts.
- Ledger any retained stale item in `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
  with owner, reason, target milestone, and verification.
- Record AC-to-evidence mapping before moving the story beyond `review`.
