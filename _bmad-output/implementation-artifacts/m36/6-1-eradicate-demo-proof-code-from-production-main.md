---
baseline_commit: 593d0a1dc86c0bac126fbde0501cc07051180346
---

# Story M36-E6.S1: Eradicate Demo And Proof Code From Production Main

Status: done

## Story

As a platform maintainer,
I want all milestone demo/proof/sample logic removed from production `src/main`,
so that Athena's compiler, package runtime, representation model, LSP, and renderer stay clean,
maintainable, and governed by production abstractions rather than milestone fixtures.

## Severity

Critical cleanup/outage story. This is not product feature work.

## Acceptance Criteria

1. Impact analysis maps every upstream and downstream reference to the named polluted production
   classes and records what runtime path each class currently affects.
2. All dedicated test/demo/proof/sample code under production `src/main` is either deleted or moved
   into `src/test`; any valid reusable production behavior is renamed and extracted behind a
   non-milestone production interface.
3. Production `src/main` contains no Athena-owned classes/files with forbidden names:
   `*Proof*`, `*Demo*`, `*Smoke*`, `*Sample*`, `*Fixture*`, `*Test*`, `M[0-9]*`, `*V0*`, or `*V1*`,
   except explicitly reviewed third-party reference mirrors under `reference/`.
4. Production pipelines no longer instantiate or depend on M30/M32/M34/M35 demo/proof classes.
   In particular, `PresentationModelDeriver` and LSP projection session code must use canonical
   production compilers/resolvers, not milestone-specific fallback branches.
5. Test coverage that still has value is preserved in `src/test` and rewritten around production
   interfaces; stale tests asserting demo internals are deleted.
6. A guard prevents recurrence before commit: a script and a Gradle/verifier entry fail when
   forbidden names appear in Athena-owned `src/main`.
7. Full E2E proof passes after cleanup:
   compiler, package runtime, representation model, LSP, Theia/frontend protocol scripts, M36
   dedicated Cabinet sample, encoding audit, `git diff --check`, and full `test`.
8. Cleanup report records deleted, moved, renamed, and preserved files; no compatibility shim is
   allowed because Athena is pre-public.

## Required Scope

### Named Files To Resolve

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/M32PackageBackedPresentationFactDeriver.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M32DemoLayoutDensityProof.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M32ProductSmokeProof.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M32SamplePackageSet.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/M30ControlSheetCompositionProof.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/M30DemoRepresentationBinder.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/M34CabinetRenderPathProof.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaM35CabinetProjectionCompiler.kt`

### Initial Impact Evidence

- `PresentationModelDeriver` has a production constructor dependency on
  `M32PackageBackedPresentationFactDeriver`; this corrupts the compiler presentation pipeline by
  deriving package-backed representation facts through a milestone sample path.
- `M32PackageBackedPresentationFactDeriver` depends on `M32SamplePackageSet.loadDefault()`, which
  makes production derivation dependent on hardcoded sample package material.
- `AthenaProjectionSessionProtocol` imports and uses `AthenaM35CabinetProjectionCompiler` for both
  M35 and M36 IDE projection sessions; this makes live LSP projection behavior depend on a
  milestone-named compiler.
- `M30DemoRepresentationBinder` is in `representation-model/src/main` but is only referenced by
  tests; it should be moved to test fixtures or deleted.
- `M30ControlSheetCompositionProof` and `M34CabinetRenderPathProof` are proof payloads in
  `representation-model/src/main`; their production value must be reclassified as either test-only
  proof helpers or renamed production evidence contracts.
- `M32DemoLayoutDensityProof`, `M32ProductSmokeProof`, and `M32SamplePackageSet` are in
  `package-runtime/src/main`; current caller evidence shows self-contained runners plus tests, not
  production package-runtime need.

### Global Audit Seed Results

Athena-owned `src/main` files initially flagged:

- `apps/desktop-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerOperatorProofVerifier.kt`
- `apps/desktop-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerSmokeVerifier.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDrawingProofPayloads.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaM35CabinetProjectionCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/M32PackageBackedPresentationFactDeriver.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M32DemoLayoutDensityProof.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M32ProductSmokeProof.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M32SamplePackageSet.kt`
- `kernel/physical-model/src/main/kotlin/com/engineeringood/athena/physical/PhysicalConstraintEvaluatorV0.kt`
- `kernel/presentation-policy-model/src/main/kotlin/com/engineeringood/athena/policy/AthenaIndustrialControlV0Profile.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/CompositionBoundsProofGuard.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/DrawingProofPayloads.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/M30ControlSheetCompositionProof.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/M30DemoRepresentationBinder.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/M34CabinetRenderPathProof.kt`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/AthenaRouteEngineV0.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiDeterministicProofProvider.kt`

