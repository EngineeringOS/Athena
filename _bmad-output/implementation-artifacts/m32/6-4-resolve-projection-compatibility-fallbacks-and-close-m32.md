---
status: review
baseline_commit: 8d12ba03c05b373b98fbc513e6c7f3f6fce962d6
story_id: 6.4
epic: 6
title: Resolve Projection Compatibility Fallbacks And Close M32
---

# Story 6.4: Resolve Projection Compatibility Fallbacks And Close M32

## Status

Review

## Story

As the Athena project owner,
I want legacy projection fallbacks and milestone artifacts closed with evidence,
so that M33 starts from accurate package and projection boundaries.

## Required Context

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/prd.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m32/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/implementation-artifacts/m32/epics.md`
- Cleanup ledger: `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
- Previous story: `_bmad-output/implementation-artifacts/m32/6-3-replace-broad-port-candidate-affordance.md`

## Acceptance Criteria

1. Given M26 display-title sheet-role fallback, when compatibility review runs, then it is removed
   or explicitly versioned outside M31/M32 typed payload authority with tests proving current
   samples do not depend on display-title parsing.
2. Given `_reference` occurrence fixtures, when compatibility review runs, then they are removed,
   renamed, or documented as legacy defensive tests with proof that normal compiler/runtime/LSP
   payloads do not emit duplicate visual reference components.
3. Given all M32 stories have evidence, when final regression runs sequentially, then relevant
   package tests, M27-M32 smoke/proof checks, duplicate sprint-key check, and encoding audit pass
   or any remaining failure is ledgered with owner and target milestone.
4. Given the milestone closes, when retrospectives and sprint status are updated, then every story
   has AC-to-evidence and polish/purge results, all stale artifacts are removed or ledgered, and no
   `.tools` path is staged or committed.
5. Mandatory Polish/Purge Gate complete.

## Tasks/Subtasks

- [x] Run CodeGraph and fixed-string audit for display-title sheet-role fallback,
  `_reference` occurrence fixtures, duplicate visual reference paths, M32 story evidence, and
  sprint status integrity. (AC: 1..4)
- [x] Add RED tests proving current M31/M32 samples do not depend on display-title sheet-role
  parsing and normal payloads do not emit duplicate `_reference` visual components. (AC: 1,2)
- [x] Implement removal/versioning/legacy-fixture closeout at the owning projection/runtime/test
  boundary. (AC: 1,2)
- [x] Run duplicate sprint-key check and final M32 evidence/purge checks. (AC: 3,4)
- [x] Update docs, cleanup ledger, action statuses, sprint artifacts, and Epic 6 retrospective.
  (AC: 3,4)
- [x] Run focused tests sequentially, then full regression sequentially; do not run Gradle
  concurrently on Windows. (AC: 1..4)
- [x] Complete mandatory polish/purge review and record final evidence. (AC: 3..5)

## Dev Notes

- Use CodeGraph before grep/read on projection/runtime source files.
- Do not remove defensive legacy tests unless a stronger proof exists; retained legacy behavior
  must be named, versioned, and ledgered.
- Do not mark stories `done`; M32 story status stops at `review` unless a separate code-review
  workflow moves them.
- Preserve M32 product smoke and density proof from Epic 5.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text edits.
- Ensure no `.tools` path is staged or added.

## Previous Story Intelligence

- Story 6.1 versioned preview/session compatibility as `legacy-preview-readonly-v1`.
- Story 6.2 versioned non-Theia connect command compatibility as
  `legacy-connect-ports-runtime-command-v1`.
- Story 6.3 removed broad `port:` relationship candidate authority and closed `M32-CL-003`.
- Remaining cleanup actions are display-title fallback, `_reference` fixtures, and preserving Epic
  5 M32 sample proof through final cleanup.

## Testing Requirements

- Follow TDD: write failing compatibility/closeout tests before implementation.
- Focused commands should target projection/runtime/LSP/package modules touched by implementation.
- Required final regression command:
  `.\gradlew.bat --no-daemon --console=plain check`.
- Required encoding audit command:
  `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- CodeGraph: `athena-glsp-projection-adapter resolveSheetViewRole adapt projection sheets displayName role policyEvidence`.
- CodeGraph: `_reference occurrence duplicate visual reference compiler runtime LSP projection tests M11 Depth AthenaProjectionRequestTest`.
- RED: `node --test test\athena-graph-glsp-adapter.test.mjs` failed only on
  `does not derive sheet role from displayName when typed policy evidence is absent`, with
  actual `control_logic` versus expected `undefined`.
- GREEN: `yarn test` in `integrations/graph-glsp` passed 7/7 after removing
  `resolveSheetViewRole` and rebuilding `lib`.
- Focused package proof: `.\gradlew.bat --no-daemon --console=plain :kernel:package-model:test`
  passed.
- Focused package proof: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test`
  passed, including M32 product smoke and density proof tests.
- Focused no-duplicate projection proof:
  `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaM30SampleProjectCompilerTest`
  passed.