Third-party/reference mirror hits under `reference/` are not Athena production code, but the guard
must make that exclusion explicit.

## Root Cause Hypothesis To Verify

Do not assign blame to a person without commit evidence. The current evidence points to process
failure:

- Milestone proof classes were accepted into `src/main` because story ACs demanded structured proof,
  but the stories did not require source-set segregation.
- Reviews verified behavior and test pass state, but did not enforce naming/source-set hygiene.
- Milestone names were used as convenient anchors during rapid delivery and then became production
  imports.
- Deletion gates existed for specific render paths, but there was no global architectural guard for
  demo/proof/sample code in production source sets.

The implementation must confirm or correct this with `git log --follow` on each named file.

## Tasks / Subtasks

- [x] Build the dependency map and cleanup ledger (AC: 1, 8)
  - [x] Run CodeGraph caller/callee analysis for every named class.
  - [x] Run `git log --follow -- <file>` for every named file and record the story/commit source.
  - [x] Create a cleanup ledger under `_bmad-output/implementation-artifacts/m36/` listing
    `delete`, `move-to-test`, `rename-production`, or `replace-with-canonical-path` for each file.

- [x] Remove M32 package sample authority from production compiler/package-runtime (AC: 2, 4, 5)
  - [x] Delete or move `M32SamplePackageSet`, `M32ProductSmokeProof`, and
    `M32DemoLayoutDensityProof` out of `src/main`.
  - [x] Replace `PresentationModelDeriver` dependency on `M32PackageBackedPresentationFactDeriver`
    with canonical representation/package resolution or no package facts if no production resolver
    exists yet.
  - [x] Preserve useful package-runtime smoke checks as tests only.

- [x] Remove M30/M34 proof/demo code from representation production paths (AC: 2, 5)
  - [x] Move or delete `M30DemoRepresentationBinder`.
  - [x] Move proof payloads to tests or rename only if they are true production evidence contracts.
  - [x] Rewrite tests to use production interfaces and local test fixtures.

- [x] Rename milestone-specific production compilers and version suffixes (AC: 2, 3, 4)
  - [x] Replace `AthenaM35CabinetProjectionCompiler` with a production name such as
    `AthenaCabinetProjectionCompiler`.
  - [x] Replace `AthenaRouteEngineV0` with `AthenaRouteEngine`.
  - [x] Replace `PhysicalConstraintEvaluatorV0` with `PhysicalConstraintEvaluator`.
  - [x] Replace `AthenaIndustrialControlV0Profile` with `AthenaIndustrialControlProfile`.
  - [x] Update imports, tests, docs, and LSP call sites.

- [x] Global source-set hygiene audit and guard (AC: 3, 6)
  - [x] Add a repository script, for example `tools/source-set-hygiene-audit.ps1`, that scans
    Athena-owned `src/main` and fails on forbidden naming patterns.
  - [x] Add a Gradle verification task or wire the script into an existing verification path.
  - [x] Document allowlist rules; `reference/` may be excluded because it is imported reference
    material, not Athena product source.
  - [x] Add reviewer checklist text under BMad/project docs requiring source-set segregation.

- [x] Documentation cleanup and module README pass (AC: 6, 8)
  - [x] Remove stale docs that describe M32 demo/proof classes as production pathways.
  - [x] Add or update minimal READMEs for touched modules only in this story unless a separate
    module-wide README epic is created.
  - [x] Document critical production flows after cleanup: package resolution, presentation
    derivation, cabinet projection, routing, and proof/evidence generation.