- Focused runtime projection proof:
  `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaRuntimeProjectionSessionTest`
  passed.
- Focused LSP projection proof:
  `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest`
  passed.
- Product bundle freshness: `yarn build` in `ide/theia-product` finished with 0 build errors and
  refreshed generated desktop output; build emitted an existing Node deprecation warning.
- Duplicate sprint-key check:
  `No duplicate development_status keys in _bmad-output\implementation-artifacts\m32\sprint-status.yaml (32 keys checked).`
- Fallback scan: `rg -n -F "resolveSheetViewRole" integrations ide kernel apps extensions docs examples --glob '!**/build/**' --glob '!**/node_modules/**'`
  returned no matches after rebuild.
- `_reference` scan retained only defensive/historical fixture strings plus explicit no-duplicate
  tests and closeout docs; normal compiler/runtime/LSP payload tests passed.
- Full regression: `.\gradlew.bat --no-daemon --console=plain check` passed.
- Encoding audit: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
- Post-closeout IDE E2E RED/GREEN:
  `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaM32SampleProjectCompilerTest`
  failed before sample repair on repository/source validity, then passed after adding M32
  `athena.yaml`/`athena.lock`, changing the source root to `system`, and using supported
  source-layer device types.
- Post-closeout product E2E:
  `yarn start:smoke:m32` in `ide/theia-product` passed with real Electron LSP initialization,
  zero diagnostics, Outline nested port proof, default Cabinet view, graph proof, and screenshot.

### Completion Notes List

- Removed the graph-glsp display-title sheet-role fallback; sheet role now comes only from typed
  `sheet.role` or normalized `policyEvidence.sheetViewRole`.
- Added graph-glsp RED/GREEN coverage proving `displayName: "Control And PLC Logic"` does not
  become sheet role authority without typed policy evidence.
- Documented `_reference` strings as retained legacy defensive fixtures only, backed by compiler,
  runtime, and LSP no-duplicate projection tests.
- Closed cleanup ledger entries `M32-CL-004-CLOSED` and `M32-CL-005-CLOSED`.
- Preserved Epic 5 M32 sample product smoke and density proof through package-runtime and full
  regression checks.
- Repaired and proved the actual M32 IDE sample repository contract after Electron E2E exposed
  missing `athena.yaml`/`athena.lock` and invalid source syntax/semantic device types.
- Added `start:m32` and `start:smoke:m32` so future closeout uses repeatable IDE product E2E
  instead of manual launch commands.
- Rebuilt Theia product output to purge stale generated display-title fallback code from the local
  IDE bundle.
- Updated sprint action statuses and added the Epic 6 retrospective.
- AC-to-evidence:
  - AC1: RED/GREEN graph-glsp test, removed `resolveSheetViewRole`, no-match fallback scan.
  - AC2: `_reference` closeout docs/ledger plus focused compiler/runtime/LSP no-duplicate tests.
  - AC3: package-model/runtime tests, focused M30/M31 projection tests, duplicate sprint-key check,
    full `check`, encoding audit.
  - AC4: sprint action statuses updated, Epic 6 retrospective created, cleanup ledger closed, no
    `.tools` path staged during final status review.
  - AC5: final polish/purge recorded in this story, cleanup ledger, docs, and sprint status.

### File List

- `_bmad-output/implementation-artifacts/m32/6-4-resolve-projection-compatibility-fallbacks-and-close-m32.md`
- `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m32/epic-6-retro-2026-07-22.md`
- `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- `examples/m32/sample-project/athena.yaml`
- `examples/m32/sample-project/athena.lock`
- `examples/m32/sample-project/src/01-package-platform-demo.athena`
- `ide/theia-product/package.json`
- `ide/theia-product/scripts/verify-athena-m32-sample-project.js`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM32SampleProjectCompilerTest.kt`
- `docs/usages/engineering-package-platform.md`
- `integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`
- `integrations/graph-glsp/test/athena-graph-glsp-adapter.test.mjs`
- `integrations/graph-glsp/lib/athena-glsp-projection-adapter.js`
- `integrations/graph-glsp/lib/athena-glsp-projection-adapter.js.map`
- `integrations/graph-glsp/lib/athena-glsp-projection-adapter.d.ts.map`

## Change Log

- 2026-07-22: Story created for M32 Epic 6 projection compatibility and milestone closeout.
- 2026-07-22: Removed display-title sheet-role fallback, documented `_reference` fixture closeout,
  ran focused/full regression, updated sprint closeout and Epic 6 retrospective.
- 2026-07-22: Added post-closeout M32 IDE/Electron repository-contract regression and product
  smoke script after actual LSP startup failed on the M32 sample.

## Mandatory Final Polish/Purge Gate

- Review projection/runtime/LSP tests, M32 package proof, sprint artifacts, cleanup ledger, story
  evidence, and git status.
- Remove stale display-title/_reference fallbacks or ledger retained compatibility with owner and
  verification.
- Record AC-to-evidence mapping before moving the story beyond `review`.