- [x] Full E2E verification gate (AC: 7)
  - [x] `.\gradlew.bat --no-daemon --console=plain clean`
  - [x] `.\gradlew.bat --no-daemon --console=plain test`
  - [x] Focused tests for changed modules:
    `:kernel:compiler:test`, `:kernel:package-runtime:test`,
    `:kernel:representation-model:test`, `:kernel:routing-model:test`,
    `:kernel:physical-model:test`, `:ide:lsp:test`
  - [x] Frontend scripts affected by LSP/projection contracts.
  - [x] M36 dedicated Cabinet sample E2E proof.
  - [x] `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - [x] `git diff --check`
  - [x] `tools/source-set-hygiene-audit.ps1`

## Dev Notes

- This story must not preserve compatibility with milestone class names. Athena is pre-public.
- Do not create shim classes with old M30/M32/M34/M35 names.
- Do not rename test files just to hide the issue. Test code may keep milestone names in `src/test`
  when it describes milestone history.
- Production names must describe current responsibility, not milestone origin.
- Use the deletion test: if removing the class only breaks tests, move/delete it. If production
  behavior spreads into callers, extract the real production module with a small interface.
- Keep the cleanup ledger explicit. Every named file must have a final disposition.
- Do not mix this with M37 planning or feature work.

## Implementation Plan

1. Freeze the current dirty worktree state by recording `git status --short` in the debug log.
2. Run dependency and history analysis for named files.
3. Add the source-set hygiene audit first so the current state fails.
4. Refactor production references from the leaves inward:
   package-runtime sample classes, representation proof/demo classes, compiler deriver, LSP cabinet
   compiler, route/physical/profile version suffixes.
5. Move useful tests to test fixtures and delete stale assertions.
6. Update docs and module READMEs only where touched.
7. Run focused tests, then full E2E.
8. Mark story review only when all ACs and audit gates pass.

## Dev Agent Record

### Agent Model Used

Codex (GPT-5)

### Debug Log References

- 2026-07-29: Created story from codebase cleanup directive after M36 story 5-4 reached review.
- 2026-07-29: Initial CodeGraph analysis found live production callers in
  `PresentationModelDeriver` and `AthenaProjectionSessionProtocol`.
- 2026-07-29: Initial global scan found Athena-owned `src/main` files matching proof/demo/sample,
  milestone, and version-suffix naming patterns.
- 2026-07-29: Removed production sample fallback authority, relocated useful proof helpers to
  `src/test`, deleted the stale desktop viewer and mock AI provider, and renamed production types by
  responsibility.
- 2026-07-29: Added the source-set audit, Gradle gate, cleanup ledger, and review checklist.
- 2026-07-30: Reconciled Tree-sitter with ANTLR anchor syntax and repaired canonical locks and
  evidence identifiers exposed by the Java-style source migration.
- 2026-07-30: Full Gradle test completed successfully with 152 tasks; Tree-sitter 61/61, Theia
  frontend 219/219, Graph GLSP 7/7, encoding, source-set, and diff gates passed.
- 2026-07-30: M36 Electron E2E rendered 21 governed occurrences and 31 orthogonal terminal-anchored
  routes with zero center fallbacks, body intersections, or compatibility views, then wrote three
  screenshots.

### Completion Notes List

- Confirmed root cause as missing source-set and naming review gates, not an attributable person;
  commit history is recorded in `source-set-cleanup-ledger.md`.
- Removed every identified demo/proof/sample production class or replaced real production behavior
  with responsibility-based compilers, validators, engines, profiles, and evidence contracts.
- Removed pre-public compatibility paths, stale XML/fallback bridges, the desktop viewer module, and
  the deterministic mock AI provider.
- Preserved useful M30/M32/M34 fixtures under `src/test` only.
- Added an executable audit wired into root Gradle verification and a mandatory review checklist.
- Verified the dedicated `examples/m36/connectivity-cabinet` product path in Electron and retained
  three current screenshots under the M36 implementation artifacts.

### File List

- `_bmad-output/implementation-artifacts/m36/6-1-eradicate-demo-proof-code-from-production-main.md`
- `_bmad-output/implementation-artifacts/m36/source-set-cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m36/source-set-review-checklist.md`
- `_bmad-output/implementation-artifacts/m36/screenshots/m36-connectivity-cabinet-desktop-1920x1080.png`
- `_bmad-output/implementation-artifacts/m36/screenshots/m36-connectivity-cabinet-desktop-1280x900.png`
- `_bmad-output/implementation-artifacts/m36/screenshots/m36-connectivity-cabinet-narrow.png`
- `_bmad-output/implementation-artifacts/m36/sprint-status.yaml`
- `apps/README.md`
- `apps/README.zh-CN.md`
- `apps/desktop-viewer/` (deleted)
- `build.gradle.kts`
- `settings.gradle.kts`
- `docs/usages/athena-workspace-summary.md`
- `docs/usages/engineering-package-platform.md`
- `examples/m14/siemens-proof-corpus/athena.lock`
- `examples/m14/siemens-proof-corpus/src/com/engineeringood/examples/m14/siemens/proof/corpus/siemens-proof-corpus.athena`
- `examples/m16/semantic-reuse-proof/athena.lock`
- `examples/m16/semantic-reuse-proof/macros/24v-distribution-unit.macro`
- `examples/m16/semantic-reuse-proof/macros/dol-starter.macro`
- `examples/m16/semantic-reuse-proof/macros/plc-rack.macro`
- `examples/m16/semantic-reuse-proof/src/com/engineeringood/examples/m16/semantic/reuse/proof/semantic-reuse-proof.athena`
- `examples/m34/professional-control-drawing/athena.lock`
- `examples/m34/sample-project/athena.lock`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDrawingEvidencePayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionSessionProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaServer.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt`
- `ide/theia-frontend/src/browser/athena-frontend-protocol.ts`
- `ide/theia-product/scripts/verify-athena-m36-connectivity-cabinet.js`
- `ide/tree-sitter-athena/grammar.js`
- `ide/tree-sitter-athena/queries/highlights.scm`
- `ide/tree-sitter-athena/scripts/athena-tree-sitter-symbol-highlights.test.mjs`
- `ide/tree-sitter-athena/test/corpus/symbol.txt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCabinetProjectionCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaM35CabinetProjectionCompiler.kt` (deleted)
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/M32PackageBackedPresentationFactDeriver.kt` (deleted)
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriver.kt`
- `kernel/package-runtime/README.md`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/LegacyBindingPolicyTagRuleAdapter.kt` (deleted)
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M32DemoLayoutDensityProof.kt` (deleted)
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M32ProductSmokeProof.kt` (deleted)
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M32SamplePackageSet.kt` (deleted)
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/M32DemoLayoutDensityProof.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/M32ProductSmokeProof.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/M32SamplePackageSet.kt`
- `kernel/physical-model/src/main/kotlin/com/engineeringood/athena/physical/PhysicalConstraintEvaluator.kt`
- `kernel/physical-model/src/main/kotlin/com/engineeringood/athena/physical/PhysicalConstraintEvaluatorV0.kt` (deleted)
- `kernel/presentation-policy-model/src/main/kotlin/com/engineeringood/athena/policy/AthenaIndustrialControlProfile.kt`
- `kernel/presentation-policy-model/src/main/kotlin/com/engineeringood/athena/policy/AthenaIndustrialControlV0Profile.kt` (deleted)
- `kernel/representation-model/README.md`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/CompositionBoundsValidator.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/DrawingAcceptanceEvidence.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/M30ControlSheetCompositionProof.kt` (deleted)
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/M30DemoRepresentationBinder.kt` (deleted)
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/M34CabinetRenderPathProof.kt` (deleted)
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/M30ControlSheetCompositionProof.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/M30DemoRepresentationBinder.kt`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/AthenaRouteEngine.kt`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/AthenaRouteEngineV0.kt` (deleted)
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiDeterministicProofProvider.kt` (deleted)
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiReasoningRuntimeService.kt`
- `tools/source-set-hygiene-audit.ps1`

## Change Log

- 2026-07-29: Added critical M36 cleanup story for production source-set segregation and prevention
  gates.
- 2026-07-30: Completed production source cleanup, canonical renames, recurrence guard, legacy
  deletion, documentation pass, full regression suite, and M36 screenshot-backed Electron E2E.
